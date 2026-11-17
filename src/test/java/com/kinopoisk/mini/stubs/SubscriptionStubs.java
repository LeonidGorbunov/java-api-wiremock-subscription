package com.kinopoisk.mini.stubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinopoisk.mini.constants.ApiEndpoints;
import com.kinopoisk.mini.constants.TestData;
import com.kinopoisk.mini.models.request.SubscriptionType;
import com.kinopoisk.mini.models.response.ErrorCode;
import com.kinopoisk.mini.models.response.ErrorResponse;
import com.kinopoisk.mini.models.response.LoginResponse;
import com.kinopoisk.mini.models.response.SubscriptionResponse;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpStatus;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@UtilityClass
public class SubscriptionStubs {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    private static String toJson(Object body) {
        return OBJECT_MAPPER.writeValueAsString(body);
    }

    public static void stubLogin() {
        stubFor(post(urlEqualTo(ApiEndpoints.SIGN_IN))
                .withRequestBody(matchingJsonPath("$.email", equalTo(TestData.VALID_EMAIL)))
                .withRequestBody(matchingJsonPath("$.password", equalTo(TestData.VALID_PASSWORD)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(toJson(new LoginResponse(TestData.VALID_TOKEN)))));
    }

    public static void stubUnauthorizedProfileUpdate() {
        String responseBody = toJson(ErrorResponse.builder()
                .errorCode(ErrorCode.UNAUTHORIZED)
                .message("Invalid or missing authorization token")
                .timestamp(TestData.DEFAULT_TIMESTAMP)
                .build());

        stubFor(patch(urlEqualTo(ApiEndpoints.USER_PROFILE))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_UNAUTHORIZED)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(responseBody)));

        stubFor(patch(urlEqualTo(ApiEndpoints.USER_PROFILE))
                .atPriority(1)
                .withHeader("Authorization", notMatching("Bearer " + TestData.VALID_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_UNAUTHORIZED)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(responseBody)));
    }

    public static void stubUserNotFound() {
        String responseBody = toJson(ErrorResponse.builder()
                .errorCode(ErrorCode.USER_NOT_FOUND)
                .message("Invalid '_userId' field, user not found")
                .timestamp(TestData.DEFAULT_TIMESTAMP)
                .build());

        stubFor(patch(urlEqualTo(ApiEndpoints.USER_PROFILE))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer " + TestData.VALID_TOKEN))
                .withRequestBody(matchingJsonPath("$._userId", equalTo(TestData.UNKNOWN_USER_ID)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_NOT_FOUND)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(responseBody)));
    }

    public static void stubValidationError() {
        String responseBody = toJson(ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Missing 'subscriptionType' field")
                .timestamp(TestData.DEFAULT_TIMESTAMP)
                .build());

        stubFor(patch(urlEqualTo(ApiEndpoints.USER_PROFILE))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer " + TestData.VALID_TOKEN))
                .withRequestBody(matchingJsonPath("$[?(!@.subscriptionType)]"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_BAD_REQUEST)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(responseBody)));
    }

    public static void stubSuccessSubscriptionUpdate(SubscriptionType subscriptionType) {
        var profile = SubscriptionResponse.Profile.builder()
                .email(TestData.VALID_EMAIL)
                .username(TestData.VALID_USERNAME)
                .build();

        var billing = SubscriptionResponse.Billing.builder()
                .features(subscriptionType.getFeatures())
                .price(subscriptionType.getPrice())
                .currency(TestData.CURRENCY_USD)
                .build();

        String subscriptionResponseBody = toJson(SubscriptionResponse.builder()
                .userId(TestData.VALID_USER_ID)
                .status(TestData.STATUS_ACTIVE)
                .updatedAt(TestData.DEFAULT_TIMESTAMP)
                .profile(profile)
                .billing(billing)
                .build());

        stubFor(patch(urlEqualTo(ApiEndpoints.USER_PROFILE))
                .atPriority(3)
                .withHeader("Authorization", equalTo("Bearer " + TestData.VALID_TOKEN))
                .withRequestBody(matchingJsonPath("$._userId", equalTo(TestData.VALID_USER_ID)))
                .withRequestBody(matchingJsonPath("$.subscriptionType", equalTo(subscriptionType.name())))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", ContentType.JSON.toString())
                        .withBody(subscriptionResponseBody)));

    }

    public static void configureAllStubs() {
        stubLogin();
        stubUnauthorizedProfileUpdate();
        stubUserNotFound();
        stubValidationError();
        for (SubscriptionType type : SubscriptionType.values()) {
            stubSuccessSubscriptionUpdate(type);
        }
    }
}
