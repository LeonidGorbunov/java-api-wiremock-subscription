package com.kinopoisk.mini.tests;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.kinopoisk.mini.client.LoginClient;
import com.kinopoisk.mini.client.ProfileClient;
import com.kinopoisk.mini.stubs.SubscriptionStubs;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@WireMockTest
public abstract class BaseTest {

    protected LoginClient loginClient;
    protected ProfileClient profileClient;

    @BeforeAll
    public static void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        RestAssured.baseURI = wmRuntimeInfo.getHttpBaseUrl();
    }

    @BeforeEach
    public void setUpStubsAndClients() {
        SubscriptionStubs.configureAllStubs();
        loginClient = new LoginClient();
        profileClient = new ProfileClient();
    }
}