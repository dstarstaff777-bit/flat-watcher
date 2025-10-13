package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if(input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Unable to find config.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
    public static long getLongProperty(String key) {
        return Long.parseLong(properties.getProperty(key));
    }
    public static int getTargetPrice() {
        return Integer.parseInt(getProperty("target.price"));
    }
    public static int getCheckIntervalMinutes() {
        return Integer.parseInt(getProperty("check.interval.minutes"));
    }
    public static String getAvitoUrl() {
        return getProperty("avito.url");
    }
}
