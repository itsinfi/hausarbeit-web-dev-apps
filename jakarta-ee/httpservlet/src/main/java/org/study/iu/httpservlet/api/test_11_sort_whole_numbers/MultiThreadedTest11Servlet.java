package org.study.iu.httpservlet.api.test_11_sort_whole_numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/11_multi", asyncSupported = true)
public class MultiThreadedTest11Servlet extends SingleThreadedTest11Servlet implements MultiThreadingTestable {
    private int[] parallelMerge(List<int[]> sortedArrays, ExecutorService executor) {
        while (sortedArrays.size() > 1) {
            final List<int[]> sortedArraysCopy = new ArrayList<>(sortedArrays);

            final List<CompletableFuture<int[]>> mergeFutures = IntStream.range(0, sortedArraysCopy.size() / 2)
                    .mapToObj(a -> CompletableFuture.supplyAsync(
                            () -> merge(sortedArraysCopy.get(2 * a), sortedArraysCopy.get(2 * a + 1)), executor))
                    .collect(Collectors.toList());

            if (sortedArraysCopy.size() % 2 == 1) {
                mergeFutures.add(CompletableFuture.completedFuture(sortedArraysCopy.get(sortedArraysCopy.size() - 1)));
            }

            sortedArrays = mergeFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        }
        return sortedArrays.get(0);
    }

    private int[] merge(int[] left, int[] right) {
        int[] merged = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            merged[k++] = (left[i] < right[j]) ? left[i++] : right[j++];
        }
        while (i < left.length) merged[k++] = left[i++];
        while (j < right.length) merged[k++] = right[j++];
        return merged;
    }

    @Override
    protected JsonObject test(JsonObject jsonInput) {
        final String taskThreadMode = jsonInput.getString("taskThreadMode", DEFAULT_TASK_THREAD_MODE);
        final int threads = jsonInput.getInt("threads", DEFAULT_THREADS);
        ExecutorService executor = getExecutor(taskThreadMode);
        if (executor == null || threads < 1) {
            return super.test(jsonInput);
        }

        final int arraySize = jsonInput.getInt("arraySize", DEFAULT_ARRAY_SIZE);
        final int minValue = jsonInput.getInt("minValue", DEFAULT_MIN_VALUE);
        final int maxValue = jsonInput.getInt("maxValue", DEFAULT_MAX_VALUE);

        Function<Integer, int[]> task = (Integer a) -> {
            int threadArraySize = arraySize / threads;

            if (a == threads - 1) {
                threadArraySize += arraySize % threads;
            }

            int[] threadArray = ThreadLocalRandom.current().ints(threadArraySize, minValue, maxValue + 1).toArray();

            Arrays.sort(threadArray);
            System.out.println("01: This is executed");
            
            return threadArray;
        };

        List<CompletableFuture<int[]>> futures = IntStream
                .range(0, threads)
                .mapToObj(a -> CompletableFuture.supplyAsync(() -> task.apply(a), executor))
                .collect(Collectors.toList());

        System.out.println("02: This is executed");

        List<int[]> sortedArrays = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        System.out.println("03: This is executed");

        int[] sortedArray = parallelMerge(sortedArrays, executor);

        System.out.println("04: This is executed");

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        Arrays.stream(sortedArray).forEach(jsonArrayBuilder::add);

        JsonArray jsonArray = jsonArrayBuilder.build();

        return Json.createObjectBuilder()
                .add("usedThreadMode", taskThreadMode)
                .add("threads", threads)
                .add("arraySize", arraySize)
                .add("minValue", minValue)
                .add("maxValue", maxValue)
                .add("result", jsonArray)
                .build();
    }
}