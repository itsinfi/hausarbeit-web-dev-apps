package org.study.iu.jaxrs.api.test_07_logarithms;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractAsyncTestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("07")
public class Test07Controller extends AbstractAsyncTestController {
    
    protected static final int DEFAULT_ITERATIONS = 1000;
    
    protected static final Random RANDOM = new Random();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        return handlePost(req);
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        
        int finiteCount = 0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble() < 0.5
                    ? RANDOM.nextDouble() * 1.0e-100
                    : RANDOM.nextDouble() * 1.0e100;

            final double result = Math.log(randomRealNumber);

            if (Double.isFinite(result)) {
                finiteCount++;
            }
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("result", finiteCount)
                .build();
    }
}