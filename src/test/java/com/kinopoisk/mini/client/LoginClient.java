package com.kinopoisk.mini.client;

import com.kinopoisk.mini.constants.ApiEndpoints;
import com.kinopoisk.mini.models.request.LoginRequest;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.given;

public class LoginClient extends BaseClient {

    @Step("Действие: авторизация пользователя {loginRequest.email}, POST /api/signin")
    public ValidatableResponse actionUserLogin(LoginRequest loginRequest) {

        return given(BASE_SPEC)
                .body(loginRequest)
                .when()
                .post(ApiEndpoints.SIGN_IN)
                .then();
    }
}