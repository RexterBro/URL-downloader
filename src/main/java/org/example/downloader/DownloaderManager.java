package org.example.downloader;

import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.*;

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
        long startTime = System.currentTimeMillis();
        initDownloaders();
        CompletableFuture<Void> downloadTask = CompletableFuture.allOf(downloaderToFuture.values().toArray(new CompletableFuture[0]));

        ScheduledExecutorService maxtimeCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        maxtimeCheckExecutor.schedule(() ->
                downloaderToFuture.forEach((urlDownloader, completableFuture) -> {
                    if (!completableFuture.isDone()) {
                        System.out.println(String.format("Cancelling download %s due to passing the maximum allowed time", urlDownloader.getUrl()));
                        urlDownloader.cancelDownload();
                    }
                }), config.maxTime, TimeUnit.SECONDS);

        downloadTask.whenComplete((unused, throwable) -> {
            maxtimeCheckExecutor.shutdownNow();
            executorService.shutdown();
            System.out.println("finished downloads");
            System.out.println(String.format("Overall time %d seconds",
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
        });
    }

    private void initDownloaders() {
        for (String url : config.urls) {
            UrlDownloader downloader = new UrlDownloader(url, config.outputDir);
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(downloader, executorService);
            completableFuture.whenComplete((unused, throwable) -> {
                if(throwable!=null) {
                    System.out.println(String.format("download of url %s failed", downloader.getUrl()));
                }
            });
            downloaderToFuture.put(downloader, completableFuture);
        }
    }
}
