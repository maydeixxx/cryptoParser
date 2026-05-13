package com.practice.cryptoParser.api;

import com.practice.cryptoParser.models.ErrorResponse;
import com.practice.cryptoParser.models.exceptions.CryptoModelNotFoundException;
import com.practice.cryptoParser.models.exceptions.CryptoParsingException;
import com.practice.cryptoParser.models.exceptions.CryptoRepositoryException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(CryptoModelNotFoundException.class)
    public ResponseEntity<?> handlerCryptoModelNotFoundException(CryptoModelNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse("Crypto model not found", ex.getMessage(), 400, request);
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CryptoParsingException.class)
    public ResponseEntity<?> handleCryptoParsingException(CryptoParsingException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse("Error parsing crypto", ex.getMessage(), 400, request);
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CryptoRepositoryException.class)
    public ResponseEntity<?> handleCryptoRepositoryException(CryptoRepositoryException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse("Error in repository", ex.getMessage(), 400, request);
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse("IllegalArgumentException", ex.getMessage(), 404, request);
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

    private static ErrorResponse buildErrorResponse(String error, String message, int status, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");

        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .path(path)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
