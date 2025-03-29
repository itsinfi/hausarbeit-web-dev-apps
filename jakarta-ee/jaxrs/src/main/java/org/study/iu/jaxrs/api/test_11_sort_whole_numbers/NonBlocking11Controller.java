package org.study.iu.jaxrs.api.test_11_sort_whole_numbers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.jaxrs.classes.AbstractTestController;
import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("11_multi")
public class NonBlocking11Controller extends AbstractTestController implements MultiThreadingTestable {
    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);


    private static final int DEFAULT_ARRAY_SIZE = 1000;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        final long startTime = System.nanoTime();   
        return CompletableFuture.supplyAsync(() -> handleRoute(req), EVENT_LOOP_THREAD_POOL)
                .thenApply(result -> sendResponse(result, startTime))
                .exceptionally(ex -> handleError(ex, startTime));
    }

    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);

        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final List<CompletableFuture<int[]>> futures = IntStream
                .range(0, threads)
                .mapToObj(t -> CompletableFuture.supplyAsync(() -> {
                    final int threadArraySize = arraySize / threads + (t < arraySize % threads ? 1 : 0);

                    final int[] threadArray = new int[threadArraySize];

                    final ThreadLocalRandom random = ThreadLocalRandom.current();

                    for (int i = 0; i < threadArraySize; i++) {
                        threadArray[i] = random.nextInt(minValue, maxValue + 1);
                    }

                    return threadArray;
                }, OPERATIONAL_THREAD_POOL))
                .collect(Collectors.toList());

        final int[] array = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> {
                    final int[] result = new int[arraySize];
                    int index = 0;

                    for (final CompletableFuture<int[]> future : futures) {
                        final int[] threadArray = future.join();
                        System.arraycopy(threadArray, 0, result, index, threadArray.length);
                        index += threadArray.length;
                    }

                    return result;
                })
                .join();
        
        Arrays.sort(array);

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < arraySize; i++) {
            jsonArrayBuilder.add(array[i]);
        }

        final JsonArray result = jsonArrayBuilder.build();
        
        return Json.createObjectBuilder()
                .add("threads", threads)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}
