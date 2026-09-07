package ru.mealcard.utils.send_models;

import lombok.Getter;
import ru.mealcard.Base;
import ru.mealcard.config.Config;
import ru.mealcard.exception.send.SendException;
import ru.mealcard.service.send.Impl.ChunkSender;
import ru.mealcard.service.send.Impl.GrpcSender;
import ru.mealcard.service.send.Impl.MultipartSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SenderFabric extends Base {

    @Getter
    private static final SenderFabric instance = new SenderFabric();

    private final Map<TypeSend, Sender> senders = new HashMap<>();

    private SenderFabric() {

        try {
            Config config = Config.getInstance();

            createSender(TypeSend.MULTIPART, new MultipartSender(config.getSendUrl() + "/multipart"));
            createSender(TypeSend.CHUNK, new ChunkSender(config.getSendUrl() + "/chunk"));
            createSender(TypeSend.GRPC, new GrpcSender(config.getGrpcHost(), config.getGrpcPort(), config.getChunkSize()));
        } catch (IOException e) {
            throw new SendException("cant create grpc Sender");
        }
    }

    private void createSender(TypeSend type, Sender sender) {
        senders.put(type, sender);
    }

    public Sender get(TypeSend type) {
        Sender sender = senders.get(type);
        if (sender == null) {
            throw new IllegalArgumentException("Unknown sender type: " + type);
        }
        return sender;
    }
}