package org.study.iu.jaxrs.api.test_11_sort_whole_numbers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.study.iu.jaxrs.classes.TestRessource;

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

@Path("11")
public class Test11Ressource extends TestRessource {

    private static final int DEFAULT_ARRAY_SIZE = 1000;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;
    
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
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final int[] array = new int[arraySize];

        for (int i = 0; i < arraySize; i++) {
            array[i] = RANDOM.nextInt(minValue, maxValue + 1);
        }

        Arrays.sort(array);

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < arraySize; i++) {
            jsonArrayBuilder.add(array[i]);
        }

        final JsonArray result = jsonArrayBuilder.build();
        
        return Json.createObjectBuilder()
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", result)
                .build();
    }
}