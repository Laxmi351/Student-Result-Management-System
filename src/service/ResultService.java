
package service;

import model.Result;
import java.util.Map;

public class ResultService {

    public Result calculate(Map<String, Integer> marks) {

        int total = 0;
        boolean passed = true;

        for (int mark : marks.values()) {

            total += mark;

            if (mark < 40) {
                passed = false;
            }
        }

        double percentage = (total / 500.0) * 100;

        String grade;

        if (!passed) {
            grade = "F";
        } else if (percentage >= 90) {
            grade = "O";
        } else if (percentage >= 80) {
            grade = "A+";
        } else if (percentage >= 70) {
            grade = "A";
        } else if (percentage >= 60) {
            grade = "B+";
        } else if (percentage >= 50) {
            grade = "B";
        } else {
            grade = "C";
        }

        String status = passed ? "PASS" : "FAIL";

        return new Result(
            total,
            percentage,
            grade,
            status
        );
    }
}
