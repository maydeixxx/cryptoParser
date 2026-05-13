package com.practice.cryptoParser.models.exceptions;

public class CryptoParsingException extends RuntimeException{
    public CryptoParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
