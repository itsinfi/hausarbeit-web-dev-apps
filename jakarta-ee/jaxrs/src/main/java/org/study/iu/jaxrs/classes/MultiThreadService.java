package org.study.iu.jaxrs.classes;

import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MultiThreadService {
    
    @Resource
    private ManagedExecutorService executor;

    public CompletableFuture<String> executeTask() {
        return CompletableFuture.supplyAsync(() -> {
            return "Executed on Physical Thread: " + Thread.currentThread();
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
