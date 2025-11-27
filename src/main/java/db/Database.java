package db;

import model.Flat;

import java.sql.*;

public class Database {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    static {
        try (Connection conn = connect()) {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS flats (" +
                            "id TEXT PRIMARY KEY, " +
                            "title TEXT, " +
                            "price TEXT, " +
                            "url TEXT, " +
                            "rooms TEXT, " +
                            "area TEXT, " +
                            "metro TEXT, " +
                            "time BIGINT)"
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean isNew(String id) throws Exception {
        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM flats WHERE id = ?"
            );
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        }
    }

    public static void saveFlat(Flat f) throws Exception {
        try (Connection conn = connect()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO flats (id, title, price, url, rooms, area, metro, time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, f.id);
            ps.setString(2, f.title);
            ps.setString(3, f.price);
            ps.setString(4, f.url);
            ps.setString(5, f.rooms);
            ps.setString(6, f.area);
            ps.setString(7, f.metro);
            ps.setLong(8, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }
}