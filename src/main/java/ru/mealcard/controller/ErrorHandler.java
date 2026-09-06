package ru.mealcard.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.mealcard.Base;
import ru.mealcard.exception.content.BlankFileException;
import ru.mealcard.exception.content.ContentFileException;
import ru.mealcard.exception.request.InvalidRequestException;
import ru.mealcard.exception.content.WrongEncodingException;
import ru.mealcard.service.ResponseService;
import ru.mealcard.service.dto.RequestErrorDTO;
import ru.mealcard.service.dto.ResponseDTO;
import ru.mealcard.service.error.ErrorService;
import ru.mealcard.service.validator.RequestValidator;
import ru.mealcard.utils.request.RequestConverterUtil;


import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class ErrorHandler extends Base implements HttpHandler {

    private final ResponseService responseService = ResponseService.getInstance();
    private final ErrorService service = ErrorService.getInstance();
    private final ExecutorService executorService;
    private final RequestValidator validator = RequestValidator.getInstance();

    public ErrorHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method should be POST");
            exchange.close();
            return;
        }
        executorService.submit(() -> process(exchange));
    }

    private void process(HttpExchange exchange) {
        try {
            RequestErrorDTO req = RequestConverterUtil.parseBody(exchange, RequestErrorDTO.class);
            validator.validateError(req);

            Path file = service.generate(req);

            ResponseDTO response = new ResponseDTO();
            response.setStatus("SUCCESS");
            response.setFilename(file.getFileName().toString());
            responseService.sendJson(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (InvalidRequestException e) {
            responseService.sendError(exchange, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());


        } catch (WrongEncodingException | BlankFileException | ContentFileException e) {
            error("File validation failed: {}", e.getMessage(), e);
            responseService.sendError(exchange, 422, e.getMessage());
        }

        catch (Exception e) {
            error("Error file generation failed: {}", e.getMessage(), e);
            responseService.sendError(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Generation failed");
        }
    }

}