package org.urldownloader;

import org.urldownloader.downloader.Config;
import org.urldownloader.downloader.DownloaderManager;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (0 < args.length) {
            File configFile = new File(args[0]);
            try {
                DownloaderManager downloaderManager = new DownloaderManager(Config.fromJsonFile(configFile));
                downloaderManager.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Invalid arguments, usage: UrlDownload <config-file>");
        }
    }
}