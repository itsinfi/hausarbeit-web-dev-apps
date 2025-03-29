package org.study.iu.jaxrs.api.test_08_prime_numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

@Path("08_multi")
public class NonBlocking08Controller extends AbstractTestController implements MultiThreadingTestable {
    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    private static final int DEFAULT_AMOUNT = 1000;

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
                    }, OPERATIONAL_THREAD_POOL)
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
                .add("threads", threads)
                .add("iterations", iterations)
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}
