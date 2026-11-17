package com.kinopoisk.mini.client;

import com.kinopoisk.mini.constants.ApiEndpoints;
import com.kinopoisk.mini.models.request.SubscriptionRequest;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.given;

public class ProfileClient extends BaseClient{

    @Step("Действие: обновление подписки пользователя {subscriptionRequest.userId}, PATCH /api/v1/users/profile")
    public ValidatableResponse updateSubscription(SubscriptionRequest subscriptionRequest, String token) {

        var requestSpecification = given(BASE_SPEC).body(subscriptionRequest);

        if (token != null && !token.isBlank()) {
            requestSpecification.auth().oauth2(token);
        }

        return requestSpecification
                .when()
                .patch(ApiEndpoints.USER_PROFILE)
                .then();
    }
}