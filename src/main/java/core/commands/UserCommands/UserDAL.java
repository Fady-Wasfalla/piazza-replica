package core.commands.UserCommands;

import org.json.JSONObject;

import java.sql.*;

public class UserDAL {

    Connection con;
    public UserDAL(){
        connectPostgreSQL();
    }
    public void connectPostgreSQL(){
        String url = "jdbc:postgresql://ec2-54-220-35-19.eu-west-1.compute.amazonaws.com:5432/d2spprpmp8ult?user=fbqvcficlrhimr&password=f989e5d9f18291eddb927bfbbbcf76121d597dddc5a3350f136b233e7c4af518";
        String user = "fbqvcficlrhimr";
        String password = "f989e5d9f18291eddb927bfbbbcf76121d597dddc5a3350f136b233e7c4af518";

        try{
            System.out.println("USEERDAL");
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
