package org.study.iu.httpservlet.api.test_12_sort_real_numbers;

import java.util.Arrays;
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
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/12_multi", asyncSupported = true)
public class NonBlockingTest12Servlet extends BlockingTest12Servlet implements MultiThreadingTestable {

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
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);

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
                }, OPERATIONAL_THREAD_POOL))
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
                .add("threads", threads)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}