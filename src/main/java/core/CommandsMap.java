package core;

import DynamicClasses.AddCommand;
import DynamicClasses.DeleteCommand;
import DynamicClasses.UpdateCommand;
import core.commands.Controller.ContinueCommand;
import core.commands.Controller.FreezeCommand;
import core.commands.Controller.SetMaxDBConnectionCountCommand;
import core.commands.Controller.SetMaxThreadCountCommand;
import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.CourseCommands.DeleteCourseCommand;
import core.commands.CourseCommands.ViewCourseCommand;
import core.commands.CourseCommands.ViewUserCoursesCommand;
import core.commands.PollCommands.*;
import core.commands.NotificationCommands.NotifyAllStudentsCommand;
import core.commands.NotificationCommands.NotifyAllInstructorsCommand;
import core.commands.NotificationCommands.NotifyStudentCommand;
import core.commands.PollCommands.CreatePollCommand;
import core.commands.PollCommands.DeletePollCommand;
import core.commands.PollCommands.ViewAllPollsCommand;
import core.commands.QuestionCommands.AnswerQuestionCommand;
import core.commands.QuestionCommands.CreateQuestionCommand;
import core.commands.QuestionCommands.DeleteCourseQuestionsCommand;
import core.commands.QuestionCommands.SearchQuestionsCommand;
import core.commands.UserCommands.DeleteCourseRegistersCommand;
import core.commands.UserCommands.LogInCommand;
import core.commands.UserCommands.RegisterUserCommand;
import core.commands.UserCommands.SignUpCommand;
import core.commands.QuestionCommands.*;
import core.commands.UserCommands.*;

import java.io.*;
import java.util.HashMap;

public class CommandsMap implements Serializable {
    public static HashMap<String, Class<?>> cmdMap;

    public CommandsMap() {

    }

    public static void instantiate() {
        cmdMap = new HashMap<>();
        cmdMap.put("user/SignupCommand", SignUpCommand.class);
        cmdMap.put("user/RegisterUserCommand", RegisterUserCommand.class);
        cmdMap.put("user/DeleteCourseRegistersCommand", DeleteCourseRegistersCommand.class);
        cmdMap.put("user/ViewUserCoursesCommand", ViewUserCoursesCommand.class);
        cmdMap.put("user/SetNotificationTokenCommand", SetNotificationTokenCommand.class);
        cmdMap.put("user/BanStudentCommand", BanStudentCommand.class);
        cmdMap.put("user/SetNotificationTokenCommand", SetNotificationTokenCommand.class);
        cmdMap.put("user/BanStudentCommand", BanStudentCommand.class);
        cmdMap.put("user/SignUpCommand", SignUpCommand.class);
        cmdMap.put("user/LogInCommand", LogInCommand.class);
        cmdMap.put("user/GetUserByEmailCommand", GetUserByEmailCommand.class);

        cmdMap.put("user/DeleteCourseRegistersCommand",DeleteCourseRegistersCommand.class);
        cmdMap.put("user/RerportUserCommand",RerportUserCommand.class);
        cmdMap.put("user/DeleteUserCommand",DeleteUserCommand.class);
        
        cmdMap.put("question/CreateQuestionCommand", CreateQuestionCommand.class);
        cmdMap.put("question/ViewAllQuestionsCommand", ViewAllQuestionsCommand.class);
        cmdMap.put("question/DeleteCourseQuestionsCommand", DeleteCourseQuestionsCommand.class);
        cmdMap.put("question/SearchQuestionsCommand", SearchQuestionsCommand.class);
        cmdMap.put("question/AnswerQuestionCommand", AnswerQuestionCommand.class);
        cmdMap.put("question/EndorseQuestionCommand", EndorseQuestionCommand.class);
        cmdMap.put("question/ViewAllQuestionsCommand", ViewAllQuestionsCommand.class);
        cmdMap.put("question/EndorseAnswerCommand", EndorseAnswerCommand.class);
        cmdMap.put("question/SuggestQuestionsCommand",SuggestQuestionsCommand.class);
        cmdMap.put("question/DeleteAnswerCommand",DeleteAnswerCommand.class);
        
        cmdMap.put("course/CreateCourseCommand", CreateCourseCommand.class);
        cmdMap.put("course/ViewCourseCommand", ViewCourseCommand.class);
        cmdMap.put("course/DeleteCourseCommand", DeleteCourseCommand.class);
        cmdMap.put("course/ViewUserCoursesCommand",ViewUserCoursesCommand.class);

        cmdMap.put("poll/CreatePollCommand", CreatePollCommand.class);
        cmdMap.put("poll/ViewAllPollsCommand", ViewAllPollsCommand.class);
        cmdMap.put("poll/DeletePollCommand", DeletePollCommand.class);
        cmdMap.put("poll/UpdatePollCommand", UpdatePollCommand.class);
        cmdMap.put("poll/AnswerPollCommand", AnswerPollCommand.class);

        cmdMap.put("controller/FreezeCommand", FreezeCommand.class);
        cmdMap.put("controller/ContinueCommand", ContinueCommand.class);
        cmdMap.put("controller/SetMaxThreadCount", SetMaxThreadCountCommand.class);
        cmdMap.put("controller/SetMaxDBConnectionCountCommand", SetMaxDBConnectionCountCommand.class);

        cmdMap.put("notification/NotifyAllStudentsCommand", NotifyAllStudentsCommand.class);
        cmdMap.put("notification/NotifyAllInstructorsCommand", NotifyAllInstructorsCommand.class);
        cmdMap.put("notification/NotifyStudentCommand", NotifyStudentCommand.class);

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
    }

    public static void remove(String key) {
        cmdMap.remove(key);
    }

    public static void getAllClasses() {
//        cmdMap = loadStatus("src/main/java/Services/cmdMap.ser");
        cmdMap.forEach((k, v) -> System.out.println(k + " ==> " + v));
    }


    public static void main(String[] args) {
        CommandsMap.instantiate();
    }
}
