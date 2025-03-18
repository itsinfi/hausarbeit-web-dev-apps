package org.study.iu.httpservlet.api.test_02_static_content;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.httpservlet.interfaces.MultiThreadingTest;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/02", asyncSupported = true)
public class MultiTest02Servlet extends Test02Servlet implements MultiThreadingTest {
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        if (getExecutor(taskThreadMode) == null || threads < 1) {
            return super.test(jsonInput);
        }
    
        final int length = jsonInput.getInt("length", DEFAULT_LENGTH);

        final StringBuilder stringBuilder = new StringBuilder(length);

        final Supplier<String> task = () -> {
            final StringBuilder innerStringBuilder = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                innerStringBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }

            return innerStringBuilder.toString();
        };

        final List<CompletableFuture<String>> futures = IntStream
                .range(0, threads)
                .mapToObj(i -> CompletableFuture
                .supplyAsync(task, getExecutor(taskThreadMode)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        allDone.thenRun(() -> {
            for (CompletableFuture<String> future : futures) {
                try {
                    stringBuilder.append(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        return Json.createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("length", length)
                .add("result", stringBuilder.toString())
                .build();
    }
}