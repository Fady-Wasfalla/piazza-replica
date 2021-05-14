package Services;

import java.sql.*;

public class PostgreSQL {
    Connection db_connection = null;
    CallableStatement delete_user = null;
    CallableStatement register_user = null;
    CallableStatement login_user = null;
    CallableStatement update_user = null;

    public  PostgreSQL(String url, String user, String password){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
       db_connection = conn;
    }
    public void create_user_table() throws SQLException{
        Statement stmt = db_connection.createStatement();
        String userTable =
                "CREATE TABLE IF NOT EXISTS users(" +
                        "userId SERIAL PRIMARY KEY," +
                        "userName CHAR(20) UNIQUE NOT NULL," +
                        "firstName CHAR(20)  NOT NULL," +
                        "lastName CHAR(20)  NOT NULL," +
                        "email CHAR(100) UNIQUE NOT NULL," +
                        "password TEXT NOT NULL," +
                        "role CHAR(20)," +
                        "createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()," +
                        "updatedAt TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                        ");";
        String indexes =
                "CREATE INDEX IF NOT EXISTS userNameIndex ON users (userName);" +
                        "CREATE INDEX IF NOT EXISTS emailIndex ON users (email);";
        stmt.executeUpdate(userTable);
        stmt.executeUpdate(indexes);
        String timestampsFunction =
                "CREATE OR REPLACE FUNCTION trigger_set_timestamp() " +
                        "RETURNS TRIGGER AS $$ " +
                        "BEGIN " +
                        "NEW.updatedAt = NOW(); " +
                        "RETURN NEW; " +
                        "END; " +
                        "$$ LANGUAGE plpgsql; ";
        stmt.executeUpdate(timestampsFunction);
        String timestampsTrigger =
                "DROP TRIGGER IF EXISTS set_timestamp ON users; " +
                        "CREATE TRIGGER set_timestamp " +
                        "BEFORE INSERT OR UPDATE ON users " +
                        "FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp(); ";
        stmt.executeUpdate(timestampsTrigger);
        String cryptoExtension = "CREATE EXTENSION IF NOT EXISTS  pgcrypto;";
        stmt.executeUpdate(cryptoExtension);
        String registerProcedure =
                "CREATE OR REPLACE PROCEDURE register(newUserName IN CHAR(20),newFirstName CHAR(20)," +
                        "newLastName IN CHAR(20),newEmail IN CHAR(100),newPassword IN TEXT, role IN CHAR(20)," +
                        "msg INOUT CHAR(20)) " +
                        "LANGUAGE plpgsql AS $$ " +
                        "DECLARE cntEmail INT := (SELECT count(*) FROM users WHERE email = newEmail); " +
                        "DECLARE cntUsername INT := (SELECT count(*) FROM users WHERE userName = newUserName); " +
                        "BEGIN " +
                        "IF cntEmail = 0 AND cntUsername = 0 THEN " +
                        "INSERT INTO users (userName, firstName, lastName, email, password, role) " +
                        "VALUES(newUsername, newFirstName, newLastName, newEmail, crypt(newPassword, gen_salt('bf')), role); " +
                        "msg := 'User registered successfully!'; " +
                        "ELSE " +
                        "IF cntEmail !=0 THEN " +
                        "msg:= 'Email already exists'; " +
                        "ELSE " +
                        "msg:= 'Username already exists'; " +
                        "END IF; " +
                        "END IF; " +
                        "END; $$;";
        stmt.executeUpdate(registerProcedure);
        String loginProcedure =
                "CREATE OR REPLACE PROCEDURE login(givenEmail IN CHAR(100), givenPassword IN CHAR(20), msg INOUT CHAR(20)) " +
                        "LANGUAGE plpgsql AS $$ " +
                        "DECLARE cnt INT := (SELECT count(*) FROM users " +
                        "WHERE email = givenEmail AND password = crypt(givenPassword, password));\n" +
                        "BEGIN " +
                        "IF cnt = 0 THEN " +
                        "msg := 'Incorrect email or password!'; " +
                        "ELSE " +
                        "msg := 'User logged in successfully!'; " +
                        "END IF; " +
                        "END; $$;";
        stmt.executeUpdate(loginProcedure);
        String deleteProcedure =
                "CREATE OR REPLACE PROCEDURE delete_user(givenUserName IN CHAR(20), msg INOUT CHAR(20))" +
                        "LANGUAGE plpgsql AS " +
                        "$$ " +
                        "DECLARE cnt INT := (SELECT count(*) FROM users " +
                        "WHERE userName = givenUserName); " +
                        "Begin " +
                        "IF cnt != 0 THEN " +
                        "DELETE FROM users WHERE userName = givenUserName; " +
                        "msg := 'User is deleted successfully'; " +
                        "ELSE " +
                        "msg := 'User not found'; " +
                        "END IF; " +
                        "END; $$;";
        stmt.executeUpdate(deleteProcedure);
        String updateProcedure =
                "CREATE OR REPLACE PROCEDURE update_user(givenUserName IN VARCHAR(20)," +
                        "givenFirstName VARCHAR(20),givenLastName IN VARCHAR(20),givenEmail IN VARCHAR(100)," +
                        "givenPassword IN TEXT, givenRole IN VARCHAR(20),msg INOUT TEXT)" +
                        "LANGUAGE plpgsql AS $$ " +
                        "DECLARE cnt INT := (SELECT count(*) FROM users " +
                        "WHERE userName = givenUserName); " +
                        "BEGIN " +
                        "IF cnt = 0 THEN " +
                        "msg := 'User not found'; " +
                        "ELSE " +
                        "UPDATE users SET userName = COALESCE(givenUserName, userName),firstName = COALESCE(givenFirstName, firstName)," +
                        "lastName = COALESCE(givenLastName, lastName),email = COALESCE(givenEmail, email), " +
                        "password = COALESCE(givenPassword, password),role = COALESCE(role, givenRole); " +
                        "msg := 'User is updated successfully'; " +
                        "END IF; " +
                        "End; $$;";
        stmt.executeUpdate(updateProcedure);
        String testProcedure = "CREATE OR REPLACE PROCEDURE helloworld() " +
                "LANGUAGE plpgsql AS " +
                "$$" +
                "BEGIN " +
                "raise info 'Hello World';" +
                "END;" +
                "$$;";
        stmt.executeUpdate(testProcedure);
        stmt.close();
        System.out.println("User Table created");
        delete_user =db_connection.prepareCall("call delete_user(?, ?)");
        System.out.println("Delete user procedure created");
        register_user = db_connection.prepareCall("call register(?, ?, ?, ?, ?, ?, ?)");
        System.out.println("Register user procedure created");
        login_user = db_connection.prepareCall("call login(?, ?, ?)");
        System.out.println("Login user procedure created");
        update_user = db_connection.prepareCall("call update_user(?, ?, ?, ?, ?, ?, ?)");
        System.out.println("Update user procedure created");
    }
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:postgresql://localhost/postgres";
        String user = "postgres";
        String password = "";
        PostgreSQL postgres = new PostgreSQL(url, user, password);
        postgres.create_user_table();
//          Creating a User
        postgres.register_user.setString(1, "Minaa");
        postgres.register_user.setString(2, "Mina");
        postgres.register_user.setString(3, "Mina@gmail.com");
        postgres.register_user.setString(4, "Mina");
        postgres.register_user.setString(5, "Mina");
        postgres.register_user.setString(6, "Mina");
        postgres.register_user.setString(7, "");
        postgres.register_user.registerOutParameter(7, Types.CHAR);
        postgres.register_user.execute();
        String result = postgres.register_user.getString(7);
        System.out.println(result);
//        Deleting a User
        postgres.delete_user.setString(1, "Mina");
        postgres.delete_user.setString(2, "");;
        postgres.delete_user.registerOutParameter(2, Types.CHAR);
        postgres.delete_user.execute();
        System.out.println(postgres.delete_user.getString(2));
//        Login
        postgres.login_user.setString(1,"Mina");
        postgres.login_user.setString(2,"Mina");
        postgres.login_user.setString(3, "");;
        postgres.login_user.registerOutParameter(3, Types.CHAR);
        postgres.login_user.execute();
        System.out.println(postgres.login_user.getString(3));
//        Update User
        postgres.update_user.setString(1, "Minaa");
        postgres.update_user.setString(2, "Minaaaa");
        postgres.update_user.setString(3, "");
        postgres.update_user.setString(4, "");
        postgres.update_user.setString(5, "");
        postgres.update_user.setString(6, "");
        postgres.update_user.setString(7, "");
        postgres.update_user.registerOutParameter(7, Types.VARCHAR);
        postgres.update_user.execute();
        System.out.println(postgres.update_user.getString(7));


    }
}
