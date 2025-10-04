import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

public class LoginTests {

    private static String token;
    private static final String BASE_URL = "https://demoqa.com";
    private static final String EXISTING_USER = "w2e3r4t51982";
    private static final String EXISTING_PASSWORD = "POiu1234!";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        token = given()
                .contentType(JSON)
                .body("{ \"userName\": \"" + EXISTING_USER + "\", \"password\": \"" + EXISTING_PASSWORD + "\" }")
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .statusCode(200)
                .body("status", is("Success"),
                        "result", is("User authorized successfully."))
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("Авторизация с некорректным паролем")
    public void unsuccessfullLoginTest() {
        given()
                .contentType(JSON)
                .body("{ \"userName\": \"" + EXISTING_USER + "\", \"password\": \"1234567\" }")
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("status", is("Failed"),
                        "result", is("User authorization failed."));
    }

    @Test
    @DisplayName("Авторизация с корректными данными")
    public void successfullLoginTest() {
        given()
                .contentType(JSON)
                .body("{ \"userName\": \"" + EXISTING_USER + "\", \"password\": \"" + EXISTING_PASSWORD + "\" }")
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("status", is("Success"),
                        "result", is("User authorized successfully."));
    }

    @Test
    @DisplayName("Проверка авторизации пользователя с некорректными данными")
    public void checkAuthorizationWithWrongUserTest() {
        given()
                .header("Authorization", "Bearer " + token)
                .pathParam("username", "wert")
                .when()
                .get("/Account/v1/User/{username}")
                .then()
                .log().status()
                .log().body()
                .statusCode(401)
                .body("code", is("1207"), "message", is("User not found!"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем")
    public void createUserWithEmptyUsernameTest() {
        given()
                .contentType(JSON)
                .body("{ \"userName\": \"\", \"password\": \"Test123!\" }")
                .when()
                .post("/Account/v1/User")
                .then()
                .log().status()
                .log().body()
                .statusCode(400)
                .body("message", containsString("UserName and Password required"));
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    public void deleteNonExistentUserTest() {
        given()
                .header("Authorization", "Bearer " + token)
                .pathParam("username", "userDoesNotExist")
                .when()
                .delete("/Account/v1/User/{username}")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("message", containsString("User Id not correct!"));
    }

    @Test
    @DisplayName("Получение списка книг")
    public void getBooksTest() {
        given()
                .contentType(JSON)
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("books", not(empty()));
    }

    @Test
    @DisplayName("Удаление временного пользователя (DELETE)")
    public void deleteTempUserTest() {
        String tempUser = "tempUser_" + System.currentTimeMillis();
        String tempPassword = "TempPass123!";


        given()
                .contentType(JSON)
                .body("{ \"userName\": \"" + tempUser + "\", \"password\": \"" + tempPassword + "\" }")
                .when()
                .post("/Account/v1/User")
                .then()
                .log().status()
                .log().body()
                .statusCode(201);


        given()
                .header("Authorization", "Bearer " + token)
                .pathParam("username", tempUser)
                .when()
                .delete("/Account/v1/User/{username}")
                .then()
                .log().status()
                .log().body()
                .statusCode(204);
    }

    @Test
    @DisplayName("Удаление книги.")
    public void deleteBookTest() {

        String isbn = "9781593277574";
        String requestBody = "{\n" +
                "  \"isbn\": \"" + isbn + "\",\n" +
                "  \"userId\": \"" + EXISTING_USER + "\"\n" +
                "}";

        given()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .delete("/BookStore/v1/Book")
                .then()
                .log().status()
                .log().body()
                .statusCode(401);
    }
}
