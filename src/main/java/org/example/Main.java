package org.example;

import java.net.URI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        String target = args.length > 0 ? args[0] : "8080";
        URI base = URI.create(target.matches("\\d+") ? "http://localhost:" + target : target);

        long start = System.nanoTime();
        Spider.Result result;
        try (Spider spider = new Spider(base)) {
            result = spider.crawl();
        }
        double seconds = (System.nanoTime() - start) / 1e9;

        result.messages().forEach(System.out::println);
        System.err.printf("messages=%d failed=%d time=%.1fs%n", result.messages().size(), result.failed().size(), seconds);
        if (!result.failed().isEmpty()) {
            System.err.println("failed paths: " + result.failed());
            System.exit(1);
        }
    }
}
