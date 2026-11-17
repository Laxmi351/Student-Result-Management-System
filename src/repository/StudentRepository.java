
package repository;

import model.Student;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentRepository {

    private final Path file;

    private static final List<String> SUBJECTS = List.of(
        "Mathematics",
        "Data Structures",
        "OOPS",
        "DBMS",
        "Operating Systems"
    );

    public StudentRepository(Path file) throws IOException {
        this.file = file;

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().encodeToString(
            value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String decode(String value) {
        return new String(
            Base64.getUrlDecoder().decode(value),
            StandardCharsets.UTF_8
        );
    }

    public void add(Student student) throws IOException {

        if (findByRoll(student.getRollNumber()) != null) {
            throw new IllegalArgumentException(
                "Roll number already exists!"
            );
        }

        Map<String, Integer> marks = student.getMarks();

        StringBuilder line = new StringBuilder();

        line.append(encode(student.getName())).append("\t");
        line.append(encode(student.getRollNumber())).append("\t");
        line.append(encode(student.getDepartment())).append("\t");
        line.append(encode(student.getSemester()));

        for (String subject : SUBJECTS) {
            line.append("\t").append(marks.getOrDefault(subject, 0));
        }

        Files.writeString(
            file,
            line + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }

    public List<Student> findAll() throws IOException {

        List<Student> students = new ArrayList<>();

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\t");

            if (parts.length != 9) {
                continue;
            }

            Map<String, Integer> marks = new LinkedHashMap<>();

            for (int i = 0; i < SUBJECTS.size(); i++) {
                marks.put(SUBJECTS.get(i), Integer.parseInt(parts[i + 4]));
            }

            Student student = new Student(
                decode(parts[0]),
                decode(parts[1]),
                decode(parts[2]),
                decode(parts[3]),
                marks
            );

            students.add(student);
        }

        return students;
    }

    public Student findByRoll(String rollNumber) throws IOException {

        for (Student student : findAll()) {
            if (student.getRollNumber().equalsIgnoreCase(rollNumber)) {
                return student;
            }
        }

        return null;
    }
}
