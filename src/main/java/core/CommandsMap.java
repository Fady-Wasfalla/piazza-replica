package core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import core.commands.UserCommads.*;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("signUp",SignupCommand.class);
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
