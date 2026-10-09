package ru.mealcard.service.mock;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.service.format.dto.DataForEnrollDTO;
import ru.mealcard.service.mock.dto.MockRequestDTO;
import ru.mealcard.service.responseDTO.ResponseDTO;
import ru.mealcard.service.format.impl.EnrollVisitor;
import ru.mealcard.utils.format.Visitor;
import ru.mealcard.utils.generate_models.TypeProcedure;
import ru.mealcard.service.FileGeneratorService;
import ru.mealcard.service.validator.RequestValidator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockService extends Base {

    @Getter
    private static final MockService instance = new MockService();

    private final RequestValidator validator = RequestValidator.getInstance();
    private final FileGeneratorService fileGenerator = FileGeneratorService.getInstance();
    private final MockDataService mockData = MockDataService.getInstance();
    private final Visitor<DataForEnrollDTO> visitor = new EnrollVisitor();

    private MockService() {}

    public ResponseDTO process(MockRequestDTO request) {
        validator.validateMock(request);

        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < request.getFileCount(); i++) {
            DataForEnrollDTO data = convertToDataForEnroll(request);

            String filename = fileGenerator.generate(
                    data,
                    visitor,
                    request.getBankCode(),
                    request.getBranchCode(),
                    request.getNameSystem()
            );
            filenames.add(filename);
        }

        ResponseDTO response = new ResponseDTO();
        response.setStatus("SUCCESS");
        response.setFilenames(filenames);
        return response;
    }

    private DataForEnrollDTO convertToDataForEnroll(MockRequestDTO requestDTO) {
        return new DataForEnrollDTO(
                ZonedDateTime.now(ZoneId.of(getConfig().getZone())),
                null,
                TypeProcedure.IMMEDIATE,
                mockData.generateRecords(requestDTO.getRowCount())
        );
    }
}