package org.study.iu.jaxrs.api.test_13_linq_streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.study.iu.jaxrs.classes.AbstractTestController;
import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("13_multi")
public class NonBlocking13Controller extends AbstractTestController implements MultiThreadingTestable {
    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    private final static int DEFAULT_PARALLELIZATION_THRESHOLD = 3;
    private final static int DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        final long startTime = System.nanoTime();
        return CompletableFuture.supplyAsync(() -> handleRoute(req), EVENT_LOOP_THREAD_POOL)
                .thenApply(result -> sendResponse(result, startTime))
                .exceptionally(ex -> handleError(ex, startTime));
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
                    for (JsonValue value : jsonObject.values()) {
                        flattenJson(value, numbers, executor, depth,
                                parallelizationThreshold, nestingParallelizationLimit);
                    }
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                
                double sum = 0.0;
                for (JsonValue element : jsonArray) {
                    sum += ((JsonNumber) element).doubleValue();
                }

                double avg = sum / jsonArray.size();
                numbers.add(avg);
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

        final List<Double> numbers = Collections.synchronizedList(new ArrayList<>());

        flattenJson(jsonInput, numbers, OPERATIONAL_THREAD_POOL, 0, parallelizationThreshold, nestingParallelizationLimit);

        Collections.sort(numbers, Comparator.naturalOrder());
        
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Double number : numbers) {
            jsonArrayBuilder.add(number);
        }

        final JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("parallelizationThreshold", parallelizationThreshold)
                .add("nestingParallelizationLimit", nestingParallelizationLimit)
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}
