package org.study.iu.jaxrs.api.test_05_division;

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

@Path("05")
public class SingleThreadedTest05Controller extends AbstractAsyncTestController {
    
    protected static final int DEFAULT_ITERATIONS = 1000;
    protected static final int DEFAULT_LOWER_BOUND = 1;
    protected static final int DEFAULT_UPPER_BOUND = 2;
    
    protected static final Random RANDOM = new Random();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        return handlePost(req);
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double quotient = Double.MAX_VALUE;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            quotient /= randomRealNumber;
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", quotient)
                .build();
    }
}