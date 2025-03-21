package org.study.iu.jaxrs.api.test_08_prime_numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Path;

@Path("08_multi")
public class MultiThreaded08Controller extends SingleThreadedTest08Controller implements MultiThreadingTestable {
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null || threads <= 1) {
            return super.test(jsonInput);
        }

        final int amount = jsonInput.getInt("amount", DEFAULT_AMOUNT);

        List<Integer> primes = Collections.synchronizedList(new ArrayList<>());
        int limit = Math.max(2, amount);
        int iterations = 0;

        do {
            final int currentLimit = limit;
            final int squareRootOfLimit = (int) Math.sqrt(currentLimit);

            primes.clear();
            
            boolean[] sieve = new boolean[squareRootOfLimit + 1];
            Arrays.fill(sieve, true);
            sieve[0] = sieve[1] = false;

            for (int i = 2; i <= squareRootOfLimit; i++) {
                if (sieve[i]) {
                    for (int j = i * i; j <= squareRootOfLimit; j += i) {
                        sieve[j] = false;
                    }
                }
            }

            for (int i = 2; i <= squareRootOfLimit; i++) {
                if (sieve[i]) {
                    primes.add(i);
                }
            }

            List<CompletableFuture<List<Integer>>> futures = new ArrayList<>(threads);

            for (int t = 0; t < threads; t++) {
                final int threadIndex = t;
                futures.add(
                    CompletableFuture.supplyAsync(() -> {
                        int start = squareRootOfLimit + 1 + threadIndex * ((currentLimit - squareRootOfLimit) / threads);
                        int end = (threadIndex == threads - 1) ? currentLimit : start + ((currentLimit - squareRootOfLimit) / threads);

                        boolean[] segmentSieve = new boolean[end - start + 1];
                        Arrays.fill(segmentSieve, true);

                        for (int prime : primes) {
                            int firstMultiple = Math.max(prime * prime, (start + prime - 1) / prime * prime);
                            for (int j = firstMultiple; j <= end; j += prime) {
                                segmentSieve[j - start] = false;
                            }
                        }

                        List<Integer> threadPrimes = new ArrayList<>();
                        for (int i = 0; i < segmentSieve.length; i++) {
                            if (segmentSieve[i]) {
                                threadPrimes.add(start + i);
                            }
                        }

                        return threadPrimes;
                    }, executor)
                );
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            for (CompletableFuture<List<Integer>> future : futures) {
                try {
                    primes.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            primes.addAll(0, primes);

            limit *= 2;
            iterations++;
        } while (primes.size() < amount);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (int i = 0; i < amount; i++) {
            jsonArrayBuilder.add(primes.get(i));
        }

        JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("threads", threads)
                .add("iterations", iterations)
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}
