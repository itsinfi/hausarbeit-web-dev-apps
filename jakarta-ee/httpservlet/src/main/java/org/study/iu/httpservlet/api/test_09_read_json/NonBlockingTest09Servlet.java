package org.study.iu.httpservlet.api.test_09_read_json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/09_multi", asyncSupported = true)
public class NonBlockingTest09Servlet extends BlockingTest09Servlet implements MultiThreadingTestable {

    private final static int DEFAULT_PARALLELIZATION_THRESHOLD = 3;
    private final static int DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        final long startTime = System.nanoTime();
        
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();

        final ExecutorService executor = getExecutor(THREAD_MODE);

        CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(res, result, asyncContext, startTime))
                .exceptionally(ex -> handleError(ex, asyncContext, startTime));

        return;
    }

    private void flattenJson(JsonValue json, List<Double> numbers, final ExecutorService executor, int depth, int parallelizationThreshold, int nestingParallelizationLimit) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();

                if (depth <= nestingParallelizationLimit && jsonObject.size() >= parallelizationThreshold) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (JsonValue value : jsonObject.values()) {
                        futures.add(CompletableFuture.runAsync(() -> 
                        flattenJson(value, numbers, executor, depth + 1,
                                parallelizationThreshold, nestingParallelizationLimit), executor));
                    }

                    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                } else {
                    jsonObject.values().forEach(value -> this.flattenJson(value, numbers, executor, depth,
                            parallelizationThreshold, nestingParallelizationLimit));
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                
                if (depth <= nestingParallelizationLimit && jsonArray.size() >= parallelizationThreshold) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (JsonValue element : jsonArray) {
                        futures.add(CompletableFuture.runAsync(() -> 
                                flattenJson(element, numbers, executor, depth,
                                        parallelizationThreshold, nestingParallelizationLimit), executor));
                    }

                    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                } else {
                    jsonArray.forEach(element -> flattenJson(element, numbers, executor, depth,
                            parallelizationThreshold, nestingParallelizationLimit));
                }
            }

            case NUMBER -> {
                synchronized (this) {
                    numbers.add(((JsonNumber) json).doubleValue());
                }
            }

            default -> {}
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int parallelizationThreshold = jsonInput.getInt("parallelizationThreshold",
                DEFAULT_PARALLELIZATION_THRESHOLD);
        final int nestingParallelizationLimit = jsonInput.getInt("nestingParallelizationLimit",
                DEFAULT_NESTING_PARALLELIZATION_LIMIT);
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null) {
            return super.test(jsonInput);
        }

        final List<Double> numbers = Collections.synchronizedList(new ArrayList<>());

        flattenJson(jsonInput, numbers, executor, 0, parallelizationThreshold, nestingParallelizationLimit);

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Double number : numbers) {
            jsonArrayBuilder.add(number);
        }

        final JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("parallelizationThreshold", parallelizationThreshold)
                .add("nestingParallelizationLimit", nestingParallelizationLimit)
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}