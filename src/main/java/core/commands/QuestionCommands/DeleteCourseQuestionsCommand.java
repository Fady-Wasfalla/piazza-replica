package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.DeleteResult;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

public class DeleteCourseQuestionsCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {"courseId"};

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String courseId = data.getString("courseId");

        DeleteResult deletedQuestions = mongoDB.deleteMany(Collections.question, new Document("courseId", courseId));

        long questionDeletedCount = deletedQuestions.getDeletedCount();
        result.put("questionDeletedCount", questionDeletedCount);

        schema = null;
        courseId = null;
        deletedQuestions = null;

        return result;
    }
}
