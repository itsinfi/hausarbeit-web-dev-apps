package org.study.iu.httpservlet.api.test_13_linq_streams;

import java.util.ArrayList;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/13_multi", asyncSupported = true)
public class MultiThreadedTest13Servlet extends SingleThreadedTest13Servlet implements MultiThreadingTestable {
    
    @Override
    protected void flattenJson(JsonValue json, ArrayList<Double> numbers) {
        switch (json.getValueType()) {
            case OBJECT -> {
                final JsonObject jsonObject = json.asJsonObject();
                for (String key : jsonObject.keySet()) {
                    this.flattenJson(jsonObject.get(key), numbers);
                }
            }

            case ARRAY -> {
                final JsonArray jsonArray = json.asJsonArray();
                double sum = 0.0;
                for (JsonValue element : jsonArray) {
                    sum += ((JsonNumber) element).doubleValue();
                }
                double avg = sum / jsonArray.size();
                numbers.add(avg);
            }

            default -> {
            }
        }
    }
}