package org.study.iu.httpservlet.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public abstract class AbstractTestServlet extends HttpServlet {

    protected static final String THREAD_MODE = System.getenv("THREAD_MODE");
    protected static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE")) * 4 + 8;
    
    protected abstract JsonObject test(JsonObject jsonInput);
    
    @Resource
    protected ManagedExecutorService managedExecutor;
    protected static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    protected static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private void addProcessingTimeHeader(long startTime, HttpServletResponse res) {
        final long endTime = System.nanoTime();
        final double duration = (endTime - startTime) / 1_000_000.0;
        res.setHeader("processing-time", Double.toString(duration));
    }

    protected JsonObject handleRoute(HttpServletRequest req) {
        // ThreadMonitor.countThreads();
        try (
            final InputStream inputStream = req.getInputStream();
            final JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, "UTF-8"))
        ) {
            final JsonObject jsonInput = jsonReader.readObject();
            return test(jsonInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }
    
    protected Void sendResponse(HttpServletResponse res, JsonObject jsonOutput, final AsyncContext asyncContext, long startTime) {
        // ThreadMonitor.countThreads();
        try (final PrintWriter out = res.getWriter()) {
            out.print(jsonOutput.toString());
            addProcessingTimeHeader(startTime, res);
            out.flush();
        } catch (Exception e) {
            addProcessingTimeHeader(startTime, res);
            e.printStackTrace();
        } finally {
            if (asyncContext != null) {
                asyncContext.complete();
            }
        }
        return null;
    }

    protected Void handleError(Throwable ex, final AsyncContext asyncContext, long startTime) {
        HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();

        ex.printStackTrace();
        
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
            if (asyncContext != null) {
                asyncContext.complete();
            }

            addProcessingTimeHeader(startTime, res);
        }
        return null;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        final long startTime = System.nanoTime();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        
        try {
            JsonObject result = handleRoute(req);
            sendResponse(res, result, null, startTime);
        } catch (Exception ex) {
            handleError(ex, null, startTime);
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
