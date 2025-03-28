package org.study.iu.jaxrs.api.test_10_write_json;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.jaxrs.classes.AbstractTestController;
import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

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

@Path("10_multi")
public class NonBlocking10Controller extends AbstractTestController implements MultiThreadingTestable {

    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_OBJECTS_PER_LEVEL = 4;
    private static final int DEFAULT_ARRAY_SIZE = 4;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        ExecutorService executor = getExecutor(THREAD_MODE);
        return CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(result))
                .exceptionally(ex -> handleError(ex));
    }

    private CompletableFuture<JsonObject> generateJsonObject(ExecutorService executor, int depth, int objectsPerLevel, int arraySize, int minValue, int maxValue) {
        return CompletableFuture.supplyAsync(() -> {
            final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

            if (depth > 1) {
                List<CompletableFuture<Void>> futures = IntStream
                        .rangeClosed(1, objectsPerLevel)
                        .mapToObj(i -> generateJsonObject(executor, depth - 1, objectsPerLevel, arraySize, minValue,
                                maxValue)
                                .thenAcceptAsync(obj -> jsonObjectBuilder.add(Integer.toString(i), obj)))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            }
            
            jsonObjectBuilder.add("_", generateJsonArray(arraySize, minValue, maxValue));
            return jsonObjectBuilder.build();
        }, executor);
    }

    private JsonArray generateJsonArray(int size, int minValue, int maxValue) {
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < size; i++) {
            jsonArrayBuilder.add(ThreadLocalRandom.current().nextDouble(minValue, maxValue));
        }

        return jsonArrayBuilder.build();
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        ExecutorService executor = getExecutor(taskThreadMode);

        final int depth = jsonInput.getInt("depth", DEFAULT_DEPTH);
        final int objectsPerLevel = jsonInput.getInt("objectsPerLevel", DEFAULT_OBJECTS_PER_LEVEL);
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final JsonObject result = this.generateJsonObject(executor, depth, objectsPerLevel, arraySize, minValue, maxValue).join();
        
        return Json.createObjectBuilder()
                .add("usedThreadMode",taskThreadMode)
                .add("depth", depth)
                .add("objectsPerLevel", objectsPerLevel)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}
