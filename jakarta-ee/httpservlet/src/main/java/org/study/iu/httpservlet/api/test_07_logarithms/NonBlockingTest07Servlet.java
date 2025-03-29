package org.study.iu.httpservlet.api.test_07_logarithms;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/07_multi", asyncSupported = true)
public class NonBlockingTest07Servlet extends BlockingTest07Servlet implements MultiThreadingTestable {

    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        final long startTime = System.nanoTime();
        
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();

        CompletableFuture.supplyAsync(() -> handleRoute(req), OPERATIONAL_THREAD_POOL)
                .thenApply(result -> sendResponse(res, result, asyncContext, startTime))
                .exceptionally(ex -> handleError(ex, asyncContext, startTime));

        return;
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