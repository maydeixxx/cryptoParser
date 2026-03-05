package com.practice.cryptoParser.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "crypto")
public class CryptoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal marketCap;
    private BigDecimal volume; //24h
    private String circSupply;

}
