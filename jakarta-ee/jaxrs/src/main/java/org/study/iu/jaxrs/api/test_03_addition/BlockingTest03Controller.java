package org.study.iu.jaxrs.api.test_03_addition;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractTestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("03")
public class BlockingTest03Controller extends AbstractTestController {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    private static final int DEFAULT_LOWER_BOUND = 0;
    private static final int DEFAULT_UPPER_BOUND = 1;
    
    private static final Random RANDOM = new Random();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(JsonObject req) {
final long startTime = System.nanoTime();
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result, startTime);
        } catch (Exception ex) {
            return handleError(ex, startTime);
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double sum = 0.0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            sum += randomRealNumber;
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", sum)
                .build();
    }
}