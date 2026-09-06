package ru.mealcard.service.validator;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import ru.mealcard.Base;
import ru.mealcard.service.dto.RequestErrorDTO;
import ru.mealcard.service.format.dto.EnrollDTO;
import ru.mealcard.service.dto.GenerateRequestDTO;
import ru.mealcard.service.dto.MockRequestDTO;
import ru.mealcard.exception.request.InvalidRequestException;
import ru.mealcard.service.send.dto.SendRequestDTO;
import ru.mealcard.utils.generate_models.TypeProcedure;

import java.util.regex.Pattern;

public class RequestValidator extends Base {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\d{1,3}");
    private static final Pattern AES_PATTERN = Pattern.compile("[A-Za-z0-9_]{1,12}");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("[1-9]\\d{15}");

    @Getter
    private final static RequestValidator instance = new RequestValidator();

    private RequestValidator() {

    }


    public void validateMock(MockRequestDTO req) {
        if (req == null) {
            throw new InvalidRequestException("Request cannot be null");
        }

        validateCommonFields(req.getBankCode(), req.getBranchCode(), req.getNameSystem());

        if (req.getRowCount() <= 0) {
            throw new InvalidRequestException("rowCount must be >= 1");
        }
        if (req.getFileCount() <= 0) {
            throw new InvalidRequestException("fileCount must be >= 1");
        }
    }

    public void validateGenerate(GenerateRequestDTO req) {
        if (req == null) {
            throw new InvalidRequestException("Request cannot be null");
        }

        validateCommonFields(req.getBankCode(), req.getBranchCode(), req.getNameSystem());

        if (req.getCards() == null || req.getCards().isEmpty()) {
            throw new InvalidRequestException("cards cannot be empty");
        }
        req.getCards().forEach(this::validateCard);

        if (StringUtils.isBlank(req.getProcType())) {
            throw new InvalidRequestException("procType is required");
        }

        TypeProcedure proc = TypeProcedure.fromCode(req.getProcType());
        if (proc == TypeProcedure.IN_TIME && StringUtils.isBlank(req.getProcessAt())) {
            throw new InvalidRequestException("processAt is required for IN-TIME");
        }
    }

    public void validateError(RequestErrorDTO req) {
        if (req == null)
            throw new InvalidRequestException("Request cannot be null");
        if (req.getBankCode() == null || req.getBankCode().isBlank())
            throw new InvalidRequestException("bankCode is required");
        if (req.getBranchCode() == null || req.getBranchCode().isBlank())
            throw new InvalidRequestException("branchCode is required");
        if (req.getNameSystem() == null || req.getNameSystem().isBlank())
            throw new InvalidRequestException("nameSystem is required");
        if (req.getErrorType() == null)
            throw new InvalidRequestException("errorType is required");
    }

    private void validateCommonFields(String bank, String branch, String aes) {
        if (StringUtils.isBlank(bank) || !CODE_PATTERN.matcher(bank).matches()) {
            throw new InvalidRequestException("bankCode must be 1..3 digits");
        }
        if (StringUtils.isBlank(branch) || !CODE_PATTERN.matcher(branch).matches()) {
            throw new InvalidRequestException("branchCode must be 1..3 digits");
        }
        if (StringUtils.isBlank(aes) || !AES_PATTERN.matcher(aes).matches()) {
            throw new InvalidRequestException("nameSystem must match [A-Za-z0-9_]{1,12}");
        }
    }

    private void validateCard(EnrollDTO c) {
        if (c.getAccount() == null || !ACCOUNT_PATTERN.matcher(c.getAccount()).matches()) {
            throw new InvalidRequestException("account must be 16 digits: " + c.getAccount());
        }
        if (c.getType() == null) {
            throw new InvalidRequestException("card type cannot be null");
        }
        if (c.getSumm() < 0) {
            throw new InvalidRequestException("sum cannot be negative");
        }
    }

    public void validateSend(SendRequestDTO req) {
        if (req == null) {
            throw new InvalidRequestException("Request cannot be null");
        }
        if (StringUtils.isBlank(req.getFilename())) {
            throw new InvalidRequestException("filename is required");
        }
        if (req.getTypeSend() == null) {
            throw new InvalidRequestException("typeSend is required");
        }
    }


}