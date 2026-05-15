package com.practice.cryptoParser;

import com.practice.cryptoParser.models.CryptoDomain;
import com.practice.cryptoParser.services.CryptoRepository;
import com.practice.cryptoParser.services.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CryptoControllerTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private CryptoRepository cryptoRepository;

    @BeforeEach
    void clearDb() {
        cryptoRepository.deleteAll();
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.username", postgres::getUsername);
    }

    @Test
    @DisplayName("Метод парсинга")
    void getParse() throws Exception {
        //given
        int page = 1;

        //when && then
        mockMvc.perform(MockMvcRequestBuilders.get("/cryptoService/parse/{page}", page))
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    @DisplayName("Get запрос getAll")
    void getAllHttpRequest() throws Exception {
        //given
        CryptoDomain bnb = CryptoDomain.createCryptoModel("bnb", new BigDecimal("123.2"), "12B", "12B", "/bnb");
        CryptoDomain tether = CryptoDomain.createCryptoModel("tether", new BigDecimal("143.2"), "11B", "11B", "/tether");

        //when
        cryptoService.saveModel(bnb);
        cryptoService.saveModel(tether);

        //then
        mockMvc.perform(MockMvcRequestBuilders.get("/cryptoService/getAll"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("bnb"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("tether"));

    }

    @Test
    @DisplayName("Get запрос getById")
    void getByIdHttpRequest() throws Exception {
        //given
        CryptoDomain bnb = CryptoDomain.createCryptoModel("bnb", new BigDecimal("123.2"), "12B", "12B", "/bnb");
        CryptoDomain tether = CryptoDomain.createCryptoModel("tether", new BigDecimal("143.2"), "11B", "11B", "/tether");
        Long id = 1L;

        //when
        cryptoService.saveModel(bnb);
        cryptoService.saveModel(tether);

        //then
        mockMvc.perform(MockMvcRequestBuilders.get("/cryptoService/getById/{id}", id))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("bnb"));

    }

    @Test
    @DisplayName("Get запрос getByName")
    void getByNameHttpRequest() throws Exception {
        //given
        CryptoDomain bnb = CryptoDomain.createCryptoModel("bnb", new BigDecimal("123.2"), "12B", "12B", "/bnb");
        CryptoDomain tether = CryptoDomain.createCryptoModel("tether", new BigDecimal("143.2"), "11B", "11B", "/tether");
        String name = "tether";

        //when
        cryptoService.saveModel(bnb);
        cryptoService.saveModel(tether);

        //then
        mockMvc.perform(MockMvcRequestBuilders.get("/cryptoService/getByName/{name}", name))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("tether"));

    }
}
