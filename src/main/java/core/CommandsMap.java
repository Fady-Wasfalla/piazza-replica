package core;

import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.CourseCommands.DeleteCourseCommand;
import core.commands.CourseCommands.ViewCourseCommand;
import core.commands.CourseCommands.ViewUserCoursesCommand;
import core.commands.PollCommands.CreatePollCommand;
import core.commands.PollCommands.DeleteCoursePollsCommand;
import core.commands.PollCommands.ViewAllPollsCommand;
import core.commands.QuestionCommands.*;
import core.commands.UserCommands.DeleteCourseRegistersCommand;
import core.commands.UserCommands.RegisterUserCommand;
import core.commands.UserCommands.SetNotificationTokenCommand;
import core.commands.UserCommands.SignupCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("user/SignupCommand",SignupCommand.class);
        cmdMap.put("user/RegisterUserCommand", RegisterUserCommand.class);
        cmdMap.put("user/DeleteCourseRegistersCommand", DeleteCourseRegistersCommand.class);
        cmdMap.put("user/ViewUserCoursesCommand", ViewUserCoursesCommand.class);
        cmdMap.put("user/SetNotificationTokenCommand", SetNotificationTokenCommand.class);

        cmdMap.put("question/CreateQuestionCommand", CreateQuestionCommand.class);
        cmdMap.put("question/ViewAllQuestionsCommand", ViewAllQuestionsCommand.class);
        cmdMap.put("question/DeleteCourseQuestionsCommand", DeleteCourseQuestionsCommand.class);
        cmdMap.put("question/SearchQuestionsCommand", SearchQuestionsCommand.class);
        cmdMap.put("question/AnswerQuestionCommand", AnswerQuestionCommand.class);

        cmdMap.put("course/CreateCourseCommand", CreateCourseCommand.class);
        cmdMap.put("course/ViewCourseCommand", ViewCourseCommand.class);
        cmdMap.put("course/DeleteCourseCommand", DeleteCourseCommand.class);

        cmdMap.put("poll/CreatePollCommand", CreatePollCommand.class);
        cmdMap.put("poll/DeleteCoursePollsCommand", DeleteCoursePollsCommand.class);
        cmdMap.put("poll/ViewAllPollsCommand", ViewAllPollsCommand.class);
//        cmdMap.put("poll/DeleteCoursePollsCommand", ViewAllPollsCommand.class);




    }

        public static Class<?> queryClass(String cmd) {
        return cmdMap.get(cmd);
    }
    public static void replace(String key , Class cls){
        cmdMap.put(key, cls);
        System.out.println("replaced");
    }
    public static String map (String key){
        for (Map.Entry<String, Class<?> > entry : cmdMap.entrySet()) {
            if(entry.getValue().toString().equals(key))
                return entry.getKey();
        }
        return "error";
    }
    public static Class<?> getClass (String key){
        return cmdMap.get(key);
    }
}
