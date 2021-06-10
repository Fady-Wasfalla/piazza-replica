package Services;

import RabbitMQ.ConsumerMQ;
import core.CommandDP;
import core.CommandsMap;
import org.json.JSONObject;


import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DeleteCommand extends CommandDP {

    public CommandsMap cmdMap;

    public void setCmd(CommandsMap cmdMap){
        this.cmdMap = cmdMap;
    }

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        if(!this.data.keySet().contains("sourceCode") || !(this.data.get("sourceCode") instanceof String) || !this.data.keySet().contains("filePath") || !(this.data.get("filePath") instanceof String) ) {
            result.put("error", "invalid request");
            return result;
        }

        String filePath = (String) this.data.get("filePath");
        String sourceCode = (String) this.data.get("sourceCode");
        String className = this.data.get("className")+".java";
        String parentPackage = (String) this.data.get("parentPackage");
        String queue = (String) this.data.get("queue");

        try {
            String fullPath = filePath + "/" + parentPackage + "/" + className;
            Files.deleteIfExists(Paths.get(fullPath));
            Files.deleteIfExists(Paths.get(String.valueOf(fullPath).replace("src/main/java", "target/classes").replace(".java", ".class")));
            String key = queue+"/"+className.split("\\.java")[0];
            cmdMap.cmdMap.remove(key);
            System.out.println("DELETE");
            cmdMap.getAllClasses();

        }catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }
}
