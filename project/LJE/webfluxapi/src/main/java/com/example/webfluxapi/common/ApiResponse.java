package com.example.webfluxapi.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * API 응답 시 일관된 결과값을 주기 위한 클래스
 */
@Builder
public class ApiResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("page")
    private Integer page;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("message")
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("data")
    private Object data;
}
