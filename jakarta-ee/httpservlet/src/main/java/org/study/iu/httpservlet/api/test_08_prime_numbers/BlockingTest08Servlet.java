package org.study.iu.httpservlet.api.test_08_prime_numbers;

import java.util.ArrayList;
import java.util.Arrays;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/08", asyncSupported = true)
public class BlockingTest08Servlet extends AbstractTestServlet {

    protected static final int DEFAULT_AMOUNT = 1000;
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int amount = jsonInput.getInt("amount", DEFAULT_AMOUNT);
        
        ArrayList<Integer> primes = new ArrayList<>();
        int limit = Math.max(2, amount);
        int iterations = 0;

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