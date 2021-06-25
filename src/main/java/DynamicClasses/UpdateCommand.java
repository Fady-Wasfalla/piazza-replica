package DynamicClasses;

import core.CommandsMap;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class UpdateCommand extends CommandClassDP {

    public CommandsMap cmdMap;

    public void setCmd(CommandsMap cmdMap) {
        this.cmdMap = cmdMap;
    }

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        Boolean status = true;
        QueryStringDecoder decoder = new QueryStringDecoder(String.valueOf(this.url));
        String serviceName = decoder.parameters().get("service").get(0);
        String className = decoder.parameters().get("className").get(0);
        String service = serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1).toLowerCase() + "Commands";

        System.out.println("ENTER ADD");
        //java file
        String filePath1 = "src/main/java";
        File root1 = new File(filePath1);
        File sourceFile1 = new File(root1, "core/commands/" + service + "/" + className + ".java");
        try {
            Files.write(sourceFile1.toPath(), StandardCharsets.UTF_8.decode(this.javaFile).toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            status=false;
            result.append("Error",e.toString());
            e.printStackTrace();
        }
        System.out.println("ADD NEW JAVA FILE");

        String filePath = "target/classes";
        File root = new File(filePath);
        File sourceFile = new File(root, "core/commands/" + service + "/" + className + ".class");
        FileChannel wChannel = null;
        try {
            wChannel = new FileOutputStream(sourceFile, false).getChannel();
            wChannel.write(this.classFile);
            wChannel.close();
        } catch (FileNotFoundException e) {
            status=false;
            result.append("Error",e.toString());
            e.printStackTrace();
        }catch (IOException e) {
            status=false;
            result.append("Error",e.toString());
            e.printStackTrace();
        }

        Class<?> newClass = null;
        try {
            newClass = Class.forName("core.commands."+service+"."+className);
        } catch (ClassNotFoundException e) {
            status=false;
            result.append("Error",e.toString());
            e.printStackTrace();
        }
        String key = serviceName+"/"+className.split("\\.java")[0];
        cmdMap.replace(key,newClass);
        cmdMap.getAllClasses();
        System.out.println("CLASS FILE");
        if( status ) {
            result.append("Message","Class added successfully");
        }
        return result;
    }
}
