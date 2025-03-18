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

public abstract class AbstractAsyncTestServlet extends HttpServlet {
    
    private static final String THREAD_MODE = System.getenv("THREAD_MODE");

    private static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
    
    protected abstract JsonObject executeTest(JsonObject jsonInput);
    
    @Resource
    protected ManagedExecutorService managedExecutor;
    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private JsonObject handleRoute(HttpServletRequest req) {
        ThreadMonitor.countThreads();
        try (
            final InputStream inputStream = req.getInputStream();
            final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))
        ) {
            final JsonObject jsonInput = jsonReader.readObject();
            return executeTest(jsonInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }
    
    private Void sendResponse(HttpServletResponse res, JsonObject jsonOutput, AsyncContext asyncContext) {
        ThreadMonitor.countThreads();
        try (final PrintWriter out = res.getWriter()) {
            out.print(jsonOutput.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncContext.complete();
        }
        return null;
    }

    private Void handleError(Throwable ex, AsyncContext asyncContext) {
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
        return null;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        
        AsyncContext asyncContext = req.startAsync();
        // ThreadMonitor.printAllThreads();

        ExecutorService executor = null;

        switch (THREAD_MODE) {
            case "V" -> {
                System.out.println("----------V----------");
                executor = virtualThreadExecutor;
            }
            case "M" -> {
                System.out.println("----------M----------");
                executor = managedExecutor;
            }
            case "TP" -> {
                System.out.println("----------TP----------");
                executor = threadPoolExecutor;
            }
            default -> {
                System.out.println("----------NONE----------");
            }
        }

        CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(res, result, asyncContext))
                .exceptionally(ex -> handleError(ex, asyncContext));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        // ThreadMonitor.printAllThreads();
        // ThreadMonitor.countThreads();
    }

}
