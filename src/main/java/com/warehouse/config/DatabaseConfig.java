package com.warehouse.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static Properties properties = new Properties();
    private static String activeDb;
    private static final String CONFIG_FILE = "config.properties";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            // Пытаемся загрузить из ресурсов JAR
            InputStream inputStream = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("config.properties");

            if (inputStream == null) {
                throw new RuntimeException("config.properties не найден в ресурсах");
            }

            properties.load(inputStream);
            activeDb = properties.getProperty("db.active");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить файл конфигурации", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db." + activeDb + ".url");
        String user = properties.getProperty("db." + activeDb + ".user");
        String password = properties.getProperty("db." + activeDb + ".password");
        String driver = properties.getProperty("db." + activeDb + ".driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC драйвер не найден: " + driver, e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    public static String getActiveDatabase() {
        return activeDb;
    }

    public static void setActiveDatabase(String dbType) throws IOException {
        if (!dbType.equals("postgres") && !dbType.equals("mssql")) {
            throw new IllegalArgumentException("Неподдерживаемая СУБД: " + dbType);
        }

        activeDb = dbType;
        properties.setProperty("db.active", dbType);

        // Сохраняем изменения в файл
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Обновлено из приложения");
        }
    }

    public static String getDatabaseDisplayName() {
        return switch (activeDb) {
            case "postgres" -> "PostgreSQL";
            case "mssql" -> "MS SQL Server";
            default -> activeDb;
        };
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}