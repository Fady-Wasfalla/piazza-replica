package Services;

import core.CommandDP;
import core.CommandsMap;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteCommand extends CommandDP {

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
            Files.deleteIfExists(Paths.get(fullPath.replace("src/main/java", "target/classes").replace(".java", ".class")));
            String key = queue + "/" + className.split("\\.java")[0];
            CommandsMap.cmdMap.remove(key);
            System.out.println("DELETE");
            CommandsMap.getAllClasses();

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
