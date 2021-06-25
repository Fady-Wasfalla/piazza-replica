package core;

import DynamicClasses.AddCommand;
import DynamicClasses.DeleteCommand;
import DynamicClasses.UpdateCommand;
import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.CourseCommands.DeleteCourseCommand;
import core.commands.CourseCommands.ViewCourseCommand;
import core.commands.CourseCommands.ViewUserCoursesCommand;
import core.commands.PollCommands.CreatePollCommand;
import core.commands.PollCommands.DeleteCoursePollsCommand;
import core.commands.PollCommands.ViewAllPollsCommand;
import core.commands.QuestionCommands.AnswerQuestionCommand;
import core.commands.QuestionCommands.CreateQuestionCommand;
import core.commands.QuestionCommands.DeleteCourseQuestionsCommand;
import core.commands.QuestionCommands.SearchQuestionsCommand;
import core.commands.UserCommands.DeleteCourseRegistersCommand;
import core.commands.UserCommands.RegisterUserCommand;
import core.commands.UserCommands.SignupCommand;

import java.io.*;
import java.util.HashMap;

public class CommandsMap implements Serializable {
    public static HashMap<String, Class<?>> cmdMap;

    public CommandsMap() {

    }

    public static void instantiate() {
        cmdMap = new HashMap<>();
        cmdMap.put("user/SignupCommand", SignupCommand.class);
        cmdMap.put("user/RegisterUserCommand", RegisterUserCommand.class);
        cmdMap.put("user/DeleteCourseRegistersCommand", DeleteCourseRegistersCommand.class);
        cmdMap.put("user/ViewUserCoursesCommand", ViewUserCoursesCommand.class);

        cmdMap.put("question/CreateQuestionCommand", CreateQuestionCommand.class);
        cmdMap.put("question/DeleteCourseQuestionsCommand", DeleteCourseQuestionsCommand.class);
        cmdMap.put("question/SearchQuestionsCommand", SearchQuestionsCommand.class);
        cmdMap.put("question/AnswerQuestionCommand", AnswerQuestionCommand.class);

        cmdMap.put("course/CreateCourseCommand", CreateCourseCommand.class);
        cmdMap.put("course/ViewCourseCommand", ViewCourseCommand.class);
        cmdMap.put("course/DeleteCourseCommand", DeleteCourseCommand.class);

        cmdMap.put("poll/CreatePollCommand", CreatePollCommand.class);
        cmdMap.put("poll/DeleteCoursePollsCommand", DeleteCoursePollsCommand.class);
        cmdMap.put("poll/ViewAllPollsCommand", ViewAllPollsCommand.class);

        cmdMap.put("DynamicClasses/AddCommand", AddCommand.class);
        cmdMap.put("DynamicClasses/UpdateCommand", UpdateCommand.class);
        cmdMap.put("DynamicClasses/DeleteCommand", DeleteCommand.class);

    }

    public static void saveStatus(Serializable object, String path) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + path);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static HashMap loadStatus(String path) {
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

    public static void replace(String key, Class cls) {
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        cmdMap.put(key, cls);
//        saveStatus(cmdMap,"src/main/java/Services/cmdMap.ser");
        System.out.println("replaced");
    }

    public static void remove(String key) {
        cmdMap.remove(key);
        System.out.println("removed");
    }

    public static void getAllClasses() {
        System.out.println("---------Command Map Classes---------");
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        cmdMap.forEach((k, v) -> System.out.println(k + " ==> " + v));
        System.out.println("-------------------------------------");
    }


    public static void main(String[] args) {
        CommandsMap.instantiate();
    }
}
