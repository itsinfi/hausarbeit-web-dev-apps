package org.study.iu.jaxrs.api.test_12_sort_real_numbers;

import java.util.Arrays;
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("12_multi")
public class NonBlocking12Controller extends AbstractTestController implements MultiThreadingTestable {
    private static final int DEFAULT_ARRAY_SIZE = 1000;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
final long startTime = System.nanoTime();
        final ExecutorService executor = getExecutor(THREAD_MODE);
        return CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(result, startTime))
                .exceptionally(ex -> handleError(ex, startTime));
    }

    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        final ExecutorService executor = getExecutor(taskThreadMode);

        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final List<CompletableFuture<double[]>> futures = IntStream
                .range(0, threads)
                .mapToObj(t -> CompletableFuture.supplyAsync(() -> {
                    final int threadArraySize = arraySize / threads + (t < arraySize % threads ? 1 : 0);

                    final double[] threadArray = new double[threadArraySize];

                    final ThreadLocalRandom random = ThreadLocalRandom.current();

                    for (int i = 0; i < threadArraySize; i++) {
                        threadArray[i] = random.nextDouble(minValue, maxValue + 1);
                    }

                    return threadArray;
                }, executor))
                .collect(Collectors.toList());

        final double[] array = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> {
                    final double[] result = new double[arraySize];
                    int index = 0;

                    for (final CompletableFuture<double[]> future : futures) {
                        final double[] threadArray = future.join();
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
                .add("usedThreadMode", taskThreadMode)
                .add("threads", threads)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}
