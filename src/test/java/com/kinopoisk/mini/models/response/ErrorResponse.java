package com.kinopoisk.mini.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    @JsonProperty("error_code")
    private ErrorCode errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private String timestamp;
}