package core;

import Services.AddCommand;
import Services.DeleteCommand;
import Services.UpdateCommand;
import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.UserCommands.RegisterUserCommand;
import core.commands.UserCommands.SignupCommand;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap implements Serializable {
    public static HashMap<String, Class<?>> cmdMap ;

    public CommandsMap(){

    }

    public static void instantiate() {
        cmdMap = new HashMap<>();
        cmdMap.put("user/SignupCommand",SignupCommand.class);
        cmdMap.put("course/CreateCourseCommand", CreateCourseCommand.class);
        cmdMap.put("user/RegisterUserCommand", RegisterUserCommand.class);
        cmdMap.put("command/AddCommand", AddCommand.class);
        cmdMap.put("command/UpdateCommand", UpdateCommand.class);
        cmdMap.put("command/DeleteCommand", DeleteCommand.class);
//        saveStatus(cmdMap,"src/main/java/Services/cmdMap.ser");
    }

    public static void saveStatus(Serializable object,String path){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in "+path);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static HashMap loadStatus(String path){
        HashMap h = null;
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            h = (HashMap) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();

        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();

        }
        return h;
    }

    public static Class<?> queryClass(String cmd) {
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        return cmdMap.get(cmd);
    }

    public static void replace(String key , Class cls){
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        cmdMap.put(key, cls);
//        saveStatus(cmdMap,"src/main/java/Services/cmdMap.ser");
        System.out.println("replaced");
    }

    public static void getAllClasses(){
        System.out.println("------------------------------");
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        cmdMap.forEach( (k, v) -> System.out.println(k+" ==> "+v));
    }


    public static void main(String[]args){
        CommandsMap.instantiate();
//        HashMap x = new HashMap<>();
//        x = loadStatus("src/main/java/Services/cmdMap.ser");
//        System.out.println(x.keySet());
//        CommandsMap cmd = new CommandsMap();
//        cmd.instantiate();
//        saveStatus(cmd,"src/main/java/Services/cmdMap.ser");
//        Object back = loadStatus("src/main/java/Services/cmdMap.dat");
//        System.out.println(back);
//        back.getAllClasses();
    }
}
