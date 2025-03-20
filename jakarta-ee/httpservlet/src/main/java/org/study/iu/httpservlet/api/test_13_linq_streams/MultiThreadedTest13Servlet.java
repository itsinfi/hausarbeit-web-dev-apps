package org.study.iu.httpservlet.api.test_13_linq_streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/13_multi", asyncSupported = true)
public class MultiThreadedTest13Servlet extends SingleThreadedTest13Servlet implements MultiThreadingTestable {

    private void flattenJson(JsonValue json, List<CompletableFuture<Double>> futures, ExecutorService executor) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                jsonObject.values().forEach(value -> flattenJson(value, futures, executor));
            }
            
            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();

                CompletableFuture<Double> avg = CompletableFuture.supplyAsync(() -> {
                    double sum = 0.0;
                    int count = 0;

                    for (JsonValue element : jsonArray) {
                        if (element.getValueType() == JsonValue.ValueType.NUMBER) {
                            sum += ((JsonNumber) element).doubleValue();
                            count++;
                        }
                    }
                    return sum / count;
                }, executor);

                futures.add(avg);
            }
        
            default -> {
            }
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        ExecutorService executor = getExecutor(DEFAULT_TASK_THREAD_MODE);

        final List<CompletableFuture<Double>> futures = Collections.synchronizedList(new ArrayList<>());

        this.flattenJson(jsonInput, futures, executor);

        List<Double> numbers = futures.stream()
                .map(CompletableFuture::join)
                .sorted()
                .collect(Collectors.toList());

        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (double number : numbers) {
            jsonArrayBuilder.add(number);
        }

        final JsonArray result = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("amount", numbers.size())
                .add("result", result)
                .build();
    }
}