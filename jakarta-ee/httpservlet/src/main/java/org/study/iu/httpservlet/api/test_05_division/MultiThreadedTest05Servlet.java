package org.study.iu.httpservlet.api.test_05_division;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/05_multi", asyncSupported = true)
public class MultiThreadedTest05Servlet extends SingleThreadedTest05Servlet implements MultiThreadingTestable {
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null || threads < 1) {
            return super.test(jsonInput);
        }

        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        BigDecimal totalProduct = BigDecimal.ONE;

        final Function<Integer, BigDecimal> task = (Integer a) -> {
            int threadIterations = iterations / threads;

            if (a == threads - 1) {
                threadIterations += iterations % threads;
            }

            BigDecimal threadProduct = BigDecimal.ONE;

            for (int i = 0; i < threadIterations; i++) {
                final double randomRealNumber = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
                threadProduct = threadProduct.multiply(BigDecimal.valueOf(randomRealNumber));
            }

            return threadProduct;
        };

        final List<CompletableFuture<BigDecimal>> futures = IntStream
                .range(0, threads)
                .mapToObj(a -> CompletableFuture
                        .supplyAsync(() -> {
                            return task.apply(a);
                        }, executor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        allDone.join();

        for (CompletableFuture<BigDecimal> future : futures) {
            try {
                totalProduct = totalProduct.multiply(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        BigDecimal quotient = BigDecimal
                .valueOf(Double.MAX_VALUE)
                .divide(totalProduct, 20, RoundingMode.HALF_UP);

        System.out.println("totalProduct: " + totalProduct.toEngineeringString());
        System.out.println("quotient: " + quotient);

        double result = quotient.doubleValue();

        return Json.createObjectBuilder()
                .add("usedThreadMode",taskThreadMode)
                .add("threads",threads)
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", result)
                .build();
    }
}