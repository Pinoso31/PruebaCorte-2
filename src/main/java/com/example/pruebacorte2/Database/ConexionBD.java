package com.example.pruebacorte2.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/papeleria_campus";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private ConexionBD() {

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
