import com.google.common.io.Resources;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class FileServer {

    private HttpServer server;

    public void startServer(int port, String imagePath) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/image", new ImageHandler(imagePath));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server is listening on port " + port);
    }

    public void stopServer() {
        if (server != null) {
            System.out.println("Closing HTTP server");
            server.stop(0);
        }
    }

    static class ImageHandler implements HttpHandler {

        private final String imagePath;

        ImageHandler(String imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File(imagePath);

            if (!file.exists()) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "image/jpeg");

            exchange.sendResponseHeaders(200, file.length());
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }
}
