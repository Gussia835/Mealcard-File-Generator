package ru.mealcard.service.send.Impl;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.FileChunk;
import proto.FileTransportGrpc;
import proto.UploadResponse;
import ru.mealcard.Base;
import ru.mealcard.exception.send.SendException;
import ru.mealcard.utils.send_models.Sender;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcSender extends Base implements Sender {
    private final ManagedChannel channel;
    private final int chunkSize;

    public GrpcSender(String host, int port, int chunkSize) {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .useTransportSecurity()
                .build();
        this.chunkSize = chunkSize;
    }

    private static class UploadContext {
        CountDownLatch latch = new CountDownLatch(1);
        String uploadId;
        Throwable error;
    }

    @Override
    public void send(Path filepath, Map<String, String> metadata) {
        try {
            FileTransportGrpc.FileTransportStub stub = FileTransportGrpc.newStub(channel);
            UploadContext context = new UploadContext();

            StreamObserver<FileChunk> requestObserver = openStream(stub, context);
            sendChunks(filepath, metadata, requestObserver);
            requestObserver.onCompleted();

            waitForCompletion(context, filepath);
        } catch (SendException e) {
            throw e;
        } catch (Exception e) {
            error("gRPC send failed: {}", e.getMessage(), e);
            throw new SendException("gRPC send failed");
        }
    }

    private FileTransportGrpc.FileTransportStub createStub() {
        return FileTransportGrpc.newStub(channel);
    }

    private StreamObserver<FileChunk> openStream(FileTransportGrpc.FileTransportStub stub, UploadContext context) {

        StreamObserver<UploadResponse> responseObserver = new StreamObserver<>() {

            @Override
            public void onNext(UploadResponse resp) {
                info("received response: {}", resp.getStatus());
                context.uploadId = resp.getRemoteId();
            }

            @Override
            public void onError(Throwable t) {
                error("gRPC error: {}", t.getMessage(), t);
                context.error = t;
                context.latch.countDown();
            }

            @Override
            public void onCompleted() {
                info("Server completed upload");
                context.latch.countDown();
            }
        };

        return stub.upload(responseObserver);
    }

    private void sendChunks(Path filepath, Map<String, String> metadata,
                            StreamObserver<FileChunk> requestObserver) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filepath.toFile(), "r")) {
            byte[] buffer = new byte[chunkSize];
            long offset = 0;
            long totalSize = Files.size(filepath);
            int chunkNumber = 0;

            while (offset < totalSize) {
                raf.seek(offset);
                int readBytes = raf.read(buffer);
                if (readBytes == -1) break;

                boolean isLast = offset + readBytes >= totalSize;

                FileChunk chunk = FileChunk.newBuilder()
                        .setFilename(filepath.getFileName().toString())
                        .setOffset(offset)
                        .setData(ByteString.copyFrom(buffer, 0, readBytes))
                        .setIsLast(isLast)
                        .putAllMetadata(metadata)
                        .build();

                requestObserver.onNext(chunk);
                chunkNumber++;

                info("Sent chunk {}: offset={}, bytes={}, isLast={}",
                        chunkNumber, offset, readBytes, isLast);

                offset += readBytes;
            }
        }
    }

    private void waitForCompletion(UploadContext context, Path filepath)
            throws InterruptedException {
        if (!context.latch.await(30, TimeUnit.SECONDS)) {
            error("gRPC timeout after 30 seconds");
            throw new SendException("gRPC timeout sending error");
        }

        if (context.error != null) {
            error("error of sending file on gRPC {}", context.error.getMessage(), context.error);
            throw new SendException("gRPC sending error " + context.error.getMessage());
        }

        try {
            info("waiting is suceess. FileSize: " + Files.size(filepath));
        } catch (IOException e) {
            error("gRPC sending error of find object on {}", filepath, e.getMessage(), e);
            throw new SendException("gRPC error of finding size file");
        }

    }


}


