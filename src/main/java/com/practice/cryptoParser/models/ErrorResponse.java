package com.practice.cryptoParser.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timeStamp;
}
