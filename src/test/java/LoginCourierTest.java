import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class LoginCourierTest {
    private CreatingCourier courier;
    private int courierId;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        courier = new CreatingCourier(RandomStringUtils.randomAlphanumeric(10), "6666", "Новый");
        stepCreateCourier(courier);
    }
    @After
    public void deleteCourier() {
        courierId = stepLoginCourier(new LoginCourier(courier.getLogin(), courier.getPassword())).extract()
                .path("id");
        stepDeleteCourier(courierId);

    }
    @Test
    @DisplayName("Авторизация курьера")
    public void loginCourier() {
        var login = new LoginCourier(courier.getLogin(), courier.getPassword());
        ValidatableResponse response = stepLoginCourier(login);
        response.assertThat()
                .statusCode(200)
                .body("id", notNullValue());
    }
    @Test
    @DisplayName("Авторизация с не правильным паролем")
    public void ivalidPassword() {
        var login = new LoginCourier(courier.getLogin(), courier.getPassword() + "123");
        ValidatableResponse response = stepLoginCourier(login);
        response.assertThat()
                .statusCode(404)
                .body("message", notNullValue());
    }
    @Test
    @DisplayName("Авторизация с не правильным логином")
    public void ivalidLogin() {
        var login = new LoginCourier(courier.getLogin() + "123", courier.getPassword() + "123");
        ValidatableResponse response = stepLoginCourier(login);
        response.assertThat()
                .statusCode(404)
                .body("message", notNullValue());
    }
    @Test
    @DisplayName("Авторизация без обязательного поля пароль")
    public void loginWithoutFieldPassword() {
        var login = new LoginCourier(courier.getLogin(), null);
        ValidatableResponse response = stepLoginCourier(login);
        response.assertThat()
                .statusCode(400)
                .body("message", notNullValue());
    }
    @Test
    @DisplayName("Авторизация без обязательного поля логин")
    public void loginWithoutFieldLogin() {
        var login = new LoginCourier(null, courier.getPassword());
        ValidatableResponse response = stepLoginCourier(login);
        response.assertThat()
                .statusCode(400)
                .body("message", notNullValue());
    }
    @Step("Авторизация под созданным курьером")
    public ValidatableResponse stepLoginCourier(LoginCourier login) {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/api/v1/courier/login")
                .then().log().all();
    }
    @Step("Создание курьера")
    public void stepCreateCourier(CreatingCourier courier) {
        given().log().all()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then().log().all();
    }
    @Step("Удаление курьера")
    public void stepDeleteCourier(int id) {
        String json = "{\"id\": " + String.valueOf(id) + "}";
        given().log().all()
                .body(json)
                .when()
                .delete("/api/v1/courier/{id}", id)
                .then().log().all()
                .assertThat()
                .statusCode(200);
    }
}
