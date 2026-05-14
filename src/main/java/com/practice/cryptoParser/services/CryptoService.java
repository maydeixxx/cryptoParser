package com.practice.cryptoParser.services;

import com.practice.cryptoParser.models.CryptoDomain;
import com.practice.cryptoParser.models.CryptoModel;
import com.practice.cryptoParser.models.exceptions.CryptoModelNotFoundException;
import com.practice.cryptoParser.models.exceptions.CryptoParsingException;
import com.practice.cryptoParser.models.exceptions.CryptoRepositoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private final HttpClient httpClient;
    private final CryptoRepository cryptoRepository;
    private final CryptoMapper cryptoMapper;

    public void saveModel(CryptoDomain model) {
        CryptoModel savedModel = cryptoRepository.save(cryptoMapper.domainToEntity(model));

        if (savedModel.getId() != null) {
            log.info("Crypto saved successfully with id: {}", savedModel.getId());
        } else {
            log.error("unlucky");
        }
    }

    public void parse(int maxPage) {
        Set<String> processedCoins = Collections.synchronizedSet(new HashSet<>());
        Set<String> processedHrefs = Collections.synchronizedSet(new HashSet<>());
        Semaphore semaphore = new Semaphore(5);
        Queue<String> queueHrefs = new ConcurrentLinkedQueue<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(20)) {
            for (int i = 1; i <= maxPage; i++) {
                String url = "https://coinmarketcap.com/?page=" + i;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Document htmlPage = Jsoup.parse(response.body());
                Elements hrefs = htmlPage.select("a.cmc-link");

                log.info("Page {}: found {} coins", i, hrefs.size());

                for (Element element : hrefs) {
                    String href = element.attr("href");
                    if (!href.startsWith("/currencies/") || href.startsWith("/currencies/coinmarketcap")) {
                        continue;
                    }
                    if (processedHrefs.add(href)) {
                        queueHrefs.offer(href);
                    }
                }
            }

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            while (!queueHrefs.isEmpty()) {
                String href = queueHrefs.poll();
                if (href == null) break;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();

                        String url;
                        if (href.contains("#markets")) {
                            url = href.replace("#markets", "");
                        } else {
                            url = href;
                        }

                        Map<String, Object> cryptoInfo = getCryptoInfo(href);
                        Integer code = (Integer) cryptoInfo.get("code");

                        if (code != 200) {
                            log.warn("Failed to get crypto info for {}: code {}", href, code);
                            return;
                        }

                        String name = (String) cryptoInfo.get("name");
                        String nameLower = name.toLowerCase();

                        if (!processedCoins.add(nameLower)) {
                            log.debug("Crypto {} already processed", nameLower);
                            return;
                        }

                        Optional<CryptoModel> existingCrypto = cryptoRepository.findCryptoModelByName(nameLower);

                        if (existingCrypto.isPresent()) {
                            log.debug("Crypto {} already exists in DB", nameLower);
                            return;
                        }

                        try {
                            saveModel(CryptoDomain.createCryptoModel(nameLower,
                                    (BigDecimal) cryptoInfo.get("price"),
                                    (String) cryptoInfo.get("marketCap"),
                                    (String) cryptoInfo.get("volume"),
                                    url
                            ));
                        } catch (DataIntegrityViolationException e) {
                            log.warn("Crypto {} exists, skip", nameLower);
                            processedCoins.remove(nameLower);
                        } catch (Exception e) {
                            log.error("Error while saving crypto {}: {}", nameLower, e.getMessage());
                            throw new CryptoRepositoryException("Error while saving crypto " + nameLower);
                        }

                    } catch (Exception e) {
                        log.error("Error processing crypto {}: {}", href, e.getMessage());
                        throw new CryptoParsingException("Error while parsing crypto: " + e.getMessage());
                    } finally {
                        semaphore.release();
                    }
                }, executorService);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            executorService.shutdown();

            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }

        } catch (NumberFormatException e) {
            log.error("NUMBER FORMAT EXCEPTION", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public CryptoDomain getById(Long id) {
        return cryptoMapper.entityToDomain(
                cryptoRepository.findById(id)
                        .orElseThrow(() -> new CryptoModelNotFoundException(String.format("Dont found crypto by id: %s", id)))
        );
    }

    @Transactional
    public void deleteCrypto(Long id) {
        try {
            cryptoRepository.deleteById(id);
            log.info("Crypto {} deleted", id);
        } catch (Exception e) {
            throw new CryptoRepositoryException(String.format("Ошибка при удалении монеты id[%s]", id));
        }
    }

    @Transactional
    public void deleteCryptoByName(String name) {
        try {
            cryptoRepository.deleteByName(name.toLowerCase());
            log.info("Crypto {} deleted", name);
        } catch (Exception e) {
            throw new CryptoRepositoryException(String.format("Ошибка при удалении монеты [%s]", name));
        }
    }

    public List<CryptoDomain> getAll() {
        return cryptoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(CryptoModel::getId))
                .map(cryptoMapper::entityToDomain)
                .toList();
    }

    public CryptoDomain getByName(String name) {
        return cryptoMapper.entityToDomain(cryptoRepository
                .findCryptoModelByName(name)
                .orElseThrow(() -> new CryptoModelNotFoundException(String.format("Монета %s не найдена", name)))
        );
    }

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void updateDataForCrypto() {
        List<CryptoDomain> all = getAll();
        if (all.isEmpty()) {
            return;
        }

        List<CryptoModel> modelsToUpdate = Collections.synchronizedList(new ArrayList<>());

        try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (CryptoDomain crypto : all) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        String cryptoName = crypto.getName();
                        Map<String, Object> cryptoInfo = getCryptoInfo(crypto.getUrl());

                        Object priceObj = cryptoInfo.get("price");
                        if (priceObj == null) {
                            log.warn("Price is null for crypto: {}, skipping update", cryptoName);
                            return;
                        }

                        BigDecimal price = new BigDecimal(priceObj.toString());
                        String marketCap = (String) cryptoInfo.get("marketCap");
                        String volume = (String) cryptoInfo.get("volume");

                        CryptoModel cryptoModel = cryptoRepository.findCryptoModelByName(cryptoName)
                                .orElseThrow(
                                        () -> new NullPointerException(String.format("Crypto %s not found", cryptoName))
                                );

                        cryptoModel.setPrice(price);
                        cryptoModel.setVolume(volume);
                        cryptoModel.setMarketCap(marketCap);

                        modelsToUpdate.add(cryptoModel);
                    } catch (Exception e) {
                        log.warn("Error updating crypto: {} : {}", crypto.getName(), e.getMessage());
                    }
                }, executorService);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }

        } catch (Exception e) {
            log.error("Error in updateDataForCrypto: {}", e.getMessage());
        }

        if (!modelsToUpdate.isEmpty()) {
            cryptoRepository.saveAll(modelsToUpdate);
            log.info("Updated {} cryptocurrencies", modelsToUpdate.size());
        }
    }

    private Map<String, Object> getCryptoInfo(String href) {
        Map<String, Object> data = new HashMap<>();
        String url = "https://coinmarketcap.com";

        if (href == null) {
            log.error("Полученный href == null");
            return Map.of("code", 404);
        }

        if (!href.startsWith("/currencies/") || href.startsWith("/currencies/coinmarketcap")) {
            return Map.of("code", 404);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + href))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BadRequestException(": " + response.statusCode());
            }

            Document cryptoPage = Jsoup.parse(response.body());
            BigDecimal price = new BigDecimal(cryptoPage.selectXpath("//*[@id=\"section-coin-overview\"]/div[2]/span").text().substring(1).replace(",", ""));

            log.info(String.valueOf(price));

            String cryptoName = cryptoPage.selectXpath("//*[@id=\"section-coin-overview\"]/div[1]/h1/span").text().replace(" price", "");

            Elements coinTable = cryptoPage.getElementsByClass("coin-metrics-table");
            String marketCap = coinTable.select("#section-coin-stats > div > div > dl > div:nth-child(1) > div > dd > div > div.sc-c1554bc0-0.hYTYQi > div > span").text();
            String volume = coinTable.select("#section-coin-stats > div > div > dl > div:nth-child(2) > div > dd > div > div.sc-c1554bc0-0.hYTYQi > div > span").text();

            data.put("name", cryptoName);
            data.put("price", price);
            data.put("marketCap", marketCap);
            data.put("volume", volume);

            data.forEach((field, object) -> {
                if (object == null) {
                    throw new NullPointerException(String.format("Field %s is null", field));
                }
            });

            data.put("code", 200);
        } catch (Exception e) {
            log.info("ERROR: {}", e.getMessage());
            return Map.of("code", 404,
                    "error", e.getMessage());
        }
        return data;
    }

}

