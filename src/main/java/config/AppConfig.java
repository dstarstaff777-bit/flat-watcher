package config;

public class AppConfig {
    public static final String DB_URL = System.getenv("DB_URL");
    public static final String DB_USER = System.getenv("DB_USER");
    public static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

}
