package org.study.iu.jaxrs.classes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;

public abstract class AbstractAsyncTestController {
    
    private static final String THREAD_MODE = System.getenv("THREAD_MODE");

    private static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
    
    protected abstract JsonObject executeTest(JsonObject jsonInput);
    
    @Resource
    protected ManagedExecutorService managedExecutor;
    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    
    // private handleError(Throwable ex, AsyncContext asyncContext) {}

    protected CompletableFuture<Response> handlePost(JsonObject req) {
        ExecutorService executor = null;

        switch (THREAD_MODE) {
            case "VIRTUAL" -> {
                executor = virtualThreadExecutor;
            }
            case "MANAGED" -> {
                executor = managedExecutor;
            }
            case "THREAD_POOL" -> {
                executor = threadPoolExecutor;
            }
            default -> {
            }
        }

        if (executor == null) {
            return CompletableFuture.runAsync(() -> executeTest(req))
                    .thenApply(result -> Response.ok(result).build())
                    .exceptionally(ex -> Response.serverError().entity("Error: " + ex.getMessage()).build());
        }

        return CompletableFuture.supplyAsync(() -> executeTest(req), executor)
                .thenApply(result -> Response.ok(result).build())
                .exceptionally(ex -> Response.serverError().entity("Error: " + ex.getMessage()).build());
    }
}
