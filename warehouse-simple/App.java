import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Endpoint: GET /products
        server.createContext("/products", exchange -> {
            String json = "["
                + "{\"id\":1,\"name\":\"Молоток\",\"sku\":\"SKU-001\",\"quantity\":15},"
                + "{\"id\":2,\"name\":\"Отвёртка\",\"sku\":\"SKU-002\",\"quantity\":42},"
                + "{\"id\":3,\"name\":\"Дрель\",\"sku\":\"SKU-003\",\"quantity\":7}"
                + "]";
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        });

        // Endpoint: GET /health
        server.createContext("/health", exchange -> {
            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        });

        server.start();
        System.out.println("Server started on port 8080");
    }
}