import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelloWorldTest {

    @Test
    public void testRestAssured() {

        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        System.out.println((response).getString("messages[1].message"));
    }

    @Test
    public void testGetRedirectLocation() {

        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        String locationHeader = response.getHeader("Location");
        System.out.println(locationHeader);

    }

    @Test
    public void testLongRedirect() {
        int statusCode = 0;
        String url = "https://playground.learnqa.ru/api/long_redirect";

        while (statusCode != 200) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .get(url)
                    .andReturn();

            String locationHeader = response.getHeader("Location");
            statusCode = response.getStatusCode();
            System.out.println(locationHeader);
            System.out.println(statusCode);
            url = locationHeader;
        }
    }

    @Test
    public void testToken() throws InterruptedException {

        JsonPath response1 = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String token = response1.getString("token");
        int timeToReady = response1.getInt("seconds");
        System.out.println((token));
        System.out.println((timeToReady));

        JsonPath response2 = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String statusNotReady = response2.getString("status");
        assertEquals("Job is NOT ready", statusNotReady);
        System.out.println((statusNotReady));

        Thread.sleep(timeToReady * 1000L);

        JsonPath response3 = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String statusReady = response3.getString("status");
        assertEquals("Job is ready", statusReady);
        System.out.println((statusReady));

        String result = response3.getString("result");
        Assertions.assertFalse(result.isEmpty());
        System.out.println((result));
    }

    @Test
    public void testBruteForse() {

        List<String> passwords = new ArrayList<>();
        passwords.add("123456");
        passwords.add("12345678");
        passwords.add("123456789");
        passwords.add("password");
        passwords.add("qwerty");
        passwords.add("abc123");
        passwords.add("12345");
        passwords.add("football");
        passwords.add("1234567");
        passwords.add("monkey");
        passwords.add("letmein");
        passwords.add("dragon");
        passwords.add("baseball");
        passwords.add("1234");
        passwords.add("sunshine");
        passwords.add("iloveyou");
        passwords.add("trustno1");
        passwords.add("princess");
        passwords.add("adobe123");
        passwords.add("123123");
        passwords.add("welcome");
        passwords.add("login");

        for (String password : passwords) {

            Response responseForGetAuthCookie = RestAssured
                    .given()
                    .queryParam("login", "super_admin")
                    .queryParam("password", password)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String responseCookie = responseForGetAuthCookie.getCookie("auth_cookie");
            Map<String, String> cookies = new HashMap<>();
            cookies.put("auth_cookie", responseCookie);

            Response responseForCheckAuthCookie = RestAssured
                    .given()
                    .cookies(cookies)
                    .when()
                    .get("https://playground.learnqa.ru/api/check_auth_cookie")
                    .andReturn();

            if (responseForCheckAuthCookie.getBody().asString().contentEquals("You are authorized")) {
                System.out.println(password);
                responseForCheckAuthCookie.print();
            }
        }
    }

    @Test
    public void testCheckLength() {
        String text = "Tra lala lala lala";
        assertTrue(text.length() > 15, "The text should be greater than 15 chars, current length: " + text.length());
    }

    @Test
    public void testGetCookie() {

        Response responseGetCookie = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        String responseCookie = responseGetCookie.getCookie("HomeWork");
        assertEquals("hw_value", responseCookie, "Wrong cookie");
    }
}
