package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try {
            InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties");
            if(input != null) {
                properties.load(input);
            } else {
                System.err.println("Файл не найден в ресурсах");
            }
            System.getenv().forEach((k, v) -> {
                if(k.startsWith("TELEGRAM_") ||  k.startsWith("AVITO_")) {
                    properties.setProperty(k.toLowerCase().replace('_', '.'), v);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
