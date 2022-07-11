package ru.masaviktoria;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConnection {
    public static Connection connection;
    public static Statement statement;
    SQLConnection (){

    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users", "postgres", "Normandia1$");
        statement = connection.createStatement();
    }

    public static void disconnect() {
        try {
            if (connection != null){
                connection.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

}
