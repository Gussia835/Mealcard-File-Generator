package ru.mealcard.utils.format;

import java.nio.charset.Charset;
import java.nio.file.Path;

public interface Visitor<T> {
    void visit(Path target, T data);
}