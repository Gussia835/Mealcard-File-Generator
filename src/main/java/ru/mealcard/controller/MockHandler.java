package ru.mealcard.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.mealcard.Base;
import ru.mealcard.service.dto.ResponseDTO;
import ru.mealcard.utils.request.RequestConverterUtil;
import ru.mealcard.service.dto.MockRequestDTO;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.exception.request.InvalidRequestException;
import ru.mealcard.service.mock.MockService;
import ru.mealcard.service.ResponseService;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class MockHandler extends Base implements HttpHandler {

    private final MockService service = MockService.getInstance();
    private final ResponseService responseService = ResponseService.getInstance();
    private final ExecutorService executorService;

    public MockHandler(ExecutorService executorService) {

        this.executorService = executorService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
            return;
        }

        try {
            executorService.submit(() -> process(exchange));
        } catch (RejectedExecutionException e) {
            error("Thread pool rejected task: {}", e.getMessage());
            responseService.sendError(exchange, HttpURLConnection.HTTP_UNAVAILABLE, "Server busy");
        }
    }

    private void process(HttpExchange exchange) {
        try {
            MockRequestDTO requestDTO = RequestConverterUtil.parseBody(exchange, MockRequestDTO.class);
            ResponseDTO response = service.process(requestDTO);
            responseService.sendJson(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (InvalidRequestException e) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } catch (FileGenerationException e) {
            error("File generation failed: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Generation failed");
        } catch (Exception e) {
            error("Unexpected error: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal server error");
        }
    }
}