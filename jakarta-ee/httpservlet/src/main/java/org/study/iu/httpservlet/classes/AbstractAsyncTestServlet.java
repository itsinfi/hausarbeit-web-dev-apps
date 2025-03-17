package org.study.iu.httpservlet.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

public abstract class AbstractAsyncTestServlet extends HttpServlet {
    
    private static final String THREAD_MODE = System.getenv("THREAD_MODE");

    private static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
    
    protected abstract JsonObject executeTest(JsonObject jsonInput);
    
    @Resource
    protected ManagedExecutorService managedExecutor;
    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private JsonObject handleRoute(HttpServletRequest req, HttpServletResponse res) {
        try (
                final InputStream inputStream = req.getInputStream();
                final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))) {
            final JsonObject jsonInput = jsonReader.readObject();
            return executeTest(jsonInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }
    
    private void sendResponse(HttpServletResponse res, JsonObject jsonOutput, AsyncContext asyncContext) {
        try (final PrintWriter out = res.getWriter()) {
            out.print(jsonOutput.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncContext.complete();
        }
    }

    private void handleError(Throwable ex, AsyncContext asyncContext) {
        HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();
        
        JsonObject jsonError = Json.createObjectBuilder()
        .add("error", ex.getMessage())
        .build();
        
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        try (final PrintWriter out = res.getWriter()) {
            out.print(jsonError.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncContext.complete();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        
        AsyncContext asyncContext = req.startAsync();

        ExecutorService executor = null;

        switch (THREAD_MODE) {
            case "VIRTUAL" -> { executor = virtualThreadExecutor; }
            case "MANAGED" -> { executor = managedExecutor; }
            case "THREAD_POOL" -> { executor = threadPoolExecutor; }
            default -> {}
        }
        
        if (executor == null) {
            CompletableFuture.supplyAsync(() -> handleRoute(req, res))
                    .thenApply(result -> sendResponse(res, result, asyncContext))
                    .exceptionally(ex -> handleError(ex, asyncContext));
        }

        CompletableFuture.supplyAsync(() -> handleRoute(req, res), executor)
                .thenApply(result -> sendResponse(res, result, asyncContext))
                .exceptionally(ex -> handleError(ex, asyncContext));
    }

}
