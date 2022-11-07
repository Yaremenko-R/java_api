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

    @Test
    public void testDeleteUserByAnotherUser() {
        //GENERATE FIRST USER
        Map<String, String> userData1 = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth1 = RestAssured
                .given()
                .body(userData1)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId1 = responseCreateAuth1.getString("id");

        //GENERATE SECOND USER
        Map<String, String> userData2 = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth2 = RestAssured
                .given()
                .body(userData2)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId2 = responseCreateAuth2.getString("id");

        //LOGIN BY FIRST USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData1.get("email"));
        authData.put("password", userData1.get("password"));

        Response responseGetAuth1 = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        String header = this.getHeader(responseGetAuth1, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth1, "auth_sid");

        //DELETE SECOND USER BY FIRST USER
        Response responseDelete = apiCoreRequest
                .makeDeleteRequestWithTokenAndCookie("https://playground.learnqa.ru/api/user/" + userId2,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseDelete, 403);
        Assertions.assertResponseTextEquals(responseDelete, "You are not authorized");

        //GET USER DATA OF SECOND USER AFTER DELETE
        Response responseUserData2 = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId2,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseUserData2, 200);
        Assertions.assertJsonHasField(responseUserData2, "username");
        String[] unexpectedFields2 = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData2, unexpectedFields2);

        //GET USER DATA OF FIRST USER AFTER DELETE
        Response responseUserData1 = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId1,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseUserData1, 200);
        String[] expectedFields1 = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData1, expectedFields1);
    }
}
