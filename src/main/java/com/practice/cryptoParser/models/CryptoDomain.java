package com.practice.cryptoParser.models;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class CryptoDomain {
    @Setter
    private Long id;
    private final String name;
    private final BigDecimal price;
    private final String marketCap;
    private final String volume; //24h
    private final String url;

    public static CryptoDomain createCryptoModel(String name, BigDecimal price, String marketCap, String volume, String url) {
        if (name == null) {
            throw new IllegalArgumentException("Имя монеты обязательно к указанию");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Имя монеты не может быть пустым");
        }

        if (price == null) {
            throw new IllegalArgumentException("Цена обязательна");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена монеты не может быть отрицательной");
        }

        if (marketCap == null) {
            throw new IllegalArgumentException("Marketcap обязательно к указанию");
        }
        if (marketCap.isBlank()) {
            throw new IllegalArgumentException("MarketCap не может быть пустым");
        }

        if (volume == null) {
            throw new IllegalArgumentException("Volume монеты обязательно к указанию");
        }
        if (volume.isBlank()) {
            throw new IllegalArgumentException("Volume монеты не может быть пустым");
        }

        return new CryptoDomain(name, price, marketCap, volume, url);
    }
}
