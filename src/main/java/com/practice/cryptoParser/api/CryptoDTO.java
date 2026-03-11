package com.practice.cryptoParser.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CryptoDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private Long marketCap;
    private Long volume; //24h
    private String circSupply;

}
