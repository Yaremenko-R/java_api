import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        passwords.add("admin");
        passwords.add("qwerty123");
        passwords.add("1234567890");
        passwords.add("1q2w3e4r");
        passwords.add("master");
        passwords.add("666666");
        passwords.add("photoshop");
        passwords.add("1qaz2wsx");
        passwords.add("qwertyuiop");
        passwords.add("ashley");
        passwords.add("mustang");
        passwords.add("121212");
        passwords.add("starwars");
        passwords.add("654321");
        passwords.add("bailey");
        passwords.add("access");
        passwords.add("flower");
        passwords.add("555555");
        passwords.add("passw0rd");
        passwords.add("shadow");
        passwords.add("lovely");
        passwords.add("7777777");
        passwords.add("michael");
        passwords.add("!@#$%^&*");
        passwords.add("jesus");
        passwords.add("password1");
        passwords.add("superman");
        passwords.add("hello");
        passwords.add("charlie");
        passwords.add("888888");
        passwords.add("696969");
        passwords.add("freedom");
        passwords.add("aa123456");
        passwords.add("qazwsx");
        passwords.add("ninja");
        passwords.add("azerty");
        passwords.add("loveme");
        passwords.add("whatever");
        passwords.add("donald");
        passwords.add("batman");
        passwords.add("zaq1zaq1");
        passwords.add("Football");
        passwords.add("000000");
        passwords.add("123qwe");

        for (String password : passwords) {

            Response responseForGetAuthCookie = RestAssured
                    .given()
                    .queryParam("login", "super_admin")
                    .queryParam("password", password)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            System.out.println(password);
            String responseCookie = responseForGetAuthCookie.getCookie("auth_cookie");
            System.out.println(responseCookie);

            Response responseForCheckAuthCookie = RestAssured
                    .given()
                    .cookie(responseCookie)
                    .when()
                    .get("https://playground.learnqa.ru/api/check_auth_cookie")
                    .andReturn();

            responseForCheckAuthCookie.print();
            System.out.println("_____________________");

        }
    }
}
