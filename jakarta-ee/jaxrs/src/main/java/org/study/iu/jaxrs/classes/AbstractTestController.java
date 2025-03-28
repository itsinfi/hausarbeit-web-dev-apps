package org.study.iu.jaxrs.classes;

import java.util.concurrent.CompletableFuture;
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

    protected JsonObject handleRoute(JsonObject req) {
        ThreadMonitor.countThreads();
        return test(req);
    }
    
    protected Response sendResponse(JsonObject jsonOutput) {
        return Response.ok(jsonOutput).build();
    }

    protected Response handleError(Throwable ex) {
        ex.printStackTrace();
        return Response
                .serverError()
                .entity(Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build())
                .build();
    }

    protected Response handlePost(JsonObject req) {
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result);
        } catch (Exception ex) {
            return handleError(ex);
        }
    }

    protected CompletableFuture<Response> handlePostAsync(JsonObject req) {
        ExecutorService executor = getExecutor(THREAD_MODE);
        return CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(result))
                .exceptionally(ex -> handleError(ex));
    }
    
    protected Response handlePostSync(JsonObject req) {
        try {
            JsonObject result = handleRoute(req);
            return sendResponse(result);
        } catch (Exception ex) {
            return handleError(ex);
        }
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
