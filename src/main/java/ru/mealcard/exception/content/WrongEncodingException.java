package ru.mealcard.exception.content;

public class WrongEncodingException extends RuntimeException {
    public WrongEncodingException(String message) {
        super(message);
    }
}
