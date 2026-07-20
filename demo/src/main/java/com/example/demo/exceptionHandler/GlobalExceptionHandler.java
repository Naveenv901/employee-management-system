package com.example.demo.exceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //---------------------- VALIDATION ERROR ----------------------
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("errors", fieldErrors);

        return response;
    }

    //---------------------- INVALID JSON ----------------------
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Map<String, Object> handleHttpMessageNotReadableException() {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Invalid request body");

        return response;
    }

    //---------------------- DB ERROR ----------------------
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Database error: " + ex.getMostSpecificCause().getMessage());

        return response;
    }

    //---------------------- TRANSACTION ERROR ----------------------
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TransactionSystemException.class)
    public Map<String, Object> handleTransactionSystemException(TransactionSystemException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Transaction error: " + ex.getRootCause().getMessage());

        return response;
    }

    //---------------------- GENERIC ERROR ----------------------
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("message", ex.getMessage());

        return response;
    }
}