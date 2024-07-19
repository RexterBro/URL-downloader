package org.example.downloader;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloaderManager implements Runnable {
    private final List<UrlDownloader> downloaders;
    private final int maxTime;

    private final ExecutorService executorService;

    public DownloaderManager(Config config) {
        downloaders = new ArrayList<>();
        config.urls.forEach(url -> downloaders.add(new UrlDownloader(url, config.outputDir)));
        maxTime = config.maxTime;
        executorService = Executors.newFixedThreadPool(config.maxConcurrentDownloads);
    }

    @SneakyThrows
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> allTasks = getAllDownloadsFuture();
        allTasks.whenComplete((unused, throwable) -> {
            executorService.shutdown();
            System.out.println("finished downloads");
            System.out.printf("Overall time %d seconds",
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
        });
    }

    private CompletableFuture<Void> getAllDownloadsFuture() {
        List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        for (UrlDownloader downloader : downloaders) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(downloader, executorService);
            completableFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    System.out.printf("download of url %s failed%n", downloader.getUrl());
                }
            });
            completableFutures.add(completableFuture);
        }

        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }
}
