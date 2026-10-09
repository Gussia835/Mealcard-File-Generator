package ru.mealcard.config;

import ru.mealcard.Base;
import ru.mealcard.utils.config.PropertyKeys;
import java.nio.file.Path;
import java.util.Properties;

public class Config extends Base {
    private static final Properties PROPERTIES = new Properties();
    private static final PropertyLoader PROPERTY_LOADER = PropertyLoader.getInstance();
    private static final Config INSTANCE = new Config();

    private Config() {
        PROPERTY_LOADER.load(PROPERTIES);
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    private String get(String key, String def) {
        return PROPERTIES.getProperty(key, def);
    }

    public int getPort() {
        return Integer.parseInt(get(PropertyKeys.PORT_KEY, "90"));
    }

    public int getPoolSize() {
        return Integer.parseInt(get(PropertyKeys.POOL_SIZE_KEY, "2"));
    }

    public String getZone() {
        return get(PropertyKeys.ZONE_KEY, "Europe/Moscow");
    }

    public Path getOutputDir() {
        return Path.of(get(PropertyKeys.OUTPUT_DIR_KEY, "out/cards"));
    }

    public String getSendUrl() {
        String envUrl = System.getenv("SEND_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return get(PropertyKeys.SEND_URL_KEY, "http://localhost:666/files");
    }

    public int getChunkSize() {
        return Integer.parseInt(get(PropertyKeys.SEND_CHUNK_SIZE_KEY, "1048576"));
    }

    public String getGrpcHost() {
        String envHost = System.getenv("GRPC_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            return envHost.trim();
        }
        return get(PropertyKeys.GRPC_HOST_KEY, "localhost");
    }

    public int getGrpcPort() {
        String envPort = System.getenv("GRPC_PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return Integer.parseInt(envPort.trim());
        }
        return Integer.parseInt(get(PropertyKeys.GRPC_PORT_KEY, "6666"));
    }
}