import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreatingCourierTest {

    private CreatingCourier courier;
    private boolean runAfter;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        courier = new CreatingCourier(RandomStringUtils.randomAlphanumeric(10), "6666", "Новый");
    }
    @After
    public void deleteCourier() {
        if (runAfter) {
            var login = new LoginCourier(courier.getLogin(), courier.getPassword());
            int id = stepLoginCourier(login);
            stepDeleteCourier(id);
        }
    }
    @Test
    @DisplayName("Создание курьера, проверка статус кода и тело ответа")
    public void createCourier() {
        runAfter = true;
        ValidatableResponse response = stepCreateCourier(courier);
        response.assertThat()
                .statusCode(201)
                .and()
                .body("ok", equalTo(true));
    }
    @Test
    @DisplayName("Создание курьеров с одинаковыми логинами")
    public void creatingCourierWithExistingLogin() {
        runAfter = true;
        stepCreateCourier(courier);
        var courierNew = new CreatingCourier(courier.getLogin(), "4444", "Имя");
        ValidatableResponse response = stepCreateCourier(courierNew);
        response.assertThat()
                .statusCode(409)
                .body("message", notNullValue());
    }
    @Test
    @DisplayName("Создание курьера без поля логин")
    public void сreatingCourierWithoutLogin() {
        runAfter = false;
        courier.setLogin(null);
        ValidatableResponse response = stepCreateCourier(courier);
        response.assertThat()
                .statusCode(400);
    }
    @Test
    @DisplayName("Создание курьера без поля пароль")
    public void сreatingCourierWithoutPassword() {
        runAfter = false;
        courier.setPassword(null);
        ValidatableResponse response = stepCreateCourier(courier);
        response.assertThat()
                .statusCode(400);
    }
    @Test
    @DisplayName("Создание курьера только с обязательными полями")
    public void creatingCourierWithRequiredFields() {
        runAfter = true;
        courier.setFirstName(null);
        ValidatableResponse response = stepCreateCourier(courier);
        response.assertThat()
                .statusCode(201)
                .and()
                .body("ok", equalTo(true));
    }
    @Step("Создание курьера")
    public ValidatableResponse stepCreateCourier(CreatingCourier courier) {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then().log().all();
    }
    @Step("Авторизация под созданным курьером")
    public int stepLoginCourier(LoginCourier login) {
        int id = given().log().all()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/api/v1/courier/login")
                .then().log().all()
                .assertThat()
                .extract()
                .path("id");
        return id;
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
