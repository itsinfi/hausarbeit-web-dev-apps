package org.study.iu.httpservlet.api.rest;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(value = "/api/rest", asyncSupported = true)
public class RestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            try {
                JsonObject entity = Json.createObjectBuilder()
                .add("msg", "Hello World!")
                .build();

                try (PrintWriter out = resp.getWriter()) {
                    out.print(entity.toString());
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        JsonObject entity = Json.createObjectBuilder()
                .add("msg", "Hello World!")
                .build();
        
        
    }   
}