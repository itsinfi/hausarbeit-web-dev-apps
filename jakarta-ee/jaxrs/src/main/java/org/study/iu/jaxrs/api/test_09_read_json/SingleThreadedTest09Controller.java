package org.study.iu.jaxrs.api.test_09_read_json;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractTestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("09")
public class SingleThreadedTest09Controller extends AbstractTestController {

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

    private void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                jsonObject.values().forEach(value -> this.flattenJson(value, numbers));
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                jsonArray.forEach(element -> this.flattenJson(element, numbers));
            }

            case NUMBER -> numbers.add(((JsonNumber) json).doubleValue());

            default -> {}
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final ArrayList<Double> numbers = new ArrayList<>();

        this.flattenJson(jsonInput, numbers);

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Double number : numbers) {
            jsonArrayBuilder.add(number);
        }

        final JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}