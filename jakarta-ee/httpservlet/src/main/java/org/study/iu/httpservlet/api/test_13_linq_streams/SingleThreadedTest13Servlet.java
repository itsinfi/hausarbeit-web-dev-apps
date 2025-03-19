package org.study.iu.httpservlet.api.test_13_linq_streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.study.iu.httpservlet.classes.AbstractAsyncTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/13", asyncSupported = true)
public class SingleThreadedTest13Servlet extends AbstractAsyncTestServlet {
    
    protected void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                for (String key : jsonObject.keySet()) {
                    this.flattenJson(jsonObject.get(key), numbers);
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                double sum = 0.0;
                for (JsonValue element : jsonArray) {
                    sum += ((JsonNumber) element).doubleValue();
                }
                double avg = sum / jsonArray.size();
                numbers.add(avg);
            }

            default -> {
            }
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final ArrayList<Double> numbers = new ArrayList<>();

        this.flattenJson(jsonInput, numbers);

        Collections.sort(numbers, Comparator.naturalOrder());

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Double number : numbers) {
            jsonArrayBuilder.add(number);
        }

        final JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("amount", numbers.size())
                .add("result", result)
                .build();
    }
}