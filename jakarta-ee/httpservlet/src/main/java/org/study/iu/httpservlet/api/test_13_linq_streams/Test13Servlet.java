package org.study.iu.httpservlet.api.test_13_linq_streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.study.iu.httpservlet.classes.TestServlet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/13", asyncSupported = true)
public class Test13Servlet extends TestServlet {

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
    
    private void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT:
                final JsonObject jsonObject = json.asJsonObject();
                for (String key : jsonObject.keySet()) {
                    this.flattenJson(jsonObject.get(key), numbers);
                }
                break;

            case ARRAY:
                final JsonArray jsonArray = json.asJsonArray();
                double sum = 0.0;
                for (JsonValue element : jsonArray) {
                    sum += ((JsonNumber) element).doubleValue();
                }
                double avg = sum / jsonArray.size();
                numbers.add(avg);
                break;

            default:
                break;
        }
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
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