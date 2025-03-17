package org.study.iu.jaxrs.api.test_06_powers;

import java.io.IOException;
import java.util.Random;

import org.study.iu.jaxrs.classes.TestRessource;

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

@Path("06")
public class Test06Ressource extends TestRessource {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    private static final int DEFAULT_LOWER_BOUND = -10;
    private static final int DEFAULT_UPPER_BOUND = 10;
    
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
        
        double sum = 0.0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            sum += Math.pow(Math.E, randomRealNumber);
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", sum)
                .build();
    }
}