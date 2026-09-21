package ru.mealcard;

import io.restassured.http.ContentType;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mealcard.service.dto.GenerateRequestDTO;
import ru.mealcard.service.format.dto.EnrollDTO;
import ru.mealcard.utils.generate_models.TypeOperation;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;
import static org.hamcrest.Matchers.*;

public class GenerateTest extends IntegrationTest {

    Faker faker = new Faker();
    GenerateRequestDTO validRequest;


    @BeforeEach
    void setUpRequest() {
        EnrollDTO card1 = new EnrollDTO();
        card1.setFio(faker.name().fullName());
        card1.setAccount("1000" + faker.number().digits(12));
        card1.setType(TypeOperation.DR);
        card1.setSumm(faker.number().numberBetween(100, 10000));

        EnrollDTO card2 = new EnrollDTO();
        card2.setFio(faker.name().fullName());
        card2.setAccount("1000" + faker.number().digits(12));
        card2.setType(TypeOperation.ZR);
        card2.setSumm(faker.number().numberBetween(100, 10000));

        validRequest = new GenerateRequestDTO();
        validRequest.setBankCode("001");
        validRequest.setBranchCode("032");
        validRequest.setNameSystem("GLAER");
        validRequest.setProcType("IMMEDIATE");
        validRequest.setCards(List.of(card1, card2));
    }

    @Test
    void testValidImmediateRequest() {
        ZonedDateTime futureTime = ZonedDateTime.now().plusHours(2);
        validRequest.setProcType("IN-TIME");
        validRequest.setProcessAt(futureTime.toString());

        String filename = given()
                .contentType(ContentType.JSON)
                .body(validRequest)

                .when()
                .post("/generate")

                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("filename", notNullValue())
                .body("filename", matchesPattern("Z001032\\.GLAER_ENROLL001032\\d+\\.\\d{3}"))

                .extract()
                .path("filename");

        Path file = outputDir.resolve(filename);
        assertThat(file).exists();

        List<String> lines = linesOf(file);
        assertThat(lines).hasSize(4);
        assertThat(lines.get(0)).hasSize(42).startsWith("H ");
        assertThat(lines.get(1)).hasSize(152);
        assertThat(lines.get(2)).hasSize(152);
        assertThat(lines.get(3)).hasSize(20).startsWith("T").endsWith("2");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"bankCode\":\"12ab\",\"branchCode\":\"032\",\"nameSystem\":\"GLAER\",\"procType\":\"IMMEDIATE\",\"cards\":[{\"fio\":\"A\",\"account\":\"1000401000050000\",\"type\":\"DR\",\"summ\":1}]}",
            "{\"bankCode\":\"001\",\"branchCode\":\"032\",\"nameSystem\":\"GLAER\",\"procType\":\"IMMEDIATE\",\"cards\":[]}",
            "{\"bankCode\":\"001\",\"branchCode\":\"032\",\"nameSystem\":\"GLAER\",\"procType\":\"IN-TIME\",\"cards\":[{\"fio\":\"A\",\"account\":\"1000401000050000\",\"type\":\"DR\",\"summ\":1}]}",
            "{broken json"
    })
    void testInvalidBodyRequest(String body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/generate")
                .then()
                .statusCode(400)
                .body("status", equalTo("ERROR"));
    }

    @Test
    void testWrongMethod() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequest)

                .when()
                .get("/generate")

                .then()
                .statusCode(405);
    }

}
