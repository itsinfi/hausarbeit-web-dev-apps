package org.study.iu.jaxrs.api.test_12_sort_real_numbers;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractTestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("12")
public class SingleThreadedTest12Controller extends AbstractTestController {

    private static final int DEFAULT_ARRAY_SIZE = 1000;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;
    
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
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final double[] array = new double[arraySize];

        for (int i = 0; i < arraySize; i++) {
            array[i] = RANDOM.nextDouble(minValue, maxValue + 1);
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