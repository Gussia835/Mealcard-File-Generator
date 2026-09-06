package ru.mealcard.utils.request;

import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.exception.request.InvalidRequestException;

public class RequestConverterUtil extends Base {
    @Getter private static final RequestConverterUtil instance = new RequestConverterUtil();

    private RequestConverterUtil() {

    }

    public static <T> T parseBody(HttpExchange exchange, Class<T> clazz) {
        try {
            byte[] bytes = exchange.getRequestBody().readAllBytes();

            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }


}
