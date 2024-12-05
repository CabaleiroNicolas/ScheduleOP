package OptaTest.OptaPlanner.Prueba.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PlanningSolution
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeTableOptaPlanner {

    @PlanningEntityCollectionProperty
    private ArrayList<CourseOptaPlanner> courses;


    @PlanningScore
    private HardSoftScore score;

    public ArrayList<CourseOptaPlanner> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<CourseOptaPlanner> courses) {
        this.courses = courses;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
