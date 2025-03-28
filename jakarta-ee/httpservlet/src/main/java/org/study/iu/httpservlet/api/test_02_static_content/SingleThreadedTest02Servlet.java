package org.study.iu.httpservlet.api.test_02_static_content;

import java.security.SecureRandom;

import org.study.iu.httpservlet.classes.AbstractTestServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/02", asyncSupported = true)
public class SingleThreadedTest02Servlet extends AbstractTestServlet {

    protected static final int DEFAULT_LENGTH = 1000;
    
    protected static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final int length = jsonInput.getInt("length", DEFAULT_LENGTH);

        final StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            stringBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return Json.createObjectBuilder()
                .add("length", length)
                .add("result", stringBuilder.toString())
                .build();
    }
}