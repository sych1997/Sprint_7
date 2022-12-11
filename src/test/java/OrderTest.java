import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class OrderTest {
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metroStation;
    private final String phone;
    private final int rentTime;
    private final String deliveryDate;
    private final String comment;
    private final List<String> color;

    public OrderTest(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, List<String> color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }
    @Parameterized.Parameters(name = "firstName = {0}, lastName = {1}, address = {2}, metroStation = {3}, phone = {4}, rentTime = {5}, " +
            "deliveryDate = {6}, comment = {7}, color = {8}")
    public static Object[][] getCities() {
        return new Object[][]{
                {"Виктор", "Иванов", "Ул.Пушкина, д3", "Жулебино", "89996664455", 2, "2022-11-26", "", List.of("BLACK ")},
                {"Наталья", "Куракина", "Ул.Молодежная", "лермонтово", "86665552211", 3, "2022-11-11", "Оставить у двери", List.of("GREY ")},
                {"Виктория", "Сычева", "Ул.Искандера", "Черкизовская", "81119992233", 6, "2022-12-01", "В первой половине дня", List.of("BLACK ", "GREY ")},
                {"Руслан", "Белый", "Москва", "Кузьминки", "81119231122", 7, "2022-12-12", "Безнал", null},
        };
    }
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }
    @Test
    @DisplayName("Создание заказа")
    public void order() {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
        given().log().all()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("track", notNullValue());
    }
}
