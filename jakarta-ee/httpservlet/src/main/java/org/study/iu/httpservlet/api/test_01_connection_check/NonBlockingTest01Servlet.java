package org.study.iu.httpservlet.api.test_01_connection_check;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(value = "/api/01_multi", asyncSupported = true)
public class NonBlockingTest01Servlet extends BlockingTest01Servlet implements MultiThreadingTestable {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        final long startTime = System.nanoTime();
        
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        final AsyncContext asyncContext = req.startAsync();

        final ExecutorService executor = getExecutor(THREAD_MODE);

        CompletableFuture.supplyAsync(() -> handleRoute(req), executor)
                .thenApply(result -> sendResponse(res, result, asyncContext, startTime))
                .exceptionally(ex -> handleError(ex, asyncContext, startTime));

        return;
    }
}