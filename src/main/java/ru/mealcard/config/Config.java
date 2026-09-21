package ru.mealcard.config;

import ru.mealcard.Base;
import ru.mealcard.utils.config.PropertyKeys;
import ru.mealcard.utils.encoding.FileEncoding;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
        return Integer.parseInt(get(PropertyKeys.PORT_KEY, "8081"));
    }

    public int getPoolSize() {
        return Integer.parseInt(get(PropertyKeys.POOL_SIZE_KEY, "2"));
    }

    public String getZone() {
        return get(PropertyKeys.ZONE_KEY, "Europe/Moscow");
    }


    public Path getOutputDir() {
        return Path.of(get(PropertyKeys.OUTPUT_DIR_KEY, "./out/cards"));
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
        String sysHost = System.getProperty("grpc.host");
        if (sysHost != null && !sysHost.trim().isEmpty()) {
            return sysHost.trim();
        }
        return PROPERTIES.getProperty(PropertyKeys.GRPC_HOST_KEY, "localhost");
    }

    public int getGrpcPort() {
        String sysPort = System.getProperty("grpc.port");
        if (sysPort != null && !sysPort.trim().isEmpty()) {
            return Integer.parseInt(sysPort.trim());
        }
        return Integer.parseInt(PROPERTIES.getProperty(PropertyKeys.GRPC_PORT_KEY, "6666"));
    }
}