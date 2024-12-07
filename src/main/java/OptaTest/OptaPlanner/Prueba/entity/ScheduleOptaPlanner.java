package OptaTest.OptaPlanner.Prueba.entity;

import lombok.*;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
public class ScheduleOptaPlanner {
    private String courseGroup;

    private ArrayList<DayAndTimeOptaPlanner> dayAndTimes;


    public ArrayList<DayAndTimeOptaPlanner> getDayAndTimes() {
        return dayAndTimes;
    }

    public void setDayAndTimes(ArrayList<DayAndTimeOptaPlanner> dayAndTimes) {
        this.dayAndTimes = dayAndTimes;
    }

    public String getCourseGroup() {
        return courseGroup;
    }

    public void setCourseGroup(String courseGroup) {
        this.courseGroup = courseGroup;
    }

    @Override
    public String toString() {
        return "ScheduleOptaPlanner{" +
                "courseGroup='" + courseGroup + '\'' +
                ", dayAndTimes=" + dayAndTimes +
                '}';
    }
}
