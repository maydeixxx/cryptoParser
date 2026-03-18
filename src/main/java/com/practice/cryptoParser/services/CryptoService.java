package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import com.practice.cryptoParser.models.CryptoModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.dao.DataIntegrityViolationException;
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

    public void saveModel(CryptoDTO model) {
        CryptoModel savedModel = cryptoRepository.save(cryptoMapper.dtoToEntity(model));

        if (savedModel.getId() != null) {
            log.info("Crypto saved successfully with id: {}", savedModel.getId());
        } else {
            log.error("unlucky");
        }
    }

    public void parse(int maxPage) {
        Set<String> processedCoins = Collections.synchronizedSet(new HashSet<>());
        Set<String> parsedCoins = new HashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        Semaphore semaphore = new Semaphore(5);
        Queue<String> queueHrefs = new ConcurrentLinkedQueue<>();

        try {
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
                    parsedCoins.add(href);
                }
                queueHrefs.addAll(parsedCoins);

                for (int j = 0; j <= queueHrefs.size(); j++) {

                    executorService.submit(() -> {
                        String href = queueHrefs.poll();
                        try {
                            semaphore.acquire();
                            Map<String, Object> cryptoInfo = getCryptoInfo(href);
                            Integer code = (Integer) cryptoInfo.get("code");

                            if (code != 200) {
                                throw new IllegalAccessException("Полученный код не 200");
                            }

                            String name = (String) cryptoInfo.get("name");

                            if (!processedCoins.add(name.toLowerCase())) {
                                return;
                            }

                            Optional<CryptoModel> existingCrypto = cryptoRepository.findCryptoModelByName(name.toLowerCase());

                            if (existingCrypto.isPresent()) {
                                return;
                            }

                            try {
                                saveModel(CryptoDTO.builder()
                                        .name(name.toLowerCase())
                                        .price((BigDecimal) cryptoInfo.get("price"))
                                        .volume((String) cryptoInfo.get("volume"))
                                        .circSupply((String) cryptoInfo.get("circSupply"))
                                        .marketCap((String) cryptoInfo.get("marketCap"))
                                        .build()
                                );
                                processedCoins.add(name);
                            } catch (DataIntegrityViolationException e) {
                                log.warn("Crypto exists, skip");
                            }

                        } catch (Exception e) {
                            log.error("Error processing crypto: ", e);
                            processedCoins.remove(href);
                            queueHrefs.offer(href);
                        } finally {
                            semaphore.release();
                        }
                    });
                }
            }

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


    public CryptoDTO getById(Long id) {
        return cryptoMapper.entityToDto(
                cryptoRepository.findById(id)
                        .orElseThrow(() -> new NullPointerException(String.format("Dont found crypto by id: %s", id)))
        );
    }

    @Transactional
    public void deleteCrypto(Long id) {
        cryptoRepository.deleteById(id);
        log.info("Crypto {} deleted", id);
    }

    @Transactional
    public void deleteCryptoByName(String name) {
        cryptoRepository.deleteByName(name.toLowerCase());
        log.info("Crypto {} deleted", name);
    }

    public List<CryptoDTO> getAll() {
        return cryptoRepository.findAll().stream().sorted(Comparator.comparing(CryptoModel::getId)).map(cryptoMapper::entityToDto).toList();
    }

    public CryptoDTO getByName(String name) {
        CryptoDTO cryptoDTO = cryptoMapper.entityToDto(
                cryptoRepository.findCryptoModelByName(name.toLowerCase())
                        .orElseThrow(() -> new NullPointerException(String.format("Crypto %s not found", name)))
        );
        return cryptoDTO;
    }

    @Transactional
    public void updateDataForCrypto(String cryptoName) {
        String href = "/currencies/" + cryptoName + "/";
        Map<String, Object> cryptoInfo = getCryptoInfo(href);
        BigDecimal price = new BigDecimal(cryptoInfo.get("price").toString());
        String marketCap = (String) cryptoInfo.get("marketCap");
        String volume = (String) cryptoInfo.get("volume");
        String circSupply = (String) cryptoInfo.get("circSupply");

        CryptoModel cryptoModel = cryptoRepository.findCryptoModelByName(cryptoName).orElseThrow(() -> new NullPointerException(String.format("Crypto %s not found", cryptoName)));
        cryptoModel.setPrice(price);
        cryptoModel.setVolume(volume);
        cryptoModel.setCircSupply(circSupply);
        cryptoModel.setMarketCap(marketCap);
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

            String cryptoName = cryptoPage.selectXpath("//*[@id=\"section-coin-overview\"]/div[1]/h1/span").text().replace(" price", "");

            Elements coinTable = cryptoPage.getElementsByClass("coin-metrics-table");
            String marketCap = coinTable.select("#section-coin-stats > div > div > dl > div:nth-child(1) > div > dd > div > div.sc-c1554bc0-0.hYTYQi > div > span").text();
            String volume = coinTable.select("#section-coin-stats > div > div > dl > div:nth-child(2) > div > dd > div > div.sc-c1554bc0-0.hYTYQi > div > span").text();
            String circSupply = coinTable.select("#section-coin-stats > div > div > dl > div:nth-child(7) > div > dd > div > div.sc-c1554bc0-0.hYTYQi > div > span").text();

            data.put("name", cryptoName);
            data.put("price", price);
            data.put("marketCap", marketCap);
            data.put("volume", volume);
            data.put("circSupply", circSupply);

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

