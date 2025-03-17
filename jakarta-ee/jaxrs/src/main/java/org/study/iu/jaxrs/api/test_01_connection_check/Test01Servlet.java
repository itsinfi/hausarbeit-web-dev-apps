package org.study.iu.jaxrs.api.test_01_connection_check;

import jakarta.json.Json;
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

import org.study.iu.jaxrs.classes.TestServlet;

@WebServlet(value = "/api/01", asyncSupported = true)
public class Test01Servlet extends TestServlet {

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

                if ((jsonInput).containsKey("name")) {
                    final JsonObject jsonOutput = executeTest(jsonInput);

                    try (final PrintWriter out = resp.getWriter()) {
                        out.print(jsonOutput.toString());
                        out.flush();
                    }
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
        final String name = jsonInput.getString("name");

        return Json.createObjectBuilder()
                .add("result", "Hello " + name + "!")
                .build();
    }
}