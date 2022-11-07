package tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequest;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Tests for editing user details")
public class UserEditTest extends BaseTestCase {

    private final ApiCoreRequest apiCoreRequest = new ApiCoreRequest();

    @Test
    @DisplayName("Editing user details with auth(positive)")
    @Description("User trying to edit the same user details with token and cookie")
    @Step("Starting test testEditJustCreatedUser")
    @Severity(value = SeverityLevel.BLOCKER)
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
    @DisplayName("Editing user details without auth(negative)")
    @Description("User trying to edit the same user details without token and cookie")
    @Step("Starting test testEditUserWithoutAuth")
    @Severity(value = SeverityLevel.BLOCKER)
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
    @DisplayName("Editing user details of another user without auth(negative)")
    @Description("User trying to edit another user details with token and cookie")
    @Step("Starting test testEditUserByAnotherUser")
    @Severity(value = SeverityLevel.BLOCKER)
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
        Map<String, String> authData1 = new HashMap<>();
        authData1.put("email", userData1.get("email"));
        authData1.put("password", userData1.get("password"));

        Response responseGetAuth1 = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData1);

        String header1 = this.getHeader(responseGetAuth1, "x-csrf-token");
        String cookie1 = this.getCookie(responseGetAuth1, "auth_sid");

        //EDIT SECOND USER BY FIRST USER
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequest
                .makePutRequestFlashRoal("https://playground.learnqa.ru/api/user/" + userId2,
                        header1,
                        cookie1,
                        editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 403);
        Assertions.assertResponseTextEquals(responseEditUser, "You are not authorized");

        //GET FIRST USER DATA AFTER EDIT
        Response responseUserData1 = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId1,
                        header1,
                        cookie1);

        Assertions.assertJsonByName(responseUserData1, "firstName", currentUserName1);
        String[] expectedFields1 = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData1, expectedFields1);

        //GET SECOND USER DATA AFTER EDIT

        //LOGIN BY SECOND USER
        Map<String, String> authData2 = new HashMap<>();
        authData2.put("email", userData2.get("email"));
        authData2.put("password", userData2.get("password"));

        Response responseGetAuth2 = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData2);

        String header2 = this.getHeader(responseGetAuth1, "x-csrf-token");
        String cookie2 = this.getCookie(responseGetAuth1, "auth_sid");

        //GET SECOND USER INFO
        Response responseUserData2 = apiCoreRequest
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId1,
                        header2,
                        cookie2);

        Assertions.assertJsonByName(responseUserData2, "firstName", newName);
        String[] expectedFields2 = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData1, expectedFields2);
    }

    @Test
    @DisplayName("Editing user details, changing email for incorrect(without @) with auth(negative)")
    @Description("User trying to edit user email for incorrect with token and cookie")
    @Step("Starting test testEditUserEmailWithAuth")
    @Severity(value = SeverityLevel.BLOCKER)
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
    @DisplayName("Editing user details, changing firstname to incorrect(1 symbol) with auth(negative)")
    @Description("User trying to edit user name for incorrect with token and cookie")
    @Step("Starting test testEditUserFirstName")
    @Severity(value = SeverityLevel.BLOCKER)
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
