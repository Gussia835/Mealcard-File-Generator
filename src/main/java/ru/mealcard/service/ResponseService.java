    package ru.mealcard.service;

    import com.sun.net.httpserver.HttpExchange;
    import lombok.Getter;
    import ru.mealcard.Base;
    import ru.mealcard.service.responseDTO.ErrorDTO;

    import java.io.OutputStream;

    public class ResponseService extends Base {
        @Getter
        private static final ResponseService instance = new ResponseService();

        private ResponseService() {}

        public void sendJson(HttpExchange exchange, int code, Object dto) {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(dto);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(code, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                error("response dont sended", e.getMessage(), e);
            } finally {
                exchange.close();
            }
        }

        public void sendError(HttpExchange exchange, int code, String message) {
            sendJson(exchange, code, new ErrorDTO("ERROR", message));
        }
    }
