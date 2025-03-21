package org.study.iu.httpservlet.api.test_08_prime_numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/08_multi", asyncSupported = true)
public class MultiThreadedTest08Servlet extends SingleThreadedTest08Servlet implements MultiThreadingTestable {
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
            final double squareRootOfLimit = Math.sqrt(currentLimit);

            primes.clear();
            
            boolean[] sieve = new boolean[currentLimit + 1];
            Arrays.fill(sieve, true);
            sieve[0] = sieve[1] = false;

            for (int i = 2; i <= squareRootOfLimit; i++) {
                if (sieve[i]) {
                    for (int j = i * i; j <= currentLimit; j += i) {
                        sieve[j] = false;
                    }
                }
            }

            List<CompletableFuture<Void>> futures = new ArrayList<>(threads);

            for (int t = 0; t < threads; t++) {
                final int threadIndex = t;
                futures.add(
                    CompletableFuture.runAsync(() -> {
                            int start = (int) squareRootOfLimit + 1 + threadIndex * ((currentLimit - (int) squareRootOfLimit) / threads);
                            int end = (threadIndex == threads - 1) ? currentLimit : start + ((currentLimit - (int) squareRootOfLimit) / threads);

                            for (int i = 2; i <= squareRootOfLimit; i++) {
                                if (sieve[i]) {
                                    int firstMultiple = Math.max(i + i, (start + i - 1) / i * i);
                                    
                                    for (int j = firstMultiple; j <= end; j += i) {
                                        sieve[j] = false;
                                    }
                                }
                            }
                    }, executor)
                );
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            for (int i = 2; i <= currentLimit; i++) {
                if (sieve[i]) {
                    primes.add(i);
                }
            }

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
