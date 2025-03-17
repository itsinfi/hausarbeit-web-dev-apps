package org.study.iu.jaxrs.classes;

import java.io.IOException;

import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class TestServlet extends HttpServlet {
    @Override
    protected abstract void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    
    protected abstract JsonObject executeTest(JsonObject jsonInput) throws IOException;
}
