package ru.mealcard.service.error;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.service.dto.RequestErrorDTO;
import ru.mealcard.service.format.dto.EnrollDTO;
import ru.mealcard.service.mock.MockDataService;
import ru.mealcard.utils.encoding.FileCorruptor;
import ru.mealcard.utils.filename.FilenameGeneratorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ErrorService extends Base {


    private final FileCorruptor corruptor = FileCorruptor.getInstance();
    private final FilenameGeneratorUtil filenameGenerator = FilenameGeneratorUtil.getInstance();
    private final MockDataService mockData = MockDataService.getInstance();

    @Getter
    private static final ErrorService instance = new ErrorService();

    private ErrorService() {


    }


    public Path generate(RequestErrorDTO requestDTO) {

        String bankCode = requestDTO.getBankCode();
        String branchCode = requestDTO.getBranchCode();
        String nameSystem = requestDTO.getNameSystem();

        String filename = filenameGenerator.generate(bankCode,
                                                branchCode,
                                                nameSystem);

        Path filepath = getConfig().getOutputDir().resolve(filename);

        try {
            Files.createDirectories(filepath.getParent());
        } catch (IOException e) {
            throw new FileGenerationException("Cannot create directory: " + filepath.getParent());
        }

        switch (requestDTO.getErrorType()) {
            case EMPTY, SPACES, NEWLINES -> {
                int lineCounts = requestDTO.getLineCount();

                corruptor.writeStructural(filepath, requestDTO.getErrorType(), lineCounts);
            }

            case UTF_16, WINDOWS_1251 -> {
                String content = buildFakerContent(requestDTO);
                corruptor.writeWithEncoding(filepath, content, requestDTO.getErrorType());
            }
        }

        info("Error file created: {} (type={})", filename, requestDTO.getErrorType());
        return filepath;
    }

    private String buildFakerContent(RequestErrorDTO requestErrorDTO) {
        int lineCount = requestErrorDTO.getLineCount();

        Iterable<EnrollDTO> records = mockData.generateRecords(lineCount);

        StringBuilder sb = new StringBuilder();

        for (EnrollDTO record : records) {
            sb.append(String.format("%s|%s|%s|%s|%d|Тест%n",
                    requestErrorDTO.getBankCode(),
                    requestErrorDTO.getBranchCode(),
                    requestErrorDTO.getNameSystem(),
                    record.getAccount(),
                    record.getSumm()));
        }

        return sb.toString();
    }

}
