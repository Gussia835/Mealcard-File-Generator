package ru.mealcard.service;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.service.format.dto.DataForEnrollDTO;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.utils.format.Visitor;
import ru.mealcard.utils.generate_models.TypeProcedure;
import ru.mealcard.utils.filename.FilenameGeneratorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileGeneratorService extends Base {

    private final ShedulerService scheduler = ShedulerService.getInstance();
    private final FilenameGeneratorUtil filenameGenerator = FilenameGeneratorUtil.getInstance();

    @Getter
    private static final FileGeneratorService instance = new FileGeneratorService();



    private FileGeneratorService() {
        try {
            Files.createDirectories(getConfig().getOutputDir());
            info("directory create");
        } catch (IOException e) {
            error("cant create directories {}", e.getMessage(), e);
            throw new FileGenerationException("error during generation file " + e.getMessage());
        }
    }

    public <T> String generate(T data, Visitor<T> visitor, String bankCode,
                                   String branchCode, String nameSystem) {
        String filename = filenameGenerator.generate(bankCode, branchCode, nameSystem);
        Path output = getConfig().getOutputDir().resolve(filename);

        visitor.visit(output, data);
        info("file created {}", filename);

        if (data instanceof DataForEnrollDTO dto) {
            shedule(dto, filename);
        }

        return filename;

    }

    private void shedule(DataForEnrollDTO data, String filename) {
        if (data.getProcType() == TypeProcedure.IN_TIME && data.getScheduledDateTime() != null) {
            scheduler.shedule(
                    data.getScheduledDateTime().toLocalDateTime(),
                    () -> info("IN-TIME processed: {}", filename)
            );
        }
    }
}
