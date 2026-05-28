package com.relatosdepapel.orders.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Getter
    @Builder
    public static class ErrorResponse {
        private final boolean success;
        private final LocalDateTime timestamp;
        private final String path;
        private final String error;
        private final String message;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .error("ORDER_NOT_FOUND")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .error("OUT_OF_STOCK")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CatalogueCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleCatalogueCommunication(CatalogueCommunicationException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .error("CATALOGUE_SERVICE_UNAVAILABLE")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .error("VALIDATION_ERROR")
                .message(details)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .error("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
