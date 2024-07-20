package org.example.downloader;

import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.*;

/**
 * A Download Manager which concurrently downloads urls
 */
public class DownloaderManager implements Runnable {
    private final Map<UrlDownloader, CompletableFuture<?>> downloaderToFuture;
    private final Config config;
    private final ExecutorService executorService;

    public DownloaderManager(Config config) {
        downloaderToFuture = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(config.maxConcurrentDownloads);
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void run() {
        if (config.urls.isEmpty()) {
            System.out.println("no urls to download");
            return;
        }

        long startTime = System.currentTimeMillis();

        ScheduledExecutorService maxtimeCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        setupMaxTimeChecker(maxtimeCheckExecutor);

        startDownloading();
        CompletableFuture<Void> downloadTask = CompletableFuture.allOf(downloaderToFuture.values().toArray(new CompletableFuture[0]));
        downloadTask.whenComplete((unused, throwable) -> {
            handleAllDownloadsComplete(maxtimeCheckExecutor);
            System.out.println(String.format("Overall time %d seconds", (System.currentTimeMillis() - startTime) / 1000));
        });
    }

    private void setupMaxTimeChecker(ScheduledExecutorService maxtimeCheckExecutor) {
        maxtimeCheckExecutor.schedule(() ->
                downloaderToFuture.forEach((urlDownloader, completableFuture) -> {
                    if (!completableFuture.isDone()) {
                        System.out.println(String.format("Cancelling download %s due to passing the maximum allowed time", urlDownloader.getUrl()));
                        urlDownloader.cancelDownload();
                    }
                }), config.maxTime, TimeUnit.SECONDS);
    }

    private void startDownloading() {
        for (String url : config.urls) {
            UrlDownloader downloader = new UrlDownloader(url, config.outputDir);
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(downloader, executorService);
            completableFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    System.out.println(String.format("download of url %s failed", downloader.getUrl()));
                }
            });

            downloaderToFuture.put(downloader, completableFuture);
        }
    }

    private void handleAllDownloadsComplete(ScheduledExecutorService maxtimeCheckExecutor) {
        maxtimeCheckExecutor.shutdownNow();
        executorService.shutdown();
        System.out.println("finished downloads");
    }
}
