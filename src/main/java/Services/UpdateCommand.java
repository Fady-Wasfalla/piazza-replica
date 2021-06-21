package Services;

import core.CommandDP;
import core.CommandsMap;
import org.json.JSONObject;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdateCommand extends CommandDP {

    public CommandsMap cmdMap;

    public void setCmd(CommandsMap cmdMap) {
        this.cmdMap = cmdMap;
    }

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        if (!this.data.keySet().contains("sourceCode") || !(this.data.get("sourceCode") instanceof String) || !this.data.keySet().contains("filePath") || !(this.data.get("filePath") instanceof String)) {
            result.put("error", "invalid request");
            return result;
        }

        String filePath = (String) this.data.get("filePath");
        String sourceCode = (String) this.data.get("sourceCode");
        String className = this.data.get("className") + ".java";
        String parentPackage = (String) this.data.get("parentPackage");
        String queue = (String) this.data.get("queue");

        try {
            String fullPath = filePath + "/" + parentPackage + "/" + className;
            Files.deleteIfExists(Paths.get(fullPath));
            Files.deleteIfExists(Paths.get(String.valueOf(fullPath).replace("src/main/java", "target/classes").replace(".java", ".class")));
            File root = new File(filePath);
            File sourceFile = new File(root, parentPackage + "/" + className);
            sourceFile.getParentFile().mkdirs();
            Files.write(sourceFile.toPath(), sourceCode.getBytes(StandardCharsets.UTF_8));
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int rr = compiler.run(System.in, System.out, System.err, sourceFile.getPath());
            System.out.println(rr + " XX: " + String.valueOf(sourceFile));

            try {
                Path temp = Files.move(Paths.get(String.valueOf(sourceFile).replace(".java", ".class")),
                        Paths.get(String.valueOf(sourceFile).replace("src/main/java", "target/classes").replace(".java", ".class")));
                if (temp != null) {
                    System.out.println("File renamed and moved successfully");
                } else {
                    System.out.println("Failed to move the file");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            String clsN = className.split("\\.java")[0];
            System.out.println((clsN));

            URLClassLoader classLoader = null;
            try {
                classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Class<?> newClass = Class.forName("Services.ViewAllQuestionsCommand", true, classLoader);
            String key = queue + "/" + className.split("\\.java")[0];
            cmdMap.cmdMap.remove(key);
            cmdMap.replace(key, newClass);
            System.out.println("UPDATE");
            cmdMap.getAllClasses();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }
}
