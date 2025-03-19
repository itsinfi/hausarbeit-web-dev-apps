package org.study.iu.httpservlet.api.test_10_write_json;

import java.util.Random;

import org.study.iu.httpservlet.classes.AbstractAsyncTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/10", asyncSupported = true)
public class SingleThreadedTest10Servlet extends AbstractAsyncTestServlet {

    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_OBJECTS_PER_LEVEL = 4;
    private static final int DEFAULT_ARRAY_SIZE = 4;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    
    private static final Random RANDOM = new Random();

    private JsonObject generateJsonObject(int depth, int objectsPerLevel, int arraySize, int minValue, int maxValue) {
        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        if (depth > 1) {
            for (int i = 1; i <= objectsPerLevel; i++) {
                jsonObjectBuilder.add(Integer.toString(i),
                this.generateJsonObject(depth - 1, objectsPerLevel, arraySize, minValue, maxValue));
            }
        }
        
        jsonObjectBuilder.add("_", generateJsonArray(arraySize, minValue, maxValue));

        return jsonObjectBuilder.build();
    }

    private JsonArray generateJsonArray(int size, int minValue, int maxValue) {
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < size; i++) {
            jsonArrayBuilder.add(RANDOM.nextDouble(minValue, maxValue));
        }

        return jsonArrayBuilder.build();
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int depth = jsonInput.getInt("depth", DEFAULT_DEPTH);
        final int objectsPerLevel = jsonInput.getInt("objectsPerLevel", DEFAULT_OBJECTS_PER_LEVEL);
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final JsonObject result = this.generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue);
        
        return Json.createObjectBuilder()
                .add("depth", depth)
                .add("objectsPerLevel", objectsPerLevel)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}