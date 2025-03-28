package org.study.iu.jaxrs.classes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;

public abstract class AbstractTestController {
    
    protected static final String THREAD_MODE = System.getenv("THREAD_MODE");

    protected static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
    
    protected abstract JsonObject test(JsonObject jsonInput);
    
    @Resource
    protected ManagedExecutorService managedExecutor;
    protected static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    protected static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private String getProcessingTimeHeaderValue(long startTime) {
        final long endTime = System.nanoTime();
        final double duration = (endTime - startTime) / 1_000_000.0;
        return Double.toString(duration);
    }

    protected JsonObject handleRoute(JsonObject req) {
        ThreadMonitor.countThreads();
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

    protected ExecutorService getExecutor(String threadMode) {
        switch (threadMode) {
            case "V" -> {
                System.out.println("----------V----------");
                return virtualThreadExecutor;
            }
            case "M" -> {
                System.out.println("----------M----------");
                return managedExecutor;
            }
            case "TP" -> {
                System.out.println("----------TP----------");
                return threadPoolExecutor;
            }
            default -> {
                System.out.println("----------NONE----------");
                return null;
            }
        }
    }
}
