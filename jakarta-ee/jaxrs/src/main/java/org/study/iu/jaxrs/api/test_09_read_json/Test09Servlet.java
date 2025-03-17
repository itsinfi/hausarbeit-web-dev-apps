package org.study.iu.jaxrs.api.test_09_read_json;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.study.iu.jaxrs.classes.TestServlet;

@WebServlet(value = "/api/09", asyncSupported = true)
public class Test09Servlet extends TestServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            try (
                final InputStream inputStream = req.getInputStream();
                final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))
            ) {
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
                for (JsonValue element : jsonArray) {
                    this.flattenJson(element, numbers);
                }
                break;

            case NUMBER:
                numbers.add(((JsonNumber) json).doubleValue());
                break;

            default:
                break;
        }
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
        final ArrayList<Double> numbers = new ArrayList<Double>();

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