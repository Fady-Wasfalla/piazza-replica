package Services;

import core.CommandDP;
import org.json.JSONObject;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class AddCommand extends CommandDP {


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

        try{
            File root = new File(filePath);
            File sourceFile = new File(root, "UserCommands/"+className);
            sourceFile.getParentFile().mkdirs();
            Files.write(sourceFile.toPath(), sourceCode.getBytes(StandardCharsets.UTF_8));
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, sourceFile.getPath());
        }catch (Exception e){
            System.out.println(e.toString());
        }

        return null;
    }
}
