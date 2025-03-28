package org.study.iu.httpservlet.api.test_09_read_json;

import java.util.ArrayList;
import java.util.List;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/09", asyncSupported = true)
public class BlockingTest09Servlet extends AbstractTestServlet {

    private void flattenJson(JsonValue json, List<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                jsonObject.values().forEach(value -> this.flattenJson(value, numbers));
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                jsonArray.forEach(element -> this.flattenJson(element, numbers));
            }

            case NUMBER -> {
                numbers.add(((JsonNumber) json).doubleValue());
            }

            default -> {}
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final List<Double> numbers = new ArrayList<>();

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