package org.study.iu.httpservlet.api.test_09_read_json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/09_multi", asyncSupported = true)
public class MultiTest09Servlet extends Test09Servlet implements MultiThreadingTestable {

    @Override
    protected void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (String key : jsonObject.keySet()) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        this.flattenJson(jsonObject.get(key), numbers);
                    }));
                }
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (JsonValue element : jsonArray) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        this.flattenJson(element, numbers);
                    }));
                }
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            }

            case NUMBER -> {
                synchronized (numbers) {
                    numbers.add(((JsonNumber) json).doubleValue());
                }
            }

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