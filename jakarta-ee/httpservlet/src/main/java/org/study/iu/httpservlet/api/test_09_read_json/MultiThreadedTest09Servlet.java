package org.study.iu.httpservlet.api.test_09_read_json;

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

@WebServlet(value = "/api/09_multi", asyncSupported = true)
public class MultiThreadedTest09Servlet extends SingleThreadedTest09Servlet implements MultiThreadingTestable {

    private List<Double> flattenJson(JsonValue json, ExecutorService executor) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();

                final List<CompletableFuture<List<Double>>> futures = jsonObject.values()
                        .stream()
                        .map(value -> CompletableFuture.supplyAsync(() -> flattenJson(value, executor), executor))
                        .collect(Collectors.toList());

                return futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                
                final List<CompletableFuture<List<Double>>> futures = jsonArray.stream()
                        .map(element -> CompletableFuture.supplyAsync(() -> flattenJson(element, executor), executor))
                        .collect(Collectors.toList());
                
                return futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }

            case NUMBER -> {
                return List.of(((JsonNumber) json).doubleValue());
            }

            default -> {
                return List.of();
            }
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final ExecutorService executor = getExecutor(DEFAULT_TASK_THREAD_MODE);

        final List<Double> numbers = flattenJson(jsonInput, executor);

        final JsonArray result = numbers.stream()
                .reduce(Json.createArrayBuilder(), JsonArrayBuilder::add, (a, b) -> a)
                .build();

        return Json.createObjectBuilder()
                .add("found", result.size())
                .add("result", result)
                .build();
    }
}