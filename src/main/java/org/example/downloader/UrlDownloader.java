package org.example.downloader;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
class UrlDownloader implements Runnable {
    private final String url;
    private final String outputDir;
    private final AtomicBoolean keepDownload;

    public UrlDownloader(String url, String outputDir) {
        this.url = url;
        this.outputDir = outputDir;
        keepDownload = new AtomicBoolean(true);
    }

    public synchronized void cancelDownload() {
        keepDownload.set(false);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String outputFileName = String.format("%s%s%s", outputDir, File.separator, FilenameUtils.getName(url));

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(outputFileName)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while (keepDownload.get() && ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1)) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }


            fileOutputStream.flush();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (!keepDownload.get()) {
                new File(outputFileName).delete();
            } else {
                System.out.println(String.format("finished downloading %s time: %d seconds",
                        url,
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
            }
        }
    }
}
