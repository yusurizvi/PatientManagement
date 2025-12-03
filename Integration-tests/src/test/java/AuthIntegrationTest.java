import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOkWithToken() {
        String payload = """
                {
                    "email" : "testuser@test.com",
                    "password" : "password123"
                }
                """;
        Response resp = given()
                .contentType("application/json")
                .body(payload)//-----set up step 1 of test
                .when().post("/auth/login")//act step 2 the actual function
                .then()
                .statusCode(200)//--asset step 3 to confirm the output
                .body("token", notNullValue())
                .extract().response();

        System.out.println("generated token :"+resp.jsonPath().getString("token"));
    }


    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        String payload = """
                {
                    "email" : "InvalidTest23@test.com",
                    "password" : "password123"
                }
                """;
        given()
                .contentType("application/json")
                .body(payload)//-----set up step 1 of test
                .when().post("/auth/login")//act step 2 the actual function
                .then()
                .statusCode(401);//--asset step 3 to confirm the output


//        System.out.println("generated token :"+resp.jsonPath().getString("token"));
    }
}
