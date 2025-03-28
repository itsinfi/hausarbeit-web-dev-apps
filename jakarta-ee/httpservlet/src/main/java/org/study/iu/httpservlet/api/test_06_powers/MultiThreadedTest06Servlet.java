package org.study.iu.httpservlet.api.test_06_powers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
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

@WebServlet(value = "/api/06_multi", asyncSupported = true)
public class MultiThreadedTest06Servlet extends SingleThreadedTest06Servlet implements MultiThreadingTestable {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        AsyncContext asyncContext = req.startAsync();

        ExecutorService executor = getExecutor(THREAD_MODE);

        CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(res, result, asyncContext))
                .exceptionally(ex -> handleError(ex, asyncContext));

        return;
    }

    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null || threads <= 1) {
            return super.test(jsonInput);
        }

        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double sum = 0.0;

        final Function<Integer, Double> task = (Integer a) -> {
            int threadIterations = Math.floorDiv(iterations, threads);

            if (a == threads - 1) {
                threadIterations += iterations % threads;
            }

            double threadSum = 0;

            for (int i = 0; i < threadIterations; i++) {
                final double randomRealNumber = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
                threadSum += Math.exp(randomRealNumber);
            }

            return threadSum;
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
                sum += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return Json.createObjectBuilder()
                .add("usedThreadMode",taskThreadMode)
                .add("threads",threads)
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", sum)
                .build();
    }
}