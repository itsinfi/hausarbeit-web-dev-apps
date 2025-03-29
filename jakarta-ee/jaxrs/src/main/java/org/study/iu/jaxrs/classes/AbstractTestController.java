package org.study.iu.jaxrs.classes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;

public abstract class AbstractTestController {
    private static final int EVENT_LOOP_THREAD_POOL_SIZE = Integer.parseInt(System.getenv("EVENT_LOOP_THREAD_POOL_SIZE"));
    protected static final int OPERATIONAL_THREAD_POOL_SIZE = 
            Integer.parseInt(System.getenv("OPERATIONAL_THREAD_POOL_SIZE")) *
            Integer.parseInt(System.getenv("CLUSTER_COUNT"));
    
    protected static final ExecutorService EVENT_LOOP_THREAD_POOL = Executors.newFixedThreadPool(EVENT_LOOP_THREAD_POOL_SIZE);
    
    protected abstract JsonObject test(JsonObject jsonInput);

    private String getProcessingTimeHeaderValue(long startTime) {
        final long endTime = System.nanoTime();
        final double duration = (endTime - startTime) / 1_000_000.0;
        return Double.toString(duration);
    }

    protected JsonObject handleRoute(JsonObject req) {
        // ThreadMonitor.countThreads();
        return test(req);
    }
    
    protected Response handleError(Throwable ex, long startTime) {
        ex.printStackTrace();
        return Response
                .serverError()
                .header("processing-time", getProcessingTimeHeaderValue(startTime))
                .entity(Json.createObjectBuilder()
                        .add("error", ex.getMessage()))
                        .build();
    }
    
    protected Response sendResponse(JsonObject jsonOutput, long startTime) {
        return Response.ok(jsonOutput)
                .header("processing-time", getProcessingTimeHeaderValue(startTime))
                .build();
    }
}
