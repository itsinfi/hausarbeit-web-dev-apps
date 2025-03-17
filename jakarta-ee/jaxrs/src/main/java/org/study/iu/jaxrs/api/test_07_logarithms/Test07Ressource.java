package org.study.iu.jaxrs.api.test_07_logarithms;

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

@Path("07")
public class Test07Ressource extends TestRessource {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    
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