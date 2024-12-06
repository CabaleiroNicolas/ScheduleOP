package OptaTest.OptaPlanner.Prueba.dto;

import java.io.Serializable;

public class ScheduleAssignedDTO implements Serializable {
    private String courseName;
    private String courseGroup;
    private String day;
    private String hour;

    public ScheduleAssignedDTO() {
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseGroup() {
        return courseGroup;
    }

    public void setCourseGroup(String courseGroup) {
        this.courseGroup = courseGroup;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
