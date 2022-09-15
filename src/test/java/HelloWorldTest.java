import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
