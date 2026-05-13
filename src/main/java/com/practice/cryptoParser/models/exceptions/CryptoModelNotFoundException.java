package com.practice.cryptoParser.models.exceptions;

public class CryptoModelNotFoundException extends RuntimeException{
    public CryptoModelNotFoundException(String message) {
        super(message);
    }
}
