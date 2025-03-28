package org.study.iu.jaxrs.api.test_07_logarithms;

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

@Path("07")
public class BlockingTest07Controller extends AbstractTestController {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    
    private static final Random RANDOM = new Random();

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
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        
        int finiteCount = 0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble() < 0.5
                    ? RANDOM.nextDouble() * Double.MAX_VALUE * Double.MAX_VALUE
                    : RANDOM.nextDouble() * Double.MAX_VALUE;

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