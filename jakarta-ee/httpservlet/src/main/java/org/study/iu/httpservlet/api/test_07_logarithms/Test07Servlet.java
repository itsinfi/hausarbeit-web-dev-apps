package org.study.iu.httpservlet.api.test_07_logarithms;

import java.util.Random;

import org.study.iu.httpservlet.classes.AbstractAsyncTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/07", asyncSupported = true)
public class Test07Servlet extends AbstractAsyncTestServlet {
    
    private static final int DEFAULT_ITERATIONS = 1000;
    
    private static final Random RANDOM = new Random();
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
        final int iterations = jsonInput.getInt("iterations", DEFAULT_ITERATIONS);
        
        int finiteCount = 0;

        for (int i = 0; i < iterations; i++) {
            final double randomRealNumber = RANDOM.nextDouble() < 0.5
                    ? RANDOM.nextDouble() * 1.0e-100
                    : RANDOM.nextDouble() * 1.0e100;

            final double result = Math.log(randomRealNumber);

            if (Double.isFinite(result)) {
                finiteCount++;
            }
        }

        return Json.createObjectBuilder()
                .add("iterations", iterations)
                .add("result", finiteCount)
                .build();
    }
}