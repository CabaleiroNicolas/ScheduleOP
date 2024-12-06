package OptaTest.OptaPlanner.Prueba;

import OptaTest.OptaPlanner.Prueba.entity.CourseOptaPlanner;
import OptaTest.OptaPlanner.Prueba.entity.TimeTableOptaPlanner;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class TimeTableEasyCalculatorScore implements EasyScoreCalculator<TimeTableOptaPlanner, HardSoftScore> {
    @Override
    public HardSoftScore calculateScore(TimeTableOptaPlanner timeTableOptaPlanner) {
        int hardScore = (-100) * timeTableOptaPlanner.getCourses().size();
        for (CourseOptaPlanner courses : timeTableOptaPlanner.getCourses()){

        }
        return null;
    }
}
