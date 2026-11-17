
package model;

public class Result {

    private int total;
    private double percentage;
    private String grade;
    private String status;

    public Result(int total, double percentage,
                  String grade, String status) {

        this.total = total;
        this.percentage = percentage;
        this.grade = grade;
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getGrade() {
        return grade;
    }

    public String getStatus() {
        return status;
    }
}
