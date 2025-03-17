package org.study.iu.jaxrs.api.test_14_argon2id;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.encoders.Hex;
import org.study.iu.jaxrs.classes.TestServlet;

@WebServlet(value = "/api/14", asyncSupported = true)
public class Test14Servlet extends TestServlet {

    private static final int DEFAULT_ARGON2_ITERATIONS = 3;
    private static final int DEFAULT_ARGON2_PARALLELISM = 4;
    private static final int DEFAULT_ARGON2_MEMORY_IN_KB = 65536;
    private static final int DEFAULT_SALT_SIZE = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            try (
                    final InputStream inputStream = req.getInputStream();
                    final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))) {
                final JsonObject jsonInput = jsonReader.readObject();

                if (jsonInput.containsKey("password")) {
                    final JsonObject jsonOutput = executeTest(jsonInput);

                    try (final PrintWriter out = resp.getWriter()) {
                        out.print(jsonOutput.toString());
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                asyncContext.complete();
            }
        });
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
    protected JsonObject executeTest(JsonObject jsonInput) {
        final String password = jsonInput.getString("password");
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ARGON2_ITERATIONS);
        final int parallelism = jsonInput.getInt("parallelism", DEFAULT_ARGON2_PARALLELISM);
        final int memoryInKb = jsonInput.getInt("memoryInKb", DEFAULT_ARGON2_MEMORY_IN_KB);
        final int saltSize = jsonInput.getInt("saltSize", DEFAULT_SALT_SIZE);

        final String hashedPassword = hashPassword(password, iterations, parallelism, memoryInKb, saltSize);

        final boolean checkAuth = verifyPassword(password, hashedPassword, iterations, parallelism, memoryInKb, saltSize);

        JsonObject hashPasswordObject = Json
                .createObjectBuilder()
                .add("hashedPassword", hashedPassword)
                .add("checkAuth", checkAuth)
                .build();

        return Json.createObjectBuilder()
                .add("password", password)
                .add("iterations", iterations)
                .add("parallelism", parallelism)
                .add("memoryInKb", memoryInKb)
                .add("saltSize", saltSize)
                .add("result", hashPasswordObject)
                .build();
    }
}