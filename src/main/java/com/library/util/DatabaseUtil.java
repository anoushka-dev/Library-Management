
package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseUtil {

    private static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/library_management_system"
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=UTC";

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_URL;
        }

        if (user == null || user.trim().isEmpty()) {
            user = "root";
        }

        if (password == null) {
            throw new SQLException(
                "Database password is not configured. Set DB_PASSWORD."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC Driver not found.", e
            );
        }

        return DriverManager.getConnection(url, user, password);
    }
}
