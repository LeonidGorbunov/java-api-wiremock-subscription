package com.kinopoisk.mini.models.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public enum SubscriptionType {

    REGULAR(9.99, List.of("HD", "CATALOG")),
    REGULAR_LONG(24.99, List.of("HD", "CATALOG", "OFFLINE_DOWNLOAD")),
    PREMIUM(14.99, List.of("4K", "NO_ADS", "MUSIC")),
    FAMILY(19.99, List.of("4K", "NO_ADS", "MUSIC", "UP_TO_5_ACCOUNTS"));

    private final double price;
    private final List<String> features;
}