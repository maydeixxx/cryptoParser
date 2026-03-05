package com.practice.cryptoParser.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CryptoDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal marketCap;
    private BigDecimal volume; //24h
    private String circSupply;

}
