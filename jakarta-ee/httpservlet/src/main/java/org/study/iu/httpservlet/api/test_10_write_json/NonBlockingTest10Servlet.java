package org.study.iu.httpservlet.api.test_10_write_json;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/10_multi", asyncSupported = true)
public class NonBlockingTest10Servlet extends BlockingTest10Servlet implements MultiThreadingTestable {

    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        final long startTime = System.nanoTime();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();

        CompletableFuture.supplyAsync(() -> handleRoute(req), EVENT_LOOP_THREAD_POOL)
                .thenApply(result -> sendResponse(res, result, asyncContext, startTime))
                .exceptionally(ex -> handleError(ex, asyncContext, startTime));

        return;
    }

    private CompletableFuture<JsonObject> generateJsonObject(final ExecutorService executor, int depth, int objectsPerLevel, int arraySize, int minValue, int maxValue) {
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
        final int depth = jsonInput.getInt("depth", DEFAULT_DEPTH);
        final int objectsPerLevel = jsonInput.getInt("objectsPerLevel", DEFAULT_OBJECTS_PER_LEVEL);
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final JsonObject result = this.generateJsonObject(OPERATIONAL_THREAD_POOL, depth, objectsPerLevel, arraySize, minValue, maxValue).join();
        
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