import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    public void testGetHeader() {

        Response responseGetHeader = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        String responseHeader = responseGetHeader.getHeader("x-secret-homework-header");
        assertEquals("Some secret value", responseHeader, "Wrong header");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0",
            "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"})
    public void testUserAgent(String userAgent) {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("User-Agent", userAgent);

        JsonPath response = RestAssured
                .given()
                .headers(queryParams)
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check")
                .jsonPath();

        System.out.println("-------------- User-Agent --------------");
        String responsePlatform = response.getString("platform");
        String responseBrowser = response.getString("browser");
        String responseDevice = response.getString("device");
        response.prettyPrint();
        if (userAgent.contentEquals("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")) {
            String expectedPlatform = "Mobile";
            String expectedBrowser = "No";
            String expectedDevice = "Android";
            assertEquals(expectedPlatform, responsePlatform, "Platform in response: " + responsePlatform + "doesn't match with expected: " + expectedPlatform);
            assertEquals(expectedBrowser, responseBrowser, "Browser in response: " + responseBrowser + "doesn't match with expected: " + expectedBrowser);
            assertEquals(expectedDevice, responseDevice, "Device in response: " + responseDevice + "doesn't match with expected: " + expectedDevice);
        } else if (userAgent.contentEquals("Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1")) {
            String expectedPlatform = "Mobile";
            String expectedBrowser = "Chrome";
            String expectedDevice = "iOS";
            assertEquals(expectedPlatform, responsePlatform, "Platform in response: " + responsePlatform + "doesn't match with expected: " + expectedPlatform);
            assertEquals(expectedBrowser, responseBrowser, "Browser in response: " + responseBrowser + "doesn't match with expected: " + expectedBrowser);
            assertEquals(expectedDevice, responseDevice, "Device in response: " + responseDevice + "doesn't match with expected: " + expectedDevice);
        } else if (userAgent.contentEquals("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")) {
            String expectedPlatform = "Googlebot";
            String expectedBrowser = "Unknown";
            String expectedDevice = "Unknown";
            assertEquals(expectedPlatform, responsePlatform, "Platform in response: " + responsePlatform + "doesn't match with expected: " + expectedPlatform);
            assertEquals(expectedBrowser, responseBrowser, "Browser in response: " + responseBrowser + "doesn't match with expected: " + expectedBrowser);
            assertEquals(expectedDevice, responseDevice, "Device in response: " + responseDevice + "doesn't match with expected: " + expectedDevice);
        } else if (userAgent.contentEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0")) {
            String expectedPlatform = "Web";
            String expectedBrowser = "Chrome";
            String expectedDevice = "No";
            assertEquals(expectedPlatform, responsePlatform, "Platform in response: " + responsePlatform + "doesn't match with expected: " + expectedPlatform);
            assertEquals(expectedBrowser, responseBrowser, "Browser in response: " + responseBrowser + "doesn't match with expected: " + expectedBrowser);
            assertEquals(expectedDevice, responseDevice, "Device in response: " + responseDevice + "doesn't match with expected: " + expectedDevice);
        } else if (userAgent.contentEquals("Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")) {
            String expectedPlatform = "Mobile";
            String expectedBrowser = "No";
            String expectedDevice = "iPhone";
            assertEquals(expectedPlatform, responsePlatform, "Platform in response: " + responsePlatform + "doesn't match with expected: " + expectedPlatform);
            assertEquals(expectedBrowser, responseBrowser, "Browser in response: " + responseBrowser + "doesn't match with expected: " + expectedBrowser);
            assertEquals(expectedDevice, responseDevice, "Device in response: " + responseDevice + "doesn't match with expected: " + expectedDevice);
        } else {
            throw new IllegalArgumentException("Unknown User-Agent: " + userAgent);
        }
    }
}
