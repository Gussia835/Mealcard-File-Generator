package ru.mealcard.service.validator;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.exception.content.BlankFileException;
import ru.mealcard.exception.content.ContentFileException;
import ru.mealcard.exception.content.WrongEncodingException;
import ru.mealcard.utils.encoding.EncodingAdapter;
import ru.mealcard.utils.encoding.FileEncoding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class FileValidator extends Base {

    @Getter
    private static final FileValidator instance = new FileValidator();

    private final EncodingAdapter detector = new EncodingAdapter();

    private FileValidator() {


    }

    public void validate(Path filepath) {
        byte[] bytes = read(filepath);

        if (bytes.length == 0) {

            error("Error: file is empty {}", filepath);

            throw new ContentFileException("file is empty");
        }

        FileEncoding detected = detector.detect(bytes);

        Set<FileEncoding> allowed = FileEncoding.ALLOWED_ENCODINGS;
        if (detected == null || !allowed.contains(detected)) {
            warn("not allowed encoding: {}", detected);

            throw new WrongEncodingException("wrong encoding");
        }

        String text = new String(bytes, detected.getCharset());
        if (text.isBlank()) {

            throw new BlankFileException("File has only whitespace: " + filepath.getFileName());
        }
    }

    private byte[] read(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }


    }
}

