package core.commands.UserCommands;

import org.json.JSONObject;

import java.sql.*;

public class UserDAL {

    Connection con;
    public UserDAL(){
        connectPostgreSQL();
    }
    public void connectPostgreSQL(){
        String url = "jdbc:postgresql://ec2-54-74-14-109.eu-west-1.compute.amazonaws.com:5432/d6461e8nfjon20?user=tubgbcanmobdya&password=6504bc63cc929664bd5ebbbdba04310949db9925ccc7a4d24cae3e41c58c8269";
        String user = "tubgbcanmobdya";
        String password = "6504bc63cc929664bd5ebbbdba04310949db9925ccc7a4d24cae3e41c58c8269";

        try{
            con = DriverManager.getConnection(url, user, password);
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
    }

    public Connection getPostgreSQLConnection(){
        return  con;
    }

    public void createUser(JSONObject json) throws SQLException {

        String firstName = json.getString("firstName");
        String lastName = json.getString("lastName");
        String query = "INSERT INTO users(firstName,lastName) VALUES(?,?)";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, firstName);
        pst.setString(2, lastName);
        pst.executeUpdate();
        pst = con.prepareStatement("SELECT * FROM users");
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            System.out.print(rs.getInt(1));
            System.out.print(": ");
            System.out.print(rs.getString(2));
            System.out.print(": ");
            System.out.println(rs.getString(3));
        }
    }
}
