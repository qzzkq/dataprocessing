package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Spider implements AutoCloseable {
    private static final String ROOT = "/";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration PROGRESS_PERIOD = Duration.ofSeconds(5);
    private static final ObjectMapper JSON = new ObjectMapper();

    public record Result(List<String> messages, List<String> failed) {
    }

    private final URI base;
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Queue<String> messages = new ConcurrentLinkedQueue<>();
    private final Queue<String> failed = new ConcurrentLinkedQueue<>();

    public Spider(URI base) {
        this.base = base;
    }

    public Result crawl() throws InterruptedException {
        long start = System.nanoTime();
        Thread progress = Thread.ofVirtual().name("progress").start(() -> reportProgress(start));
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            visited.add(ROOT);
            executor.invokeAll(List.of(() -> visit(executor, ROOT)));
        } finally {
            progress.interrupt();
        }
        List<String> sorted = new ArrayList<>(messages);
        sorted.sort(null);
        List<String> failedSorted = new ArrayList<>(failed);
        failedSorted.sort(null);
        return new Result(sorted, failedSorted);
    }

    private Void visit(ExecutorService executor, String path) throws InterruptedException {
        Page page = fetch(path);
        if (page == null) {
            return null;
        }
        messages.add(page.message());
        List<Callable<Void>> children = new ArrayList<>();
        for (String successor : page.successors()) {
            String next = successor.startsWith("/") ? successor : "/" + successor;
            if (visited.add(next)) {
                children.add(() -> visit(executor, next));
            }
        }
        executor.invokeAll(children);
        return null;
    }

    private Page fetch(String path) throws InterruptedException {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(base.resolve(path)).timeout(REQUEST_TIMEOUT).GET().build();
        } catch (IllegalArgumentException e) {
            System.err.printf("%s: invalid path: %s%n", path, e.getMessage());
            failed.add(path);
            return null;
        }
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return JSON.readValue(response.body(), Page.class);
                }
                System.err.printf("%s: HTTP %d (attempt %d)%n", path, response.statusCode(), attempt);
                if (response.statusCode() == 404) {
                    break;
                }
            } catch (IOException e) {
                System.err.printf("%s: %s (attempt %d)%n", path, e, attempt);
            }
            Thread.sleep(500L * attempt);
        }
        failed.add(path);
        return null;
    }

    private void reportProgress(long start) {
        try {
            while (true) {
                Thread.sleep(PROGRESS_PERIOD);
                int done = messages.size();
                int lost = failed.size();
                System.err.printf("[%5.1fs] fetched=%d failed=%d in flight=%d%n",
                        (System.nanoTime() - start) / 1e9, done, lost, visited.size() - done - lost);
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
