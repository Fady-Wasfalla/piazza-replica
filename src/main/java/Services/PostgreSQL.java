package Services;

import java.io.*;

import java.sql.*;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.json.JSONObject;

public class PostgreSQL {

    public static Connection connectPostgres(String url, String user, String password){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
       return conn;
    }

    public static void initPostgres(Connection connection) {

        String path = "/src/main/java/Services/users.sql";

        ScriptRunner sr = new ScriptRunner(connection);
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("").getAbsolutePath().concat(path)));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        sr.runScript(reader);
    }

    public static int registerUser(Connection connection, JSONObject user) throws Exception {

            connection.setAutoCommit(true);
            CallableStatement st = connection.prepareCall("{? = call register_user(?, ?, ?, ?, ?, ?) }");

            st.setString(2, user.getString("userName"));
            st.setString(3, user.getString("firstName"));
            st.setString(4, user.getString("lastName"));
            st.setString(5, user.getString("email"));
            st.setString(6, user.getString("password"));
            st.setString(7, user.getString("role"));

            st.registerOutParameter(1, Types.OTHER);
            st.execute();

            ResultSet set = (ResultSet) st.getObject(1);
            if( set.next() ) {
                int id = set.getInt("userId");
                st.close();
                return id;
            } else {
                throw new Exception("SQL Error");
            }
    }

    public static void main(String[] args) throws SQLException {
        String url = "jdbc:postgresql://localhost/piazza";
        String user = "postgres";
        String password = "0000";

        Connection connection = connectPostgres(url, user, password);
        initPostgres(connection);

        JSONObject obj = new JSONObject();
        obj.put("userName", "newPaul");
        obj.put("firstName", "Paul");
        obj.put("lastName", "Ashraf");
        obj.put("email", "newPaul");
        obj.put("password", "0000");
        obj.put("role", "student");

        try {
            int id = registerUser(connection, obj);
            System.out.println(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
