package org.study.iu.jaxrs.classes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VirtualThreadService {
    
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public CompletableFuture<String> executeTask() {
        return CompletableFuture.supplyAsync(() -> {
            return "Executed on Virtual Thread: " + Thread.currentThread();
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
