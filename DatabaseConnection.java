
package collegeadmin.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties config = new Properties();

    static {
        try (FileInputStream in =
                new FileInputStream("db.properties")) {

            config.load(in);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                "Could not load db.properties: " + e.getMessage()
            );
        }
    }

    public static Connection getConnection()
            throws SQLException {

        String url = config.getProperty("url");
        String user = config.getProperty("user");
        String password = config.getProperty("password");

        if (url == null || user == null || password == null) {
            throw new SQLException(
                "Database configuration is incomplete."
            );
        }

        return DriverManager.getConnection(
            url, user, password
        );
    }

    public static void main(String[] args) {

        try (Connection con = getConnection()) {

            System.out.println(
                "Connected to MySQL successfully!"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
