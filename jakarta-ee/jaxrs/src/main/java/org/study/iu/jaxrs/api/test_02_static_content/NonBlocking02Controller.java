package org.study.iu.jaxrs.api.test_02_static_content;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

@Path("02_multi")
public class NonBlocking02Controller extends AbstractTestController implements MultiThreadingTestable {

    private static final ExecutorService OPERATIONAL_THREAD_POOL = Executors
            .newFixedThreadPool(OPERATIONAL_THREAD_POOL_SIZE);

    private static final int DEFAULT_LENGTH = 1000;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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
    
        final int length = jsonInput.getInt("length", DEFAULT_LENGTH);

        final Function<Integer, String> task = (Integer a) -> {
            int threadLength = Math.floorDiv(length, threads);

            if (a == threads - 1) {
                threadLength += length % threads;
            }

            final StringBuffer stringBuffer = new StringBuffer(threadLength);

            for (int i = 0; i < threadLength; i++) {
                stringBuffer.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
            }

            return stringBuffer.toString();
        };

        final List<CompletableFuture<String>> futures = IntStream
                .range(0, threads)
                .mapToObj(a -> CompletableFuture
                        .supplyAsync(() -> {
                            return task.apply(a);
                        }, OPERATIONAL_THREAD_POOL))
                .collect(Collectors.toList());

        final String result = futures.parallelStream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining());

        return Json
                .createObjectBuilder()
                .add("threads", threads)
                .add("length", length)
                .add("result", result)
                .build();
    }
}
