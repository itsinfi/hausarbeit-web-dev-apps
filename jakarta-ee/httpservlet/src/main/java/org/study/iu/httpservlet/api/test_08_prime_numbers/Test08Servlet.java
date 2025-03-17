package org.study.iu.httpservlet.api.test_08_prime_numbers;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
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
import java.util.ArrayList;
import java.util.Arrays;

import org.study.iu.httpservlet.classes.TestServlet;

@WebServlet(value = "/api/08", asyncSupported = true)
public class Test08Servlet extends TestServlet {

    private static final int DEFAULT_AMOUNT = 1000;

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

                final JsonObject jsonOutput = executeTest(jsonInput);

                try (final PrintWriter out = resp.getWriter()) {
                    out.print(jsonOutput.toString());
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                asyncContext.complete();
            }
        });
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) throws IOException {
        final int amount = jsonInput.getInt("amount", DEFAULT_AMOUNT);
        
        ArrayList<Integer> primes = new ArrayList<Integer>();
        int limit = amount;
        int iterations = 0;

        if (amount <= 1) {
            throw new IOException("'amount' needs to be > 1");
        }

        do {
            final double squareRootOfLimit = Math.sqrt(limit);

            primes.clear();
            
            final boolean[] sieve = new boolean[limit + 1];
            Arrays.fill(sieve, true);
            sieve[0] = sieve[1] = false;

            for (int i = 2; i <= squareRootOfLimit; i++) {
                if (sieve[i]) {
                    for (int j = i * i; j <= limit; j += i) {
                        sieve[j] = false;
                    }
                }
            }

            for (int i = 2; i <= limit; i++) {
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
                .add("iterations", iterations)
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}