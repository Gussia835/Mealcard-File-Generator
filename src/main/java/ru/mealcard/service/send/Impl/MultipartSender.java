package ru.mealcard.service.send.Impl;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import ru.mealcard.Base;
import ru.mealcard.exception.send.SendException;
import ru.mealcard.utils.send_models.Sender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MultipartSender extends Base implements Sender {

    private final String sendTo;
    private final CloseableHttpClient client;

    public MultipartSender(String sendTo) {
        this.sendTo = sendTo;
        this.client = HttpClients.createDefault();
    }

    @Override
    public void send(Path filepath, Map<String, String> metadata) {
        try {

            HttpPost post = createRequest(filepath, metadata);
            executeRequest(post, filepath);

        } catch (IOException e) {


            error("multipart send failed: {}", e.getMessage(), e);
            throw new SendException("multipart send failed" + e.getMessage());

        }
    }

    private HttpPost createRequest(Path filepath, Map<String, String> metadata) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        metadata.forEach(builder::addTextBody);

        builder.addBinaryBody("file", filepath.toFile(),
                ContentType.MULTIPART_FORM_DATA, filepath.getFileName().toString());


        HttpPost post = new HttpPost(sendTo);
        post.setEntity(builder.build());

        return post;
    }

    private void executeRequest(HttpPost post, Path filepath) throws IOException {
        try (CloseableHttpResponse response = client.execute(post)) {
            int status = response.getCode() / 100;

            if (status != 2) {
                throw new SendException(" multipart failed: HTTP " + response.getCode());
            }

            info("Multipart OK: {} ({} bytes)", filepath.getFileName(), Files.size(filepath));
        }
    }
}