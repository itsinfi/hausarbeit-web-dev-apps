package org.study.iu.jaxrs.api.test_02_static_content;

import java.io.IOException;
import java.security.SecureRandom;

import org.study.iu.jaxrs.classes.TestRessource;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("02")
public class Test02Ressource extends TestRessource {

    private static final int DEFAULT_LENGTH = 1000;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public void post(@Suspended AsyncResponse res, JsonObject req) throws IOException {
        final JsonObject entity = executeTest(req);

        Response response = Response
                .ok()
                .entity(entity)
                .build();

        res.resume(response);
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
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