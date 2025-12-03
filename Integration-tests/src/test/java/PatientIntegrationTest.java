import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnPatientsOnValidToken(){
        String payload = """
                {
                    "email" : "testuser@test.com",
                    "password" : "password123"
                }
                """;
        String token =given()
                .contentType("application/json")
                .body(payload)//-----set up step 1 of test
                .when().post("/auth/login")//act step 2 the actual function
                .then()
                .statusCode(200)//--asset step 3 to confirm the output
                .extract()
                .jsonPath()
                .getString("token");

        given().header("Authorization", "Bearer " + token)
                .when().get("/auth/login")
                .then()
                .statusCode(200)
                .body("patients",  notNullValue());
    }

}
