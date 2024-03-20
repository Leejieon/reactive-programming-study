package com.example.webfluxapi.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * API 응갑 시 일관된 에러 메시지 처리를 위한 클래스
 */
@RestControllerAdvice
public class APIException {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception exp) {
        ApiResponse response = ApiResponse.builder()
                .code(500)
                .message(exp.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
