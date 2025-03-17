package org.study.iu.jaxrs.api.threading_test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/test")
public class ThreadTestRessource {

    @Resource
    private ManagedExecutorService managedExecutorService;

    private final ExecutorService virtualExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String runThreadTests() throws ExecutionException, InterruptedException {
        int input = 5; // Beispiel: Berechnung von 5²

        // 1️⃣ Variante: ManagedExecutorService
        Future<Integer> managedFuture = managedExecutorService.submit(() -> input * input);
        int managedResult = managedFuture.get(); // Wartet auf das Ergebnis

        // 2️⃣ Variante: Virtual Thread
        Future<Integer> virtualFuture = virtualExecutorService.submit(() -> input * input);
        int virtualResult = virtualFuture.get();

        // 3️⃣ Variante: Hybrid (ManagedExecutorService startet Virtual Thread)
        Future<Integer> hybridFuture = managedExecutorService.submit(() -> {
            try {
                return virtualExecutorService.submit(() -> input * input).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        int hybridResult = hybridFuture.get();

        // Ergebnisse als String zurückgeben
        return String.format("""
            ManagedExecutorService: %d
            Virtual Thread: %d
            Hybrid Ansatz: %d
            """, managedResult, virtualResult, hybridResult);
    }

    public void shutdown() {
        virtualExecutorService.shutdown();
    }
}
