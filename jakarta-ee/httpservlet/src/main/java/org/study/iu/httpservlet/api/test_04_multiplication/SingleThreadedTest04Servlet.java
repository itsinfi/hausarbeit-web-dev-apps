package org.study.iu.httpservlet.api.test_04_multiplication;

import java.util.Random;

import org.study.iu.httpservlet.classes.AbstractAsyncTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/04", asyncSupported = true)
public class SingleThreadedTest04Servlet extends AbstractAsyncTestServlet {
    
    protected static final int DEFAULT_ITERATIONS = 1000;
    protected static final int DEFAULT_LOWER_BOUND = 1;
    protected static final int DEFAULT_UPPER_BOUND = 2;
    
    private static final Random RANDOM = new Random();
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double product = 1.0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            product *= randomRealNumber;
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", product)
                .build();
    }
}