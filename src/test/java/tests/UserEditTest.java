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

public class UserEditTest extends BaseTestCase {

    private final ApiCoreRequest apiCoreRequest = new ApiCoreRequest();

    @Test
    public void testEditJustCreatedUser() {
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

        //EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser = apiCoreRequest
                .makePutRequestFlashRoal("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie,
                        editData);

        //GET
        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    public void testEditUserWithoutAuth() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequest
                .makePutRequestWithoutTokenAndCookie("https://playground.learnqa.ru/api/user/" + userId, editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "Auth token not supplied");
    }

    @Test
    public void testEditUserByAnotherUser() {
        //GENERATE FIRST USER
        Map<String, String> userData1 = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth1 = RestAssured
                .given()
                .body(userData1)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId1 = responseCreateAuth1.getString("id");
        String currentUserName1 = responseCreateAuth1.getString("firstname");

        //GENERATE SECOND USER
        Map<String, String> userData2 = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth2 = RestAssured
                .given()
                .body(userData2)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

        String userId2 = responseCreateAuth2.getString("id");
        String currentUserName2 = responseCreateAuth1.getString("firstname");

        //LOGIN BY FIRST USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData1.get("email"));
        authData.put("password", userData1.get("password"));

        Response responseGetAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        //EDIT SECOND USER BY FIRST USER
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser = apiCoreRequest
                .makePutRequestFlashRoal("https://playground.learnqa.ru/api/user/" + userId2,
                        header,
                        cookie,
                        editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "You are not authorized");

        //GET FIRST USER DATA AFTER EDIT
        Response responseUserData1 = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId1,
                        header,
                        cookie);

        Assertions.assertJsonByName(responseUserData1, "firstName", currentUserName1);
        Assertions.assertJsonHasField(responseUserData1, "username");
        String[] expectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData1, expectedFields);


        //GET SECOND USER FIRSTNAME AFTER EDIT
        Response responseUserData2 = apiCoreRequest
                .makeGetRequestWithoutTokenAndCookie("https://playground.learnqa.ru/api/user/" + userId2);

        Assertions.assertJsonByName(responseUserData2, "firstName", currentUserName2);
        Assertions.assertJsonHasField(responseUserData2, "username");
        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasNotFields(responseUserData2, unexpectedFields);
    }

    @Test
    public void testEditUserEmailWithAuth() {
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

        //EDIT
        String newEmail = "i.v.got.the.power";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser = apiCoreRequest
                .makePutRequestFlashRoal("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie,
                        editData);

        //GET
        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "Invalid email format");
    }

    @Test
    public void testEditUserFirstName() {
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

        //EDIT
        String newName = "n";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser = apiCoreRequest
                .makePutRequestFlashRoal("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie,
                        editData);

        //GET
        Response responseUserData = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "{\"error\":\"Too short value for field firstName\"}");
    }
}
