package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;

public class PostgreSQL {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/piazza";
        String user = "postgres";
        String password = "";
        try{
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id serial PRIMARY KEY, name VARCHAR(25));");
            String name = "Ahmed";
            String query = "INSERT INTO users(name) VALUES(?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.executeUpdate();
            pst = con.prepareStatement("SELECT * FROM users");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                System.out.print(rs.getInt(1));
                System.out.print(": ");
                System.out.println(rs.getString(2));
            }
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
    }
}
