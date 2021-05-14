package core;

import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.QuestionCommands.CreateQuestionCommand;
import core.commands.QuestionCommands.ViewAllQuestionsCommand;
import core.commands.UserCommands.RegisterUserCommand;
import core.commands.UserCommands.SignupCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("user/SignupCommand",SignupCommand.class);
        cmdMap.put("question/ViewAllQuestionsCommand", ViewAllQuestionsCommand.class);
        cmdMap.put("course/CreateCourseCommand", CreateCourseCommand.class);
        cmdMap.put("user/RegisterUserCommand", RegisterUserCommand.class);
        cmdMap.put("question/CreateQuestionCommand", CreateQuestionCommand.class);
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
