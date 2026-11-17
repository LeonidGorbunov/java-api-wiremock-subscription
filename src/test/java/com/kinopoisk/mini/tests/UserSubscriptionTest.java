package com.kinopoisk.mini.tests;

import com.kinopoisk.mini.constants.TestData;
import com.kinopoisk.mini.models.request.LoginRequest;
import com.kinopoisk.mini.models.request.SubscriptionRequest;
import com.kinopoisk.mini.models.response.ErrorCode;
import com.kinopoisk.mini.models.response.LoginResponse;
import com.kinopoisk.mini.models.response.SubscriptionResponse;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import com.kinopoisk.mini.models.request.SubscriptionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Автоматизация с WireMock. Медиа-сервис Кинопоиск.Мини")
@Feature("Управление подпиской пользователя")
public class UserSubscriptionTest extends BaseTest {

    private String token;
    private LoginRequest loginRequest;
    private SubscriptionRequest subscriptionRequestForNegativeTests;

    @BeforeEach
    void prepareTestData() {
        loginRequest = LoginRequest.builder()
                .email(TestData.VALID_EMAIL)
                .password(TestData.VALID_PASSWORD)
                .build();

        token = loginClient.actionUserLogin(loginRequest)
                .extract()
                .as(LoginResponse.class)
                .getToken();

        subscriptionRequestForNegativeTests = SubscriptionRequest.builder()
                .userId(TestData.VALID_USER_ID)
                .subscriptionType(SubscriptionType.PREMIUM)
                .build();
    }

    @ParameterizedTest(name = "Тариф: {0}")
    @EnumSource(SubscriptionType.class)
    @Story("1. Позитивные сценарии")
    @DisplayName("Параметризованный тест успешного обновления подписки.")
    @Description("Тест проверяет позитивные сценарии обновления всех видов подписок. Эндпоинт PATCH /api/v1/users/profile, " +
            "полная проверка контракта ответа.")
    void subscriptionSuccessfulUpdateParametrizedTest(SubscriptionType subscriptionType) {

        var subscriptionRequest = SubscriptionRequest.builder()
                .userId(TestData.VALID_USER_ID)
                .subscriptionType(subscriptionType)
                .build();

        var response = profileClient.updateSubscription(subscriptionRequest, token);

        var subscriptionResponse = Allure.step("Проверка статус-кода и полей \"_userId\", \"status\", \"updatedAt\"", () ->
                response
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .contentType(ContentType.JSON)
                        .body("_userId", describedAs("Поле \"_userId\" не совпадает с ожидаемым!",
                                equalTo(subscriptionRequest.getUserId())))
                        .body("status", describedAs("Поле \"status\" не совпадает с ожидаемым!",
                                equalTo(TestData.STATUS_ACTIVE)))
                        .body("updatedAt", describedAs("Поле \"updatedAt\" не совпадает с ожидаемым!",
                                equalTo(TestData.DEFAULT_TIMESTAMP)))
                        .extract().as(SubscriptionResponse.class)
        );

        assertAll(
                () -> Allure.step("Проверка контракта ответа, поле \"profile.email\"",
                        () -> assertEquals(loginRequest.getEmail(), subscriptionResponse.getProfile().getEmail(),
                                "Поле \"profile.email\" не совпадает с ожидаемым!")),

                () -> Allure.step("Проверка контракта ответа, поле \"profile._username\"",
                        () -> assertEquals(TestData.VALID_USERNAME, subscriptionResponse.getProfile().getUsername(),
                                "Поле \"profile._username\" не совпадает с ожидаемым!")),

                () -> Allure.step("Проверка контракта ответа, поле \"billing.features\"",
                        () -> assertEquals(subscriptionRequest.getSubscriptionType().getFeatures(), subscriptionResponse.getBilling().getFeatures(),
                                "Поле \"billing.features\" не совпадает с ожидаемым!")),

                () -> Allure.step("Проверка контракта ответа, поле \"billing.price\"",
                        () -> assertEquals(subscriptionRequest.getSubscriptionType().getPrice(), subscriptionResponse.getBilling().getPrice(),
                                "Поле \"billing.price\" не совпадает с ожидаемым!")),

                () -> Allure.step("Проверка контракта ответа, поле \"billing.currency\"",
                        () -> assertEquals(TestData.CURRENCY_USD, subscriptionResponse.getBilling().getCurrency(),
                                "Поле \"billing.currency\" не совпадает с ожидаемым!"))
        );
    }

    static Stream<String> provideInvalidTokens() {
        return Stream.of(null, "", TestData.INVALID_TOKEN);
    }

    @ParameterizedTest(name = "Токен: {0}")
    @MethodSource("provideInvalidTokens")
    @Story("2. Негативные сценарии")
    @DisplayName("Параметризованный негативный тест обновления подписки при передаче невалидного токена.")
    @Description("Тест проверяет негативные сценарии обновления подписки при передаче невалидного токена или его отсутствии в запросе. " +
            "Эндпоинт PATCH /api/v1/users/profile, полная проверка контракта ответа.")
    void subscriptionWithInvalidTokenShouldReturn401ParametrizedTest(String invalidOrNullToken) {

        var response = profileClient.updateSubscription(subscriptionRequestForNegativeTests, invalidOrNullToken);

        Allure.step("Проверка статус-кода и полного контракта ответа", () -> {
            response
                    .assertThat()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .contentType(ContentType.JSON)
                    .body("error_code", describedAs("Поле \"error_code\" не совпадает с ожидаемым!",
                            equalTo(ErrorCode.UNAUTHORIZED.name())))
                    .body("message", describedAs("Поле \"message\" не совпадает с ожидаемым!",
                            equalTo("Invalid or missing authorization token")))
                    .body("timestamp", describedAs("Поле \"timestamp\" не совпадает с ожидаемым!",
                            equalTo(TestData.DEFAULT_TIMESTAMP)));
        });
    }

    @Test
    @Story("2. Негативные сценарии")
    @DisplayName("Негативный тест обновления подписки для несуществующего пользователя: " + TestData.UNKNOWN_USER_ID)
    @Description("Тест проверяет негативный сценарий обновления подписки при передаче невалидного ID пользователя " +
            "(\"_userId\":\"" + TestData.UNKNOWN_USER_ID + "\"). Эндпоинт PATCH /api/v1/users/profile, полная проверка контракта ответа.")
    void subscriptionWithUnknownUserIdShouldReturn404Test() {

        subscriptionRequestForNegativeTests.setUserId(TestData.UNKNOWN_USER_ID);

        var response = profileClient.updateSubscription(subscriptionRequestForNegativeTests, token);

        Allure.step("Проверка статус-кода и полного контракта ответа", () -> {
            response
                    .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .contentType(ContentType.JSON)
                    .body("error_code", describedAs("Поле \"error_code\" не совпадает с ожидаемым!",
                            equalTo(ErrorCode.USER_NOT_FOUND.name())))
                    .body("message", describedAs("Поле \"message\" не совпадает с ожидаемым!",
                            equalTo("Invalid '_userId' field, user not found")))
                    .body("timestamp", describedAs("Поле \"timestamp\" не совпадает с ожидаемым!",
                            equalTo(TestData.DEFAULT_TIMESTAMP)));
        });
    }

    @Test
    @Story("2. Негативные сценарии")
    @DisplayName("Негативный тест обновления подписки без её указания - при отсутствии поля \"subscriptionType\" в запросе")
    @Description("Тест проверяет негативный сценарий обновления подписки при отправке запроса без поля \"subscriptionType\"." +
            " Эндпоинт PATCH /api/v1/users/profile, полная проверка контракта ответа.")
    void subscriptionWithMissingTypeShouldReturn400Test() {

        subscriptionRequestForNegativeTests.setSubscriptionType(null);

        var response = profileClient.updateSubscription(subscriptionRequestForNegativeTests, token);

        Allure.step("Проверка статус-кода и полного контракта ответа", () -> {
            response
                    .assertThat()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .contentType(ContentType.JSON)
                    .body("error_code", describedAs("Поле \"error_code\" не совпадает с ожидаемым!",
                            equalTo(ErrorCode.VALIDATION_ERROR.name())))
                    .body("message", describedAs("Поле \"message\" не совпадает с ожидаемым!",
                            equalTo("Missing 'subscriptionType' field")))
                    .body("timestamp", describedAs("Поле \"timestamp\" не совпадает с ожидаемым!",
                            equalTo(TestData.DEFAULT_TIMESTAMP)));
        });
    }
}