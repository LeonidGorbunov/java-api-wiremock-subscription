package com.kinopoisk.mini.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {

    @JsonProperty("_userId")
    private String userId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("profile")
    private Profile profile;

    @JsonProperty("billing")
    private Billing billing;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Profile {

        @JsonProperty("email")
        private String email;

        @JsonProperty("_username")
        private String username;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Billing {

        @JsonProperty("features")
        private List<String> features;

        @JsonProperty("price")
        private double price;

        @JsonProperty("currency")
        private String currency;
    }
}