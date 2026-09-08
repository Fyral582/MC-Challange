package de.Fyral.challange;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection connection;

    public void connect() {
        File dataFolder = Main.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "data.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try {
            connection = DriverManager.getConnection(url);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String effectsTable = "CREATE TABLE IF NOT EXISTS random_effects ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "effect_name TEXT NOT NULL"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(effectsTable);
        }
    }

    public void addEffect(String effectName) {
        String sql = "INSERT INTO random_effects(effect_name) VALUES(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, effectName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getEffects() {
        List<String> effects = new ArrayList<>();
        String sql = "SELECT effect_name FROM random_effects";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                effects.add(rs.getString("effect_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return effects;
    }

    public void clearEffects() {
        String sql = "DELETE FROM random_effects";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}