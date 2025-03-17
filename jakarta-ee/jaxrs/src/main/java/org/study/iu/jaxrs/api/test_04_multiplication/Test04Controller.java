package org.study.iu.jaxrs.api.test_04_multiplication;

import java.io.IOException;
import java.util.Random;

import org.study.iu.jaxrs.classes.AbstractAsyncTestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("04")
public class Test04Controller extends AbstractAsyncTestController {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    private static final int DEFAULT_LOWER_BOUND = 1;
    private static final int DEFAULT_UPPER_BOUND = 2;
    
    private static final Random RANDOM = new Random();

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
    protected JsonObject executeTest(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double product = 1.0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            product *= randomRealNumber;
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", product)
                .build();
    }
}