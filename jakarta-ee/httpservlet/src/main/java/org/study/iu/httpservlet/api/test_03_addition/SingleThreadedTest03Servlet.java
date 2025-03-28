package org.study.iu.httpservlet.api.test_03_addition;

import java.util.Random;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/03", asyncSupported = true)
public class SingleThreadedTest03Servlet extends AbstractTestServlet {
    
    protected static final int DEFAULT_ITERATIONS = 1000;
    protected static final int DEFAULT_LOWER_BOUND = 0;
    protected static final int DEFAULT_UPPER_BOUND = 1;
    
    private static final Random RANDOM = new Random();
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        final int lowerBound = jsonInput.getInt("lowerBound", DEFAULT_LOWER_BOUND);
        final int upperBound = jsonInput.getInt("upperBound", DEFAULT_UPPER_BOUND);
        
        double sum = 0.0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble(lowerBound, upperBound);
            sum += randomRealNumber;
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("lowerBound", lowerBound)
                .add("upperBound", upperBound)
                .add("result", sum)
                .build();
    }
}