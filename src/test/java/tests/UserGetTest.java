package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequest;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Tests for getting user details")
public class UserGetTest extends BaseTestCase {

    private final ApiCoreRequest apiCoreRequest = new ApiCoreRequest();

    @Test
    @DisplayName("Getting user details without auth(negative)")
    @Description("User trying to get user details without token and cookie")
    @Step("Starting test testGetUserDataNotAuth")
    @Severity(value = SeverityLevel.BLOCKER)
    public void testGetUserDataNotAuth() {
        Response responseUserData = apiCoreRequest
                .makeGetRequestWithoutTokenAndCookie("https://playground.learnqa.ru/api/user/2");

        Assertions.assertJsonHasField(responseUserData, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }

    @Test
    @DisplayName("Getting user details with auth(positive)")
    @Description("User trying to get user details with token and cookie")
    @Step("Starting test testGetUserDetailsAuthAsSameUser")
    @Severity(value = SeverityLevel.BLOCKER)
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        String header = this.getHeader(responseCreateAuth, "x-csrf-token");
        String cookie = this.getCookie(responseCreateAuth, "auth_sid");

        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/2",
                        header,
                        cookie);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Test
    @DisplayName("Getting user details from another user(negative)")
    @Description("User trying to get user details with token and cookie from another user")
    @Step("Starting test testGetUserDetailsAuthAsAnotherUser")
    @Severity(value = SeverityLevel.BLOCKER)
    public void testGetUserDetailsAuthAsAnotherUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        String header = this.getHeader(responseCreateAuth, "x-csrf-token");
        String cookie = this.getCookie(responseCreateAuth, "auth_sid");

        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/1",
                        header,
                        cookie);

        Assertions.assertJsonHasField(responseUserData, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }
}
