package ru.mealcard.service.send.Impl;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import ru.mealcard.Base;
import ru.mealcard.exception.send.SendException;
import ru.mealcard.utils.send_models.Sender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ChunkSender extends Base implements Sender {

    private final String sendTo;
    private final CloseableHttpClient client;

    public ChunkSender(String sendTo) {
        this.sendTo = sendTo;
        this.client = HttpClients.createDefault();
    }

    @Override
    public void send(Path filepath, Map<String, String> metadata) {
        try {
            HttpPost post = createRequest(filepath, metadata);
            executeRequest(post, filepath);
        } catch (IOException e) {
            error("chunked send failed: {}", e.getMessage(), e);
            throw new SendException("chunked send failed" + e.getMessage());
        }
    }

    private HttpPost createRequest(Path filepath, Map<String, String> metadata) throws IOException {

        HttpPost post = new HttpPost(sendTo);
        metadata.forEach((k, v) -> post.setHeader("X-Meta-" + k, v));
        post.setHeader("filename", filepath.getFileName().toString());


        InputStreamEntity entity = new InputStreamEntity(
                Files.newInputStream(filepath), -1, ContentType.APPLICATION_OCTET_STREAM);
        post.setEntity(entity);
        return post;
    }

    private void executeRequest(HttpPost post, Path filepath) throws IOException {
        try (CloseableHttpResponse response = client.execute(post)) {
            int status = response.getCode() / 100;
            if (status != 2) {
                throw new SendException("chunked failed: HTTP " + response.getCode());
            }
            info("Chunked OK: {} ({} bytes)", filepath.getFileName(), Files.size(filepath));
        }
    }
}