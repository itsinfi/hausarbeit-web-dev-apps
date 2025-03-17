package org.study.iu.jaxrs.api.test_08_prime_numbers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.study.iu.jaxrs.classes.AbstractAsyncTestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("08")
public class Test08Controller extends AbstractAsyncTestController {

    private static final int DEFAULT_AMOUNT = 1000;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public void post(@Suspended AsyncResponse res, JsonObject req) throws IOException {
        final JsonObject entity = executeTest(req);

        Response response = Response
                .ok()
                .entity(entity)
                .build();

        res.resume(response);
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) throws IOException {
        final int amount = jsonInput.getInt("amount", DEFAULT_AMOUNT);
        
        ArrayList<Integer> primes = new ArrayList<>();
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