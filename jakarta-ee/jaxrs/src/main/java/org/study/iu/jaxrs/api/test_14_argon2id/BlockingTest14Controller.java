package org.study.iu.jaxrs.api.test_14_argon2id;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.encoders.Hex;
import org.study.iu.jaxrs.classes.AbstractTestController;

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

@Path("14")
public class BlockingTest14Controller extends AbstractTestController {

    private static final int DEFAULT_ARGON2_ITERATIONS = 3;
    private static final int DEFAULT_ARGON2_PARALLELISM = 4;
    private static final int DEFAULT_ARGON2_MEMORY_IN_KB = 65536;
    private static final int DEFAULT_SALT_SIZE = 128;
    private static final int DEFAULT_TASK_AMOUNT = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(JsonObject req) {
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result);
        } catch (Exception ex) {
            return handleError(ex);
        }
    }
    
    private String hashPassword(String password, int iterations, int parallelism, int memoryInKb, int saltSize) {
        byte[] salt = new byte[saltSize / 8];
        RANDOM.nextBytes(salt);

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
    
    private boolean verifyPassword(String password, String storedHash, int iterations, int parallelism, int memoryInKb, int saltSize) {
        String[] parts = storedHash.split("\\$");
        String storedSaltHex = parts[0];
        String storedHashHex = parts[1];

        byte[] salt = Hex.decode(storedSaltHex);
        byte[] expectedHash = Hex.decode(storedHashHex);

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withMemoryAsKB(memoryInKb)
                .withSalt(salt)
                .build();
            
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] actualHash = new byte[saltSize / 4];

        generator.generateBytes(passwordBytes, actualHash, 0, actualHash.length);

        return Arrays.equals(actualHash, expectedHash);
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String password = jsonInput.getString("password");
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ARGON2_ITERATIONS);
        final int parallelism = jsonInput.getInt("parallelism", DEFAULT_ARGON2_PARALLELISM);
        final int memoryInKb = jsonInput.getInt("memoryInKb", DEFAULT_ARGON2_MEMORY_IN_KB);
        final int saltSize = jsonInput.getInt("saltSize", DEFAULT_SALT_SIZE);
        final int taskAmount = jsonInput.getInt("taskAmount", DEFAULT_TASK_AMOUNT);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < taskAmount; i++) {
            final String hashedPassword = hashPassword(password, iterations, parallelism, memoryInKb, saltSize);
            final boolean checkAuth = verifyPassword(password, hashedPassword, iterations, parallelism, memoryInKb,
                    saltSize);

            JsonObject hashPasswordObject = Json
                    .createObjectBuilder()
                    .add("hashedPassword", hashedPassword)
                    .add("checkAuth", checkAuth)
                    .build();

            jsonArrayBuilder.add(hashPasswordObject);
        }
        
        JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
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