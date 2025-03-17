package org.study.iu.jaxrs.api.test_09_read_json;

import java.io.IOException;
import java.util.ArrayList;

import org.study.iu.jaxrs.classes.TestRessource;

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
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("09")
public class Test09Ressource extends TestRessource {

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

    private void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                for (String key : jsonObject.keySet()) {
                    this.flattenJson(jsonObject.get(key), numbers);
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                for (JsonValue element : jsonArray) {
                    this.flattenJson(element, numbers);
                }
            }

            case NUMBER -> numbers.add(((JsonNumber) json).doubleValue());

            default -> {}
        }
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
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