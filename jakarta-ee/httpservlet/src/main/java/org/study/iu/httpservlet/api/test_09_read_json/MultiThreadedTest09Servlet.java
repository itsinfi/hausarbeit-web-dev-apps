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
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/09_multi", asyncSupported = true)
public class MultiThreadedTest09Servlet extends SingleThreadedTest09Servlet implements MultiThreadingTestable {

    private final static int DEFAULT_PARALLELIZATION_THRESHOLD = 3;
    private final static int DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;

    private void flattenJson(JsonValue json, List<Double> numbers, ExecutorService executor, int depth, int parallelizationThreshold, int nestingParallelizationLimit) {
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
                    for (JsonValue value : jsonObject.values()) {
                        flattenJson(value, numbers, executor, depth,
                                parallelizationThreshold, nestingParallelizationLimit);
                    }
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                
                if (depth <= DEFAULT_NESTING_PARALLELIZATION_LIMIT && jsonArray.size() >= DEFAULT_PARALLELIZATION_THRESHOLD) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (JsonValue element : jsonArray) {
                        futures.add(CompletableFuture.runAsync(() -> 
                                flattenJson(element, numbers, executor, depth,
                                        parallelizationThreshold, nestingParallelizationLimit), executor));
                    }

                    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                } else {
                    for (JsonValue element : jsonArray) {
                        flattenJson(element, numbers, executor, depth,
                                parallelizationThreshold, nestingParallelizationLimit);
                    }
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
        ExecutorService executor = getExecutor(taskThreadMode);
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