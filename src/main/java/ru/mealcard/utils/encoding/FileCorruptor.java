package ru.mealcard.utils.encoding;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.utils.error.ErrorType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCorruptor extends Base {

    @Getter
    private static final FileCorruptor instance = new FileCorruptor();

    private FileCorruptor() {


    }


    public void writeWithEncoding(Path file, String content, ErrorType type) {
        try {
            FileEncoding enc = type.getEncoding();
            Files.writeString(file, content, enc.getCharset());

            info("Corrupted file written (encoding): {} ({})", file.getFileName(), type);
        } catch (IOException e) {

            throw new FileGenerationException("Failed to write corrupted file: " + file);
        }
    }

    public void writeStructural(Path file, ErrorType type, int lineCount) {
        try {
            switch (type) {
                case EMPTY    -> Files.write(file, new byte[0]);

                case SPACES   -> Files.writeString(file, spaces(lineCount), StandardCharsets.UTF_8);

                case NEWLINES -> Files.writeString(file, "\n".repeat(lineCount), StandardCharsets.UTF_8);

                default -> throw new IllegalArgumentException("Not a structural error: " + type);
            }

            info("Corrupted file written (structural): {} ({})", file.getFileName(), type);
        } catch (IOException e) {

            throw new FileGenerationException("Failed to write corrupted file: " + file);

        }
    }

    private String spaces(int lineCount) {


        return ("    \t   \t\t    \n").repeat(lineCount);
    }
}