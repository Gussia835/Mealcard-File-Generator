package ru.mealcard.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.mealcard.Base;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.exception.request.InvalidRequestException;
import ru.mealcard.exception.send.SendException;
import ru.mealcard.service.ResponseService;
import ru.mealcard.service.dto.ResponseDTO;
import ru.mealcard.service.send.SendService;
import ru.mealcard.service.send.dto.SendRequestDTO;
import ru.mealcard.utils.request.RequestConverterUtil;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;

public class SendHandler extends Base implements HttpHandler {


    private ResponseService responseService = ResponseService.getInstance();
    private SendService sendService = SendService.getInstance();

    private ExecutorService executorService;


    public SendHandler(ExecutorService executorService) {

        this.executorService = executorService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Mthod should be POST");
            return;
        }

        executorService.submit(() -> process(exchange));

    }

    private void process(HttpExchange exchange) {
        try {
            SendRequestDTO requestDTO = RequestConverterUtil.parseBody(exchange, SendRequestDTO.class);
            ResponseDTO response = sendService.process(requestDTO);
            responseService.sendJson(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (InvalidRequestException e) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } catch (FileGenerationException e) {
            error("File generation failed: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Generation failed");
        }catch (SendException e) {
            error("Send failed: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Send fail because file invalid" + e.getMessage());
        } catch (Exception e) {
            error("Unexpected error: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal server error " + e.getMessage());
        }
    }
}