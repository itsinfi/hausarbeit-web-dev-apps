package org.study.iu.httpservlet.api.test_12_sort_real_numbers;

import java.util.Arrays;
import java.util.Random;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/12", asyncSupported = true)
public class BlockingTest12Servlet extends AbstractTestServlet {

    protected static final int DEFAULT_ARRAY_SIZE = 1000;
    protected static final int DEFAULT_MIN_VALUE = 0;
    protected static final int DEFAULT_MAX_VALUE = 1000;
    
    private static final Random RANDOM = new Random();
    
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