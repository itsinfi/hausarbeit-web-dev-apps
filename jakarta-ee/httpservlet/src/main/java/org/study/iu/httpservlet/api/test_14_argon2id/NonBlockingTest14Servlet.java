package org.study.iu.httpservlet.api.test_14_argon2id;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.encoders.Hex;
import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/14_multi", asyncSupported = true)
public class NonBlockingTest14Servlet extends BlockingTest14Servlet implements MultiThreadingTestable {

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
    protected String hashPassword(String password, int iterations, int parallelism, int memoryInKb, int saltSize) {
        byte[] salt = new byte[saltSize / 8];
        ThreadLocalRandom.current().nextBytes(salt);

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withMemoryAsKB(memoryInKb)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        byte[] hash = new byte[saltSize / 4];

        generator.generateBytes(passwordBytes, hash, 0, hash.length);

        String saltHex = new String(Hex.encode(salt));
        String hashHex = new String(Hex.encode(hash));

        return saltHex + "$" + hashHex;
    }
    
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