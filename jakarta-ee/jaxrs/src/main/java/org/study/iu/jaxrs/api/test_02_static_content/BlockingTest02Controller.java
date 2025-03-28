package org.study.iu.jaxrs.api.test_02_static_content;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

import org.study.iu.jaxrs.classes.AbstractTestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("02")
public class BlockingTest02Controller extends AbstractTestController {

    private static final int DEFAULT_LENGTH = 1000;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(JsonObject req) {
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result);
        } catch (Exception ex) {
            return handleError(ex);
        }
    }
    
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