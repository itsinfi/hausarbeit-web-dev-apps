package org.study.iu.jaxrs.api.test_01_connection_check;

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

@Path("01")
public class BlockingTest01Controller extends AbstractTestController {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(JsonObject req) {
final long startTime = System.nanoTime();
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result, startTime);
        } catch (Exception ex) {
            return handleError(ex, startTime);
        }
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String name = jsonInput.getString("name");

        return Json.createObjectBuilder()
                .add("result", "Hello " + name + "!")
                .build();
    }
}