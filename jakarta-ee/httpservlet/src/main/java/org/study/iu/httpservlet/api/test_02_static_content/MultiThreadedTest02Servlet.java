package org.study.iu.httpservlet.api.test_02_static_content;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

@WebServlet(value = "/api/02_multi", asyncSupported = true)
public class MultiThreadedTest02Servlet extends SingleThreadedTest02Servlet implements MultiThreadingTestable {

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