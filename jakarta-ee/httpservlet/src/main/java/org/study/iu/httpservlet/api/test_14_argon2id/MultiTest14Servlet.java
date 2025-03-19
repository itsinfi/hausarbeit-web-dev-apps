package org.study.iu.httpservlet.api.test_14_argon2id;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/14_multi", asyncSupported = true)
public class MultiTest14Servlet extends Test14Servlet implements MultiThreadingTestable {
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null) {
            return super.test(jsonInput);
        }

        final String password = jsonInput.getString("password");
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ARGON2_ITERATIONS);
        final int parallelism = jsonInput.getInt("parallelism", DEFAULT_ARGON2_PARALLELISM);
        final int memoryInKb = jsonInput.getInt("memoryInKb", DEFAULT_ARGON2_MEMORY_IN_KB);
        final int saltSize = jsonInput.getInt("saltSize", DEFAULT_SALT_SIZE);
        final int taskAmount = jsonInput.getInt("taskAmount", DEFAULT_TASK_AMOUNT);

        final Supplier<JsonObject> task = () -> {
            final String hashedPassword = hashPassword(password, iterations, parallelism, memoryInKb, saltSize);

            final boolean checkAuth = verifyPassword(password, hashedPassword, iterations, parallelism, memoryInKb, saltSize);

            return Json
                    .createObjectBuilder()
                    .add("hashedPassword", hashedPassword)
                    .add("checkAuth", checkAuth)
                    .build();
        };

        final List<CompletableFuture<JsonObject>> futures = IntStream
                .range(0, taskAmount)
                .mapToObj(a -> CompletableFuture
                .supplyAsync(task, executor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        List<JsonObject> results = allDone.thenApply(v -> 
            futures.stream().map(CompletableFuture::join).collect(Collectors.toList())
        ).join();
        
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        results.forEach(jsonArrayBuilder::add);
        JsonArray result = jsonArrayBuilder.build();
        
        return Json.createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("password", password)
                .add("iterations", iterations)
                .add("parallelism", parallelism)
                .add("memoryInKb", memoryInKb)
                .add("saltSize", saltSize)
                .add("taskAmount", taskAmount)
                .add("result", result)
                .build();
    }
}