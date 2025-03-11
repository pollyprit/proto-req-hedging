import java.sql.*;

public class Database {
    private static String url = System.getenv("IMDB_DB_URL");
    private static String user = System.getenv("IMDB_DB_USERNAME");
    private static String password = System.getenv("IMDB_DB_PASSWORD");
    private Connection connection;

    Database() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");

        this.connection = DriverManager.getConnection(url, user, password);
    }

    String getMovieNameFromID(int movieId) {
       try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name from movies where id = " + movieId);

            if (resultSet.next())
                return resultSet.getString("name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "<NA>";
    }
}
