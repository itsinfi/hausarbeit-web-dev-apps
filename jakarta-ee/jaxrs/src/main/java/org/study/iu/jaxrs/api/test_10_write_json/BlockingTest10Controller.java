package org.study.iu.jaxrs.api.test_10_write_json;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractTestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("10")
public class BlockingTest10Controller extends AbstractTestController {

    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_OBJECTS_PER_LEVEL = 4;
    private static final int DEFAULT_ARRAY_SIZE = 4;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    
    private static final Random RANDOM = new Random();
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(JsonObject req) {
final long startTime = System.nanoTime();
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result, startTime);
        } catch (Exception ex) {
            return handleError(ex, startTime);
        }
    }

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