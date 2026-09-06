package ru.mealcard.service;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mealcard.service.format.dto.EnrollDTO;
import ru.mealcard.service.dto.GenerateRequestDTO;
import ru.mealcard.service.dto.MockRequestDTO;
import ru.mealcard.exception.request.InvalidRequestException;
import ru.mealcard.utils.generate_models.TypeOperation;
import ru.mealcard.service.validator.RequestValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestValidatorTest {
    Faker faker = new Faker();
    RequestValidator validator = RequestValidator.getInstance();

    MockRequestDTO mockRequestDTO;
    GenerateRequestDTO generateRequestDTO;

    @BeforeEach
    public void setUpMock() {
        mockRequestDTO = new MockRequestDTO();

        mockRequestDTO.setBankCode(faker.number().digits(3));
        mockRequestDTO.setBranchCode(faker.number().digits(3));
        mockRequestDTO.setNameSystem("GLAER");
        mockRequestDTO.setRowCount(faker.number().numberBetween(1, 1000));
        mockRequestDTO.setFileCount(faker.number().numberBetween(1, 10));
    }

    @BeforeEach
    public void setUpGenerate() {
        generateRequestDTO = new GenerateRequestDTO();
        generateRequestDTO.setBankCode(faker.number().digits(3));
        generateRequestDTO.setBranchCode(faker.number().digits(3));
        generateRequestDTO.setNameSystem("GLAER");
        generateRequestDTO.setProcType("IMMEDIATE");

        generateRequestDTO.setCards(List.of(new EnrollDTO(
                faker.name().fullName(),
                faker.number().digits(16),
                TypeOperation.DR,
                faker.number().numberBetween(1, 100_000)
        )));
    }

    @Test
    void testValidRequest() {
        assertDoesNotThrow(() -> validator.validateMock(mockRequestDTO));
        assertDoesNotThrow(() -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcd", "-1", "20000"})
    void testInvalidBankCode(String bankCode) {
        mockRequestDTO.setBankCode(bankCode);
        assertThrows(InvalidRequestException.class, () -> validator.validateMock(mockRequestDTO));

        generateRequestDTO.setBankCode(bankCode);
        assertThrows(InvalidRequestException.class, () -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "9000", "aa"})
    void testInvalidBranchCode(String branchCode) {
        mockRequestDTO.setBranchCode(branchCode);
        assertThrows(InvalidRequestException.class, () -> validator.validateMock(mockRequestDTO));

        generateRequestDTO.setBranchCode(branchCode);
        assertThrows(InvalidRequestException.class, () -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testInvalidRowCountMock(int rowCount) {
        mockRequestDTO.setRowCount(rowCount);
        assertThrows(InvalidRequestException.class, () -> validator.validateMock(mockRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, 0})
    void testInvalidFileCountMock(int fileCount) {
        mockRequestDTO.setFileCount(fileCount);
        assertThrows(InvalidRequestException.class, () -> validator.validateMock(mockRequestDTO));
    }

    @Test
    void testEmptyCards() {
        generateRequestDTO.setCards(List.of());
        assertThrows(InvalidRequestException.class, () -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678901234567", "10004010000500ab"})
    void testInvalidAccount(String account) {
        generateRequestDTO.setCards(List.of(new EnrollDTO(
                faker.name().fullName(),
                account,
                TypeOperation.DR,
                faker.number().numberBetween(1, 100_000))));

        assertThrows(InvalidRequestException.class, () -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void testNegativeSum(int summ) {
        generateRequestDTO.setCards(List.of(new EnrollDTO(
                faker.name().fullName(),
                faker.number().digits(16),
                TypeOperation.DR,
                summ
        )));

        assertThrows(InvalidRequestException.class, () -> validator.validateGenerate(generateRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLAER", "AES_123", "a1"})
    void testValidAesPasses(String aes) {
        mockRequestDTO.setNameSystem(aes);
        assertDoesNotThrow(() -> validator.validateMock(mockRequestDTO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLA-ER", "TOOLONGNAME123", "рус!"})
    void testInvalidAesThrows(String aes) {
        mockRequestDTO.setNameSystem(aes);
        assertThrows(InvalidRequestException.class, () -> validator.validateMock(mockRequestDTO));
    }
}

