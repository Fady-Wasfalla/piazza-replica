package Database;

import java.sql.*;

public class PostgreSQL {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/piazza";
        url = "jdbc:postgresql://ec2-54-74-14-109.eu-west-1.compute.amazonaws.com:5432/d6461e8nfjon20?user=tubgbcanmobdya&password=6504bc63cc929664bd5ebbbdba04310949db9925ccc7a4d24cae3e41c58c8269";
        String user = "postgres";
        user = "tubgbcanmobdya";
        String password = "";
        password = "6504bc63cc929664bd5ebbbdba04310949db9925ccc7a4d24cae3e41c58c8269";
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
