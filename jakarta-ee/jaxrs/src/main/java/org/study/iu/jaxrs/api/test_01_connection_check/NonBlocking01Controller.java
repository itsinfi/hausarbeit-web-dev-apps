package org.study.iu.jaxrs.api.test_01_connection_check;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.study.iu.jaxrs.classes.AbstractTestController;
import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("01_multi")
public class NonBlocking01Controller extends AbstractTestController implements MultiThreadingTestable {
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CompletableFuture<Response> post(JsonObject req) {
        final long startTime = System.nanoTime();
        return CompletableFuture.supplyAsync(() -> handleRoute(req), EVENT_LOOP_THREAD_POOL)
                .thenApply(result -> sendResponse(result, startTime))
                .exceptionally(ex -> handleError(ex, startTime));
    }
    
    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String name = jsonInput.getString("name");

        return Json.createObjectBuilder()
                .add("result", "Hello " + name + "!")
                .build();
    }
}
