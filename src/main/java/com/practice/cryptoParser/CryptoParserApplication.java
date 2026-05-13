package com.practice.cryptoParser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoParserApplication.class, args);
	}

}
