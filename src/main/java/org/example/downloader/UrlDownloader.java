package org.example.downloader;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Getter
public class UrlDownloader implements Runnable {
    private final String url;
    private final String outputDir;

    public UrlDownloader(String url, String outputDir) {
        this.url = url;
        this.outputDir = outputDir;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s\\%s", outputDir, "filebla"))) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

           System.out.println(String.format("finished downloading %s time: %d seconds", url, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
