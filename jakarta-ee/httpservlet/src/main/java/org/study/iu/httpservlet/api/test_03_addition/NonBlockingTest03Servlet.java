package org.study.iu.httpservlet.api.test_03_addition;

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

@WebServlet(value = "/api/03_multi", asyncSupported = true)
public class NonBlockingTest03Servlet extends BlockingTest03Servlet implements MultiThreadingTestable {

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
        
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double sum = 0.0;

        final Function<Integer, Double> task = (Integer a) -> {
            int threadIterations = Math.floorDiv(iterations, threads);

            if (a == threads - 1) {
                threadIterations += iterations % threads;
            }
            
            double threadSum = 0.0;

            for (int i = 0; i < threadIterations; i++) {
                final double randomRealNumber = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
                threadSum += randomRealNumber;
            }

            return threadSum;
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
                sum += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return Json.createObjectBuilder()
                .add("threads",threads)
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", sum)
                .build();
    }
}