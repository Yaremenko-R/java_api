import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        System.out.println("Hello from Roman");
    }

    @Test
    public void testGetText() {
        Response response = RestAssured.
                get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        response.prettyPrint();
    }
}
