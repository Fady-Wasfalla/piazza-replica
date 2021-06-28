package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class SuggestQuestionsCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "courseId",
                "query",
                "sort",
                "skip",
                "limit"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String query = data.getString("query");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if (sort == null) {
            sort = "title";
        }

        query = query.toLowerCase(Locale.ROOT);
        String[] keywords = query.split(" ");

        String expression = "";
        for (int i = 0; i < keywords.length; i++) {
            expression += ".*";
            expression += keywords[i];
            expression += ".*";
            if (!(i == keywords.length - 1)) {
                expression += "|";
            }

        }
        Document regex = new Document("$regex", expression);
        Document queryDocument = new Document("description", regex);

        ArrayList<Document> queryResults = mongoDB.readAll(Collections.question,
                queryDocument, Sorts.ascending(sort), skip, limit);

        if (queryResults.isEmpty()) {
            result.put("[]", "No suggested for this course");
            return result;
        }

        int numberOfResutls = 0;
        for (Document doc : queryResults) {
            numberOfResutls++;
            JSONObject instance = new JSONObject(doc.toJson());
            result.append("question", instance);
            if(numberOfResutls == 5)
                break;
        }



        schema = null;
        query = null;
        sort = null;
        query = null;
        keywords = null;
        regex = null;
        queryDocument = null;

        return result;
    }
}
