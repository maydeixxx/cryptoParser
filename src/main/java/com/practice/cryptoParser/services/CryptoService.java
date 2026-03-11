package com.practice.cryptoParser.services;

import com.practice.cryptoParser.api.CryptoDTO;
import com.practice.cryptoParser.models.CryptoModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


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
        try {
            for (int i = 1; i <= maxPage; i++) {
                String url = "https://coinmarketcap.com/?page=" + i;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Document htmlPage = Jsoup.parse(response.body());
                Elements circSupply = htmlPage.getElementsByClass("circulating-supply-value");
                Elements elements = htmlPage.getElementsByClass("coin-item-name");
                Elements values = htmlPage.getElementsByClass("ilZTOW");
                Elements marketCaps = htmlPage.getElementsByClass("jfwGHx");
                Elements volume24H = htmlPage.getElementsByClass("font_weight_500");

                log.info(String.valueOf(elements.size()));
                for (int j = 0; j <= 15-1; j++){
                    saveModel(CryptoDTO.builder()
                            .price(new BigDecimal(values.get(j).text().substring(1).replace(",","")))
                            .name(elements.get(j).text().toLowerCase())
                            .circSupply(circSupply.get(j).text())
                            .marketCap(Long.parseLong(marketCaps.get(j).text().substring(1).replace(",","")))
                            .volume(Long.parseLong(volume24H.get(j).text().substring(1).replace(",","")))
                            .build()
                    );
                }
            }
        } catch (NumberFormatException e) {
            log.error("NUMBER FORMAT EXCEPTION");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error(e.getMessage());
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
        return cryptoRepository.findAll().stream().map(cryptoMapper::entityToDto).toList();
    }

    public CryptoDTO getByName(String name) {
        return cryptoMapper.entityToDto(
                cryptoRepository.getByName(name.toLowerCase())
                .orElseThrow(() -> new NullPointerException(String.format("Crypto %s not found", name)))
        );
    }

}

