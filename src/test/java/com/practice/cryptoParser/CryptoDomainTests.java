package com.practice.cryptoParser;

import com.practice.cryptoParser.models.CryptoDomain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CryptoDomainTests {

    @Test
    @DisplayName("Успешное создание CryptoDomain")
    void successfulCreateCryptoDomain() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        //when
        CryptoDomain cryptoModel = CryptoDomain.createCryptoModel(name, price, marketCap, volume, url);

        //then
        Assertions.assertEquals(name, cryptoModel.getName());
        Assertions.assertEquals(price, cryptoModel.getPrice());
        Assertions.assertEquals(marketCap, cryptoModel.getMarketCap());
        Assertions.assertEquals(volume, cryptoModel.getVolume());
        Assertions.assertEquals(url, cryptoModel.getUrl());
    }

    @Test
    @DisplayName("Создание CryptoDomain с null name")
    void createCryptoDomainNullName() {
        //given
        String name = null;
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Имя монеты обязательно к указанию", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с blank name")
    void createCryptoDomainBlankName() {
        //given
        String name = "";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Имя монеты не может быть пустым", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с null price")
    void createCryptoDomainNullPrice() {
        //given
        String name = "bnb";
        BigDecimal price = null;
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Цена обязательна", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с negative price")
    void createCryptoDomainNegativePrice() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("-111.1");
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Цена монеты не может быть отрицательной", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с null marketcap")
    void createCryptoDomainNullMarketCap() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = null;
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Marketcap обязательно к указанию", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с blank marketcap")
    void createCryptoDomainBlankMarketCap() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "";
        String volume = "13B";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("MarketCap не может быть пустым", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с null volume")
    void createCryptoDomainNullVolume() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "11B";
        String volume = null;
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Volume монеты обязательно к указанию", ex.getMessage());
    }

    @Test
    @DisplayName("Создание CryptoDomain с blank volume")
    void createCryptoDomainBlankVolume() {
        //given
        String name = "bnb";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "45B";
        String volume = "";
        String url = "/currencies/";

        //when
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> CryptoDomain.createCryptoModel(name, price, marketCap, volume, url));

        //then
        Assertions.assertEquals("Volume монеты не может быть пустым", ex.getMessage());
    }
}
