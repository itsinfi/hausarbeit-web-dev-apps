package org.study.iu.jaxrs.classes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HybridThreadService {
    
    @Resource
    private ManagedExecutorService managedExecutorService;

    private final ExecutorService virtualExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    public CompletableFuture<String> executeTask() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return virtualExecutorService.submit(() -> "Task ausgeführt!").get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, (ExecutorService) managedExecutorService);
    }

    public void shutdown() {
        managedExecutorService.shutdown();
    }
}
