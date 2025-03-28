package org.study.iu.jaxrs.api.test_02_static_content;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
public class MultiThreaded02Controller extends AbstractTestController implements MultiThreadingTestable {

    private static final int DEFAULT_LENGTH = 1000;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        ExecutorService executor = getExecutor(THREAD_MODE);
        return CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(result))
                .exceptionally(ex -> handleError(ex));
    }

    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
    
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
                        }, executor))
                .collect(Collectors.toList());

        final String result = futures.parallelStream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining());

        return Json
                .createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("threads", threads)
                .add("length", length)
                .add("result", result)
                .build();
    }
}
