
package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Student extends Person {

    private String rollNumber;
    private String department;
    private String semester;
    private Map<String, Integer> marks;

    public Student(
            String name,
            String rollNumber,
            String department,
            String semester,
            Map<String, Integer> marks) {

        super(name);

        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
        this.marks = new LinkedHashMap<>(marks);
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public String getSemester() {
        return semester;
    }

    public Map<String, Integer> getMarks() {
        return new LinkedHashMap<>(marks);
    }
}
