import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Simple HTTP server for login and launching the Timetable Generator
 * Serves static files and handles login authentication
 */
public class LoginServer {
    private static final int PORT = 8080;
    private static final String USERNAME = "Admin";
    private static final String PASSWORD = "12345";
    private static Process timetableProcess = null;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        // Serve static files
        server.createContext("/", new StaticFileHandler());

        // Login endpoint
        server.createContext("/login", new LoginHandler());

        server.start();
        System.out.println("========================================");
        System.out.println("Server started on http://localhost:" + PORT);
        System.out.println("Open your browser and go to: http://localhost:" + PORT + "/login.html");
        System.out.println("========================================");
    }

    /**
     * Handles login authentication and launches the timetable app
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read request body
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();

                // Parse credentials
                String username = "";
                String password = "";
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            if ("username".equals(key)) {
                                username = value;
                            } else if ("password".equals(key)) {
                                password = value;
                            }
                        }
                    }
                }

                // Validate credentials
                boolean success = USERNAME.equals(username) && PASSWORD.equals(password);

                if (success) {
                    // Launch timetable generator app
                    launchTimetableApp();

                    // Send success response
                    String response = "{\"success\": true, \"message\": \"Login successful! Launching Timetable Generator...\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    // Send error response
                    String response = "{\"success\": false, \"message\": \"Invalid Username or Password!\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(401, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }

        /**
         * Launches the Timetable Generator application
         */
        private void launchTimetableApp() {
            try {
                // Don't launch if already running
                if (timetableProcess != null && timetableProcess.isAlive()) {
                    System.out.println("Timetable Generator is already running.");
                    return;
                }

                String srcDir = "Automatic-TimeTable-Generator/src";

                // Compile if not already compiled
                File classFile = new File(srcDir + "/TimetableGeneratorApp.class");
                if (!classFile.exists()) {
                    System.out.println("Compiling TimetableGeneratorApp.java...");
                    ProcessBuilder compileProcess = new ProcessBuilder(
                        "javac", "TimetableGeneratorApp.java"
                    );
                    compileProcess.directory(new File(srcDir));
                    compileProcess.inheritIO();
                    Process compile = compileProcess.start();
                    compile.waitFor();
                    System.out.println("Compilation complete.");
                }

                // Launch the application
                System.out.println("Launching Timetable Generator Application...");
                ProcessBuilder pb = new ProcessBuilder(
                    "java", "TimetableGeneratorApp"
                );
                pb.directory(new File(srcDir));
                timetableProcess = pb.start();

                System.out.println("Timetable Generator launched successfully!");
            } catch (Exception e) {
                System.err.println("Error launching Timetable Generator: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Serves static files (HTML, CSS, JS, images)
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Default to login.html
            if ("/".equals(path)) {
                path = "/login.html";
            }

            File file = new File("." + path);

            if (file.exists() && file.isFile()) {
                // Determine content type
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);

                // Send file
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                // 404 Not Found
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        /**
         * Determines content type based on file extension
         */
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".gif")) return "image/gif";
            return "text/plain";
        }
    }
}
