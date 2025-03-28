package org.study.iu.httpservlet.api.test_01_connection_check;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/01", asyncSupported = true)
public class BlockingTest01Servlet extends AbstractTestServlet {
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String name = jsonInput.getString("name");

        return Json.createObjectBuilder()
                .add("result", "Hello " + name + "!")
                .build();
    }
}