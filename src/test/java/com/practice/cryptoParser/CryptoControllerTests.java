package com.practice.cryptoParser;

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

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CryptoControllerTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

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
}
