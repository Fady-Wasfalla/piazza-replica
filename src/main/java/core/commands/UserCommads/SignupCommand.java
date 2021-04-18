package core.commands.UserCommads;
import core.Command;
import core.commands.UserCommads.UserDAL;

import java.sql.SQLException;

public class SignupCommand extends Command {

    @Override
    public void execute() {
        System.out.println(this.data.toString());
        if(this.data.getString("firstName")!="" && this.data.getString("lastName")!=""){
            UserDAL newDAL = new UserDAL();
            newDAL.connectPostgreSQL();
            try {
                newDAL.createUser(this.data);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
