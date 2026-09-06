package ru.mealcard.utils.encoding;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

public enum FileEncoding {
    UTF_8("UTF-8", StandardCharsets.UTF_8),
    UTF_16("UTF-16",StandardCharsets.UTF_16),
    WINDOWS_1251("Windows-1251",Charset.forName("windows-1251")),
    WINDOWS_1252("Windows-1252", Charset.forName("windows-1252"));

    public static final Set<FileEncoding> ALLOWED_ENCODINGS = EnumSet.of(UTF_8,
                                                            UTF_16,
                                                            WINDOWS_1251,
                                                            WINDOWS_1252);


    @Getter
    private final String name;

    @Getter
    private final Charset charset;

    FileEncoding(String name, Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    @JsonCreator
    public static FileEncoding fromName(String s) {
        for (FileEncoding e : values())
            if (e.name.equalsIgnoreCase(s)) return e;
        throw new IllegalArgumentException("Unknown encoding: " + s);
    }

}
