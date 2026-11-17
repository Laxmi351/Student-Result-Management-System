
package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.Student;
import model.Result;
import repository.StudentRepository;
import service.ResultService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static StudentRepository repository;
    private static final ResultService resultService = new ResultService();

    private static final List<String> SUBJECTS = List.of(
        "Mathematics",
        "Data Structures",
        "OOPS",
        "DBMS",
        "Operating Systems"
    );

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
        );

        String dataDir = System.getenv().getOrDefault("DATA_DIR", "data");

        repository = new StudentRepository(
            Paths.get(dataDir, "students.tsv")
        );

        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", port), 0
        );

        server.createContext("/api/students", Main::handleStudents);
        server.createContext("/", Main::serveWebsite);

        server.start();

        System.out.println("Student Result System started!");
        System.out.println("Open: http://localhost:" + port);
    }

    private static void handleStudents(HttpExchange exchange)
            throws IOException {

        try {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {

                String query = exchange.getRequestURI().getRawQuery();

                if (query != null && query.startsWith("roll=")) {

                    String roll = URLDecoder.decode(
                        query.substring(5),
                        StandardCharsets.UTF_8
                    );

                    Student student = repository.findByRoll(roll);

                    if (student == null) {
                        sendJson(exchange, 404,
                            "{\"error\":\"Student not found\"}");
                    } else {
                        sendJson(exchange, 200, studentJson(student));
                    }

                } else {

                    List<Student> students = repository.findAll();
                    StringBuilder json = new StringBuilder("[");

                    for (int i = 0; i < students.size(); i++) {
                        if (i > 0) json.append(",");
                        json.append(studentJson(students.get(i)));
                    }

                    json.append("]");
                    sendJson(exchange, 200, json.toString());
                }

            } else if (method.equals("POST")) {

                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                Map<String, String> form = parseForm(body);

                String name = form.get("name");
                String roll = form.get("rollNumber");
                String department = form.get("department");
                String semester = form.get("semester");

                if (name == null || name.isBlank()
                    || roll == null || roll.isBlank()
                    || department == null || department.isBlank()
                    || semester == null || semester.isBlank()) {

                    sendJson(exchange, 400,
                        "{\"error\":\"Please fill all student details\"}");
                    return;
                }

                Map<String, Integer> marks = new LinkedHashMap<>();

                for (int i = 0; i < SUBJECTS.size(); i++) {
                    int mark = Integer.parseInt(form.get("mark" + i));
                    marks.put(SUBJECTS.get(i), mark);
                }

                Student student = new Student(
                    name, roll, department, semester, marks
                );

                resultService.calculate(marks);
                repository.add(student);

                sendJson(exchange, 201, studentJson(student));

            } else {
                sendJson(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            }

        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400,
                "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            sendJson(exchange, 500,
                "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static Map<String, String> parseForm(String body) {

        Map<String, String> data = new LinkedHashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                parts[0], StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                : "";

            data.put(key, value);
        }

        return data;
    }

    private static String studentJson(Student student) {

        Result result = resultService.calculate(student.getMarks());

        StringBuilder json = new StringBuilder("{");

        json.append("\"name\":\"").append(escape(student.getName())).append("\",");
        json.append("\"rollNumber\":\"").append(escape(student.getRollNumber())).append("\",");
        json.append("\"department\":\"").append(escape(student.getDepartment())).append("\",");
        json.append("\"semester\":\"").append(escape(student.getSemester())).append("\",");
        json.append("\"marks\":{");

        Map<String, Integer> marks = student.getMarks();

        for (int i = 0; i < SUBJECTS.size(); i++) {
            String subject = SUBJECTS.get(i);

            if (i > 0) json.append(",");

            json.append("\"").append(escape(subject)).append("\":")
                .append(marks.getOrDefault(subject, 0));
        }

        json.append("},");
        json.append("\"total\":").append(result.getTotal()).append(",");
        json.append("\"percentage\":").append(result.getPercentage()).append(",");
        json.append("\"grade\":\"").append(result.getGrade()).append("\",");
        json.append("\"status\":\"").append(result.getStatus()).append("\"");

        json.append("}");

        return json.toString();
    }

    private static String escape(String value) {

        if (value == null) return "";

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private static void sendJson(
            HttpExchange exchange, int status, String response)
            throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void serveWebsite(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        Path webDir = Paths.get("web").toAbsolutePath().normalize();

        String requested = exchange.getRequestURI().getPath();

        if (requested.equals("/")) {
            requested = "/index.html";
        }

        Path file = webDir.resolve(
            requested.substring(1)
        ).normalize();

        if (!file.startsWith(webDir) || !Files.exists(file)
                || Files.isDirectory(file)) {

            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        String contentType = "text/plain; charset=UTF-8";
        String filename = file.toString();

        if (filename.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (filename.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (filename.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        }

        byte[] bytes = Files.readAllBytes(file);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
