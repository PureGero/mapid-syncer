package com.github.puregero.mapidsyncer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    public static String HOST;
    public static String PORT;
    public static String DATABASE;
    public static String USERNAME;
    public static String PASSWORD;
    private static Connection connection = null;

    private static long lastValidCheck = 0;
    private static final long VALID_CHECK_PERIOD = 30000;

    public static Connection connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            if (lastValidCheck < System.currentTimeMillis() - VALID_CHECK_PERIOD) {
                if (connection.isValid(5000)) {
                    lastValidCheck = System.currentTimeMillis();
                } else {
                    try {
                        connection.close();
                    } catch (SQLException ignored) {}
                    return openConnection();
                }
            }

            return connection;
        }

        return openConnection();
    }

    private static Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", HOST, PORT, DATABASE, USERNAME, PASSWORD));

            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }

    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        return connect().prepareStatement(sql);
    }
}
