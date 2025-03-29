package org.study.iu.httpservlet.api.test_02_static_content;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

@WebServlet(value = "/api/02_multi", asyncSupported = true)
public class NonBlockingTest02Servlet extends BlockingTest02Servlet implements MultiThreadingTestable {

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