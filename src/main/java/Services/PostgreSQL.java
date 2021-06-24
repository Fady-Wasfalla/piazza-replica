package Services;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class PostgreSQL {

    public static final String dbName = "piazza";
    private static PoolingDataSource<PoolableConnection> postgresPool;
    private static PoolingDriver dbDriver;

    public static void initPostgres(int maxConnection) throws SQLException {

        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        if(maxConnection == -1){
            maxConnection = 29;
        }

        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("POSTGRES_URL");
        String user = dotenv.get("POSTGRES_USER");
        String password = dotenv.get("POSTGRES_PASSWORD");
        String initPool = dotenv.get("POSTGRES_POOL_INIT_CONNECTIONS");
        String maxPool = maxConnection+"";


        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("initialSize", initPool);
        props.setProperty("maxActive", maxPool);

        DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, props);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setPoolStatements(true);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(Integer.parseInt(initPool));
        poolConfig.setMaxTotal(Integer.parseInt(maxPool));

        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory, poolConfig);
        poolableConnectionFactory.setPool(connectionPool);


        dbDriver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

        dbDriver.registerPool(dbName, connectionPool);

        postgresPool = new PoolingDataSource<PoolableConnection>(connectionPool);

        Connection connection = postgresPool.getConnection();
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

    public static void closeDBPool()throws SQLException{
        dbDriver.closePool(dbName);
    }

    public static int registerUser(JSONObject user) throws SQLException {

        Connection connection = postgresPool.getConnection();

        connection.setAutoCommit(false);
        CallableStatement st = connection.prepareCall("{ ? = call register_user(?, ?, ?, ?, ?, ?) }");
        st.setPoolable(true);

        st.setString(2, user.getString("userName"));
        st.setString(3, user.getString("firstName"));
        st.setString(4, user.getString("lastName"));
        st.setString(5, user.getString("email"));
        st.setString(6, user.getString("password"));
        st.setString(7, user.getString("role"));

        st.registerOutParameter(1, Types.OTHER);
        st.execute();

        ResultSet set = (ResultSet) st.getObject(1);
        if (set.next()) {
            int id = set.getInt("userId");
            set.close();
            st.close();
            connection.commit();
            return id;
        } else {
            set.close();
            st.close();
            throw new SQLException();
        }
    }

    public static boolean isUser(String email, String password) throws SQLException {
        Connection connection = postgresPool.getConnection();

        connection.setAutoCommit(false);
        CallableStatement st = connection.prepareCall("{ ? = call login_user(?, ?) }");
        st.setPoolable(true);

        st.setString(2, email);
        st.setString(3, password);

        st.registerOutParameter(1, Types.OTHER);
        st.execute();

        ResultSet set = (ResultSet) st.getObject(1);
        if (set.next()) {
            set.close();
            st.close();
            return true;
        } else {
            set.close();
            st.close();
            return false;
        }
    }

    public static void deleteUser(String username) throws SQLException {
        Connection connection = postgresPool.getConnection();

        connection.setAutoCommit(true);
        CallableStatement st = connection.prepareCall("{ call delete_user(?) }");
        st.setPoolable(true);

        st.setString(1, username);

        st.execute();
        st.close();
    }

    public static void updateUser(JSONObject user, String username) throws SQLException {
        Connection connection = postgresPool.getConnection();

        connection.setAutoCommit(true);
        CallableStatement st = connection.prepareCall("{ call update_user(?, ?, ?, ?) }");
        st.setPoolable(true);

        if (user.has("firstName")) st.setString(1, user.getString("firstName"));
        else st.setString(1, null);

        if (user.has("lastName")) st.setString(2, user.getString("lastName"));
        else st.setString(2, null);

        if (user.has("password")) st.setString(3, user.getString("password"));
        else st.setString(3, null);

        st.setString(4, username);

        st.execute();
        st.close();
    }


    public static void main(String[] args) throws SQLException {
        initPostgres(-1);

//        JSONObject obj = new JSONObject();
//        obj.put("userName", UUID.randomUUID().toString().substring(0, 20));
//        obj.put("firstName", "tttttttttt");
//        obj.put("lastName", "tttttttt");
//        obj.put("email", UUID.randomUUID().toString().substring(0, 20));
//        obj.put("password", "0000");
//        obj.put("role", "student");
//
//        try {
//            int id = registerUser(obj);
//            System.out.println(id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        deleteUser("cc");

        JSONObject obj1 = new JSONObject();
        obj1.put("firstName", "test_update");
        updateUser(obj1, "paulpaul");

    }
}
