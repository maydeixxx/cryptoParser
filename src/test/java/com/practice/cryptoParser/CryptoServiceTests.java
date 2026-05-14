package com.practice.cryptoParser;

import com.practice.cryptoParser.models.CryptoDomain;
import com.practice.cryptoParser.models.exceptions.CryptoModelNotFoundException;
import com.practice.cryptoParser.services.CryptoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@SpringBootTest
@Transactional
public class CryptoServiceTests {
    @Autowired
    private CryptoService cryptoService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Успешное сохранение монеты")
    void createCrypto() {
        //given
        String name = "bibi";
        BigDecimal price = new BigDecimal("111.1");
        String marketCap = "12B";
        String volume = "13B";
        String url = "/currencies/";

        CryptoDomain cryptoModel = CryptoDomain.createCryptoModel(name, price, marketCap, volume, url);

        //when
        cryptoService.saveModel(cryptoModel);

        //then
        CryptoDomain bnb = cryptoService.getByName("bibi");

        Assertions.assertEquals(name, bnb.getName());
        Assertions.assertEquals(price, bnb.getPrice());
    }

    @Test
    @DisplayName("Метод парсинга")
    void parseMethod() {
        //given
        int maxPage = 1;

        //when
        cryptoService.parse(maxPage);

        //then
        CryptoDomain bnb = cryptoService.getByName("bnb");

        Assertions.assertEquals("bnb", bnb.getName());
    }

    @Test
    @DisplayName("GetById")
    void getById() {
        //given
        Long id = 1L;

        //when && then
        assertDoesNotThrow(() -> cryptoService.getById(id));
    }

    @Test
    @DisplayName("Удаление и неуспешный поиск монеты по id")
    void deleteAndFindByIdNotSuccessful() {
        //given
        Long id = 2L;

        //when
        cryptoService.deleteCrypto(id);
        CryptoModelNotFoundException ex = Assertions.assertThrows(CryptoModelNotFoundException.class, () -> cryptoService.getById(id));

        //then

        Assertions.assertEquals("Dont found crypto by id: 2", ex.getMessage());
    }

    @Test
    @DisplayName("Удаление монеты по имени")
    void deleteCryptoByName() {
        //given
        String name = "bnb";

        //when && then
        Assertions.assertDoesNotThrow(() -> cryptoService.deleteCryptoByName(name));
    }

}
