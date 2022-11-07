package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequest;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


class UserDeleteTest extends BaseTestCase {
    private final ApiCoreRequest apiCoreRequest = new ApiCoreRequest();

    @Test
    void testDeleteSuperUser() {
        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        int userId = this.getIntFromJson(responseGetAuth, "user_id");

        //DELETE
        Response responseDelete = apiCoreRequest
                .makeDeleteRequestWithTokenAndCookie("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        Assertions.assertResponseTextEquals(responseDelete,
                "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    void testDeleteUserWithAuth() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //DELETE
        Response responseDelete = apiCoreRequest
                .makeDeleteRequestWithTokenAndCookie("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        //GET
        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseUserData, 404);
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }
}
