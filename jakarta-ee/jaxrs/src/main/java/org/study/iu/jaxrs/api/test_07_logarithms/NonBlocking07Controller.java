package org.study.iu.jaxrs.api.test_07_logarithms;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.jaxrs.classes.AbstractTestController;
import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("07_multi")
public class NonBlocking07Controller extends AbstractTestController implements MultiThreadingTestable {

    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    private static final int DEFAULT_ITERATIONS = 1000;

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

        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        
        int finiteCount = 0;

        final Function<Integer, Double> task = (Integer a) -> {
            int threadIterations = Math.floorDiv(iterations, threads);

            if (a == threads - 1) {
                threadIterations += iterations % threads;
            }

            double threadFiniteCount = 0;

            for (int i = 0; i < threadIterations; i++) {
                ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

                final double randomRealNumber = RANDOM.nextDouble() < 0.5
                        ? RANDOM.nextDouble() * Double.MAX_VALUE * Double.MAX_VALUE
                        : RANDOM.nextDouble() * Double.MAX_VALUE;

                final double result = Math.log(randomRealNumber);

                if (Double.isFinite(result)) {
                    threadFiniteCount++;
                }
            }

            return threadFiniteCount;
        };

        final List<CompletableFuture<Double>> futures = IntStream
                .range(0, threads)
                .mapToObj(a -> CompletableFuture
                        .supplyAsync(() -> task.apply(a), OPERATIONAL_THREAD_POOL))
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        allDone.join();

        for (CompletableFuture<Double> future : futures) {
            try {
                finiteCount += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return Json.createObjectBuilder()
                .add("threads",threads)
                .add("iterations", iterations)
                .add("result", finiteCount)
                .build();
    }
}
