import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class App {
    // Читаем настройки из переменных окружения (Docker Compose передаст их)
    static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "5432");
    static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "app");
    static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    static final String DB_PASS = System.getenv().getOrDefault("DB_PASS", "1234");

    static String getDbUrl() {
        return "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found!");
            System.exit(1);
        }

        initDb();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                if (path.equals("/products") && method.equals("GET")) {
                    handleGetProducts(exchange);
                } else if (path.equals("/products") && method.equals("POST")) {
                    handlePostProduct(exchange);
                } else if (path.matches("/products/\\d+") && method.equals("PUT")) {
                    handlePutProduct(exchange, path);
                } else if (path.matches("/products/\\d+") && method.equals("DELETE")) {
                    handleDeleteProduct(exchange, path);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.start();
        System.out.println("API Server started on port 8080");
    }

    static void initDb() {
        try (Connection conn = DriverManager.getConnection(getDbUrl(), DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "quantity INT NOT NULL)");
        } catch (SQLException e) {
            System.err.println("DB Init Error: " + e.getMessage());
        }
    }

    static void handleGetProducts(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("[");
        try (Connection conn = DriverManager.getConnection(getDbUrl(), DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, quantity FROM products ORDER BY id")) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"quantity\":%d}",
                        rs.getInt("id"), rs.getString("name"), rs.getInt("quantity")));
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        json.append("]");
        sendResponse(exchange, 200, json.toString());
    }

    static void handlePostProduct(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        
        try {
            String name = extractJsonValue(body, "name");
            int qty = Integer.parseInt(extractJsonValue(body, "quantity"));

            try (Connection conn = DriverManager.getConnection(getDbUrl(), DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products (name, quantity) VALUES (?, ?) RETURNING id")) {
                pstmt.setString(1, name);
                pstmt.setInt(2, qty);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    sendResponse(exchange, 201, String.format("{\"id\":%d,\"name\":\"%s\",\"quantity\":%d}", rs.getInt(1), name, qty));
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("JSON Parse error: " + e.getMessage());
        }
        sendResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}");
    }

    static void handlePutProduct(HttpExchange exchange, String path) throws IOException {
        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            int qty = Integer.parseInt(extractJsonValue(body, "quantity"));
            try (Connection conn = DriverManager.getConnection(getDbUrl(), DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?")) {
                pstmt.setInt(1, qty);
                pstmt.setInt(2, id);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    sendResponse(exchange, 200, "{\"status\":\"updated\"}");
                    return;
                }
            }
        } catch (Exception e) {
             System.out.println("JSON Parse error: " + e.getMessage());
        }
        sendResponse(exchange, 404, "{\"error\":\"Not found or invalid data\"}");
    }

    static void handleDeleteProduct(HttpExchange exchange, String path) throws IOException {
        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        try (Connection conn = DriverManager.getConnection(getDbUrl(), DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                sendResponse(exchange, 200, "{\"status\":\"deleted\"}");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
    }

    // Простой и надёжный парсер для нашего конкретного случая
    static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) throw new IllegalArgumentException("Key not found: " + key);

        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) throw new IllegalArgumentException("Colon not found");

        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (json.charAt(valueStart) == '"') {
            // String value
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) throw new IllegalArgumentException("Closing quote not found");
            return json.substring(valueStart + 1, valueEnd);
        } else {
            // Number value
            int valueEnd = valueStart;
            while (valueEnd < json.length() && (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '-')) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd);
        }
    }

    static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}