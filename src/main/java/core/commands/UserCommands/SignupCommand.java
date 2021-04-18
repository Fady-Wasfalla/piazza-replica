package core.commands.UserCommands;
import core.Command;
import org.json.JSONObject;

import java.sql.SQLException;

public class SignupCommand extends Command {


    @Override
    public void execute() {
        System.out.println(this.data.toString());

        if(this.data.getString("firstName")!="" && this.data.getString("lastName")!="") {
            try {
                ((UserDAL)dal).createUser(this.data);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.out.println(throwables);
            }
        }
        else {
            System.out.println("else");
        }
    }

    @Override
    public void setData(JSONObject data, Object dal) {
        this.data = data;
        this.dal = dal;

    }
}
