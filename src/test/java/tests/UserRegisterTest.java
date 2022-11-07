package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequest;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Tests for register users")
public class UserRegisterTest extends BaseTestCase {
    private final ApiCoreRequest apiCoreRequest = new ApiCoreRequest();

    @Test
    @DisplayName("Creating user with existing email(negative)")
    @Description("Trying to create user with existing email")
    @Step("Starting test testCreateUserWithExistingEmail")
    @Severity(value = SeverityLevel.CRITICAL)
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    @DisplayName("Creating user(positive)")
    @Description("Trying to create new user")
    @Step("Starting test testCreateUserSuccessfully")
    @Severity(value = SeverityLevel.BLOCKER)
    public void testCreateUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @DisplayName("Creating user with incorrect format email(positive)")
    @Description("Trying to create new user with incorrect format email")
    @Step("Starting test testCreateUserWithIncorrectEmail")
    @Severity(value = SeverityLevel.BLOCKER)
    public void testCreateUserWithIncorrectEmail() {
        //EMail without @
        String email = "vinexample.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @Test
    @DisplayName("Creating user with absence one of the mandatory fields(negative)")
    @Description("Trying to create new user without one of the mandatory fields")
    @Step("Starting test testCreateUserWithoutOneParameter")
    @Severity(value = SeverityLevel.BLOCKER)
    @ParameterizedTest
    @ValueSource(strings = {"email", "username", "password", "firstName", "lastName"})
    void testCreateUserWithoutOneParameter(String condition) {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        if (condition.equals("username") || condition.equals("email") ||
                condition.equals("password") || condition.equals("firstName") ||
                condition.equals("lastName")) {
            userData.remove(condition);
        } else {
            throw new IllegalArgumentException("Condition value is unknown: " + condition);
        }

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The following required params are missed: " + condition);
    }

    @Test
    @DisplayName("Creating user with too short name(negative)")
    @Description("Trying to create new user with too short name")
    @Step("Starting test testCreateOneSymbolNameUser")
    @Severity(value = SeverityLevel.CRITICAL)
    void testCreateOneSymbolNameUser() {
        String username = "A";
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too short");
    }

    @Test
    @DisplayName("Creating user with too long name(negative)")
    @Description("Trying to create new user with too long name")
    @Step("Starting test testCreateVeryLongNameUser")
    @Severity(value = SeverityLevel.CRITICAL)
    void testCreateVeryLongNameUser() {
        String username = "qwertyuioplkjhgfdsazxcvbnmqwertyuiopasdfAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequest
                .makePostRequest("https://playground.learnqa.ru/api/user", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too long");
    }
}
