package com.kinopoisk.mini.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionRequest {

    @JsonProperty("_userId")
    private String userId;
    @JsonProperty("subscriptionType")
    private SubscriptionType subscriptionType;
}