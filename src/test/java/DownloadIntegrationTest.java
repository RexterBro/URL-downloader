import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.urldownloader.downloader.Config;
import org.urldownloader.downloader.DownloaderManager;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class DownloadIntegrationTest {
    private static final int PORT = 8000;
    private static final String DOWNLOAD_FILE_PATH =  Resources.getResource("download_image.jpg").getPath();

    private static FileServer fileServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        fileServer = new FileServer();
        fileServer.startServer(PORT, DOWNLOAD_FILE_PATH);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void afterAll() {
        fileServer.stopServer();
    }

    @Test
    @Disabled
    @DisplayName("Downloaded file from a local http server and checked that it's equal to the serverside file")
    void testDownloadTimeUnderMaxTimeShouldSucceed() throws IOException {
        Config config = new Config();
        config.outputDir = ".\\src\\test\\resources".replace("\\", File.separator);
        config.maxTime = 10;
        config.maxConcurrentDownloads = 1;
        config.urls = Collections.singleton(String.format("http://localhost:%d/image", PORT));
        DownloaderManager downloaderManager = new DownloaderManager(config);
        downloaderManager.run();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        File serverFile = new File(DOWNLOAD_FILE_PATH);
        File downloadedFile = new File(Resources.getResource("image").getPath());

        Assertions.assertTrue(FileUtils.contentEquals(serverFile, downloadedFile));
    }
}
