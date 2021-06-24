package Database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class PostgreSQL {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:postgresql://" + dotenv.get("postgres_host", "localhost") + ":5432/piazza";
        url = "jdbc:postgresql://ec2-54-220-35-19.eu-west-1.compute.amazonaws.com:5432/d2spprpmp8ult?user=fbqvcficlrhimr&password=f989e5d9f18291eddb927bfbbbcf76121d597dddc5a3350f136b233e7c4af518";
        String user = "postgres";
        user = "fbqvcficlrhimr";
        String password = "f989e5d9f18291eddb927bfbbbcf76121d597dddc5a3350f136b233e7c4af518";

        try {
            Connection c = DriverManager.getConnection(url, user, password);
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT table_name\n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_schema = 'public'\n" +
                    "ORDER BY table_name;");
            while (rs.next()) {
                String table = rs.getString("table_name");
                System.out.println(table);
            }

//            String name = "Ahmed";
//            String query = "INSERT INTO users(name) VALUES(?)";
//            PreparedStatement pst = con.prepareStatement(query);
//            pst.setString(1, name);
//            pst.executeUpdate();
//            pst = con.prepareStatement("SELECT * FROM users");
//            ResultSet rs = pst.executeQuery();
//            while (rs.next()) {
//                System.out.print(rs.getInt(1));
//                System.out.print(": ");
//                System.out.println(rs.getString(2));
//            }
//            Statement stmt = c.createStatement();
//            String userTable =
//                    "CREATE TABLE IF NOT EXISTS users(" +
//                            "userId SERIAL PRIMARY KEY," +
//                            "userName CHAR(20) UNIQUE NOT NULL," +
//                            "firstName CHAR(20)  NOT NULL," +
//                            "lastName CHAR(20)  NOT NULL," +
//                            "email CHAR(100) UNIQUE NOT NULL," +
//                            "password TEXT NOT NULL," +
//                            "role CHAR(20)," +
//                            "createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW()," +
//                            "updatedAt TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
//                            ");";
//            stmt.executeUpdate(userTable);
//            String indexes =
//                    "CREATE INDEX IF NOT EXISTS userNameIndex ON users (userName);" +
//                            "CREATE INDEX IF NOT EXISTS emailIndex ON users (email);";
//            stmt.executeUpdate(indexes);
//            String timestampsFunction =
//                    "CREATE OR REPLACE FUNCTION trigger_set_timestamp() " +
//                            "RETURNS TRIGGER AS $$ " +
//                            "BEGIN " +
//                            "NEW.updatedAt = NOW(); " +
//                            "RETURN NEW; " +
//                            "END; " +
//                            "$$ LANGUAGE plpgsql; ";
//            stmt.executeUpdate(timestampsFunction);
//            String timestampsTrigger =
//                    "DROP TRIGGER IF EXISTS set_timestamp ON users; " +
//                            "CREATE TRIGGER set_timestamp " +
//                            "BEFORE INSERT OR UPDATE ON users " +
//                            "FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp(); ";
//            stmt.executeUpdate(timestampsTrigger);
//            String cryptoExtension = "CREATE EXTENSION IF NOT EXISTS  pgcrypto;";
//            stmt.executeUpdate(cryptoExtension);
//            String registerProcedure =
//                    "CREATE OR REPLACE PROCEDURE register(newUserName IN CHAR(20),newFirstName CHAR(20)," +
//                            "newLastName IN CHAR(20),newEmail IN CHAR(100),newPassword IN TEXT, role IN CHAR(20)," +
//                            "msg INOUT CHAR(20)) " +
//                            "LANGUAGE plpgsql AS $$ " +
//                            "DECLARE cntEmail INT := (SELECT count(*) FROM users WHERE email = newEmail); " +
//                            "DECLARE cntUsername INT := (SELECT count(*) FROM users WHERE userName = newUserName); " +
//                            "BEGIN " +
//                            "IF cntEmail = 0 AND cntUsername = 0 THEN " +
//                            "INSERT INTO users (userName, firstName, lastName, email, password, role) " +
//                            "VALUES(newUsername, newFirstName, newLastName, newEmail, crypt(newPassword, gen_salt('bf')), role); " +
//                            "msg := 'User registered successfully!'; " +
//                            "ELSE " +
//                            "IF cntEmail !=0 THEN " +
//                            "msg:= 'Email already exists'; " +
//                            "ELSE " +
//                            "msg:= 'Username already exists'; " +
//                            "END IF; " +
//                            "END IF; " +
//                            "END; $$;";
//            stmt.executeUpdate(registerProcedure);
//            String loginProcedure =
//                    "CREATE OR REPLACE PROCEDURE login(givenEmail IN CHAR(100), givenPassword IN CHAR(20), msg INOUT CHAR(20)) " +
//                            "LANGUAGE plpgsql AS $$ " +
//                            "DECLARE cnt INT := (SELECT count(*) FROM users " +
//                            "WHERE email = givenEmail AND password = crypt(givenPassword, password));\n" +
//                            "BEGIN " +
//                            "IF cnt = 0 THEN " +
//                            "msg := 'Incorrect email or password!'; " +
//                            "ELSE " +
//                            "msg := 'User logged in successfully!'; " +
//                            "END IF; " +
//                            "END; $$;";
//            stmt.executeUpdate(loginProcedure);
//            String deleteProcedure =
//                    "CREATE OR REPLACE PROCEDURE deleteUser(givenUserName IN CHAR(20), msg INOUT CHAR(20))" +
//                            "LANGUAGE plpgsql AS " +
//                            "$$ " +
//                            "DECLARE cnt INT := (SELECT count(*) FROM users " +
//                            "WHERE userName = givenUserName); " +
//                            "Begin " +
//                            "IF cnt != 0 THEN " +
//                            "DELETE FROM users WHERE userName = givenUserName; " +
//                            "msg := 'User is deleted successfully'; " +
//                            "ELSE " +
//                            "msg := 'User not found'; " +
//                            "END IF; " +
//                            "END; $$;";
//            stmt.executeUpdate(deleteProcedure);
//            String updateProcedure =
//                    "CREATE OR REPLACE PROCEDURE updateUser(givenUserName IN VARCHAR(20)," +
//                            "givenFirstName VARCHAR(20),givenLastName IN VARCHAR(20),givenEmail IN VARCHAR(100)," +
//                            "givenPassword IN TEXT, givenRole IN VARCHAR(20),msg INOUT TEXT)" +
//                            "LANGUAGE plpgsql AS $$ " +
//                            "DECLARE cnt INT := (SELECT count(*) FROM users " +
//                            "WHERE userName = givenUserName); " +
//                            "BEGIN " +
//                            "IF cnt = 0 THEN " +
//                            "msg := 'User not found'; " +
//                            "ELSE " +
//                            "UPDATE users SET userName = COALESCE(givenUserName, userName),firstName = COALESCE(givenFirstName, firstName)," +
//                            "lastName = COALESCE(givenLastName, lastName),email = COALESCE(givenEmail, email), " +
//                            "password = COALESCE(givenPassword, password),role = COALESCE(role, givenRole); " +
//                            "msg := 'User is updated successfully'; " +
//                            "END IF; " +
//                            "End; $$;";
//            stmt.executeUpdate(updateProcedure);
//            stmt.close();
//            c.close();

        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
}
