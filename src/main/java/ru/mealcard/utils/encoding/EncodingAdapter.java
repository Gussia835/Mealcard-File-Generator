package ru.mealcard.utils.encoding;


import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.UniversalEncodingDetector;
import ru.mealcard.Base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;


public class EncodingAdapter extends Base {

    public FileEncoding detect(byte[] bytes) {
        String name = detectName(bytes);

        if (name == null) {
            warn("Cant detect encoding, assume UTF-8");
            return FileEncoding.UTF_8;
        }

        try {
            return FileEncoding.fromName(name);

        } catch (IllegalArgumentException e) {
            warn("Detected unsupported encoding: {}", name);
            return null;
        }

    }

    private String detectName(byte[] bytes) {
        UniversalEncodingDetector detector = new UniversalEncodingDetector();
        Metadata metadata = new Metadata();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            Charset charset = detector.detect(byteArrayInputStream, metadata);
            return charset != null ? charset.name() : null;
        } catch (IOException e) {
            error("cant detect charset {}", e.getMessage());
            return null;
        }
    }

}

