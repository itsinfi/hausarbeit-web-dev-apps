package org.study.iu.httpservlet.api.test_11_sort_whole_numbers;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import org.study.iu.httpservlet.classes.TestServlet;

@WebServlet(value = "/api/11", asyncSupported = true)
public class Test11Servlet extends TestServlet {

    private static final int DEFAULT_ARRAY_SIZE = 1000;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;
    
    private static final Random RANDOM = new Random();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            try (
                    final InputStream inputStream = req.getInputStream();
                    final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))) {
                final JsonObject jsonInput = jsonReader.readObject();

                final JsonObject jsonOutput = executeTest(jsonInput);

                try (final PrintWriter out = resp.getWriter()) {
                    out.print(jsonOutput.toString());
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                asyncContext.complete();
            }
        });
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        final int[] array = new int[arraySize];

        for (int i = 0; i < arraySize; i++) {
            array[i] = RANDOM.nextInt(minValue, maxValue + 1);
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