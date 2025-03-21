package org.study.iu.jaxrs.api.test_07_logarithms;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Path;

@Path("07_multi")
public class MultiThreaded07Controller extends SingleThreadedTest07Controller implements MultiThreadingTestable {
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null || threads < 1) {
            return super.test(jsonInput);
        }

        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        
        int finiteCount = 0;

        final Function<Integer, Double> task = (Integer a) -> {
            int threadIterations = iterations / threads;

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
                        .supplyAsync(() -> task.apply(a), executor))
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
                .add("usedThreadMode",taskThreadMode)
                .add("threads",threads)
                .add("iterations", iterations)
                .add("result", finiteCount)
                .build();
    }
}
