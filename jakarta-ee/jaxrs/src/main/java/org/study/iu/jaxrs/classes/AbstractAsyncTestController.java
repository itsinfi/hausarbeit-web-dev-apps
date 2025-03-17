package org.study.iu.jaxrs.classes;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.json.JsonObject;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Response;

public abstract class AbstractAsyncTestController {

    @Resource
    protected ManagedExecutorService mes;

    // private static final boolean USE_CONTAINER_MANAGED_THREAD_POOL = Boolean.parseBoolean(System.getenv("USE_THREAD_POOL"));
    // private static final boolean USE_VIRTUAL_THREADS = Boolean.parseBoolean(System.getenv("USE_VIRTUAL_THREADS"));
    // private static final boolean USE_THREAD_POOL = Boolean.parseBoolean(System.getenv("USE_THREAD_POOL"));
    // private static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    protected <T, R> CompletableFuture<Response> handlePost(T req, Function<T, R> handler) {
        ExecutorService executor = determineExecutor();

        return CompletableFuture.supplyAsync(() -> handler.apply(req), executor)
                .thenApply(result -> Response.ok(result).build())
                .exceptionally(ex -> Response.serverError().entity("Error: " + ex.getMessage()).build());
    }

    private ExecutorService determineExecutor() {
        return threadPool;
        // if (USE_VIRTUAL_THREADS) {
        //     return virtualThreadExecutor;
        // } else if (USE_THREAD_POOL) {
        //     return threadPool;
        // } else {
        //     return mes;
        // }
    }

    // TODO: migrate
    protected void post(@Suspended AsyncResponse res, JsonObject req) throws IOException {};
    
    // TODO: migrate
    protected abstract JsonObject executeTest(JsonObject jsonInput) throws IOException;
}
