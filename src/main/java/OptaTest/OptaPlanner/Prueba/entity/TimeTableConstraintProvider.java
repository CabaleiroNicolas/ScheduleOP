package OptaTest.OptaPlanner.Prueba.entity;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.Duration;
import java.util.List;

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                avoidScheduleOverlap(constraintFactory)
               // minimizeTimeGaps(constraintFactory)
        };
    }

    // Restricción dura: evitar la superposición de horarios
    private Constraint avoidScheduleOverlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                .filter((course1, course2) -> {
                    ScheduleOptaPlanner schedule1 = course1.getAssignedSchedule();
                    ScheduleOptaPlanner schedule2 = course2.getAssignedSchedule();

                    if (schedule1 == null || schedule2 == null) {
                        return false;
                    }

                    // Verificar superposición de horarios
                    for (DayAndTimeOptaPlanner time1 : schedule1.getDayAndTimes()) {
                        for (DayAndTimeOptaPlanner time2 : schedule2.getDayAndTimes()) {
                            if (time1.getDay().equals(time2.getDay())
                                    && time1.getEndTime().isAfter(time2.getStartTime())
                                    && time1.getStartTime().isBefore(time2.getEndTime())) {
                                return true; // Hay superposición
                            }
                        }
                    }
                    return false;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Superposición de horarios");
    }

    // Restricción blanda: minimizar espacios entre horarios
    private Constraint minimizeTimeGaps(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CourseOptaPlanner.class)
                .join(CourseOptaPlanner.class,
                        Joiners.filtering((course1, course2) -> !course1.equals(course2)))
                .penalize(HardSoftScore.ONE_SOFT,
                        (course1, course2) -> {
                            ScheduleOptaPlanner schedule1 = course1.getAssignedSchedule();
                            ScheduleOptaPlanner schedule2 = course2.getAssignedSchedule();

                            if (schedule1 == null || schedule2 == null) {
                                return 0;
                            }

                            return calculateGapBetweenSchedules(schedule1, schedule2);
                        })
                .asConstraint("Minimizar huecos entre horarios");
    }

    private int calculateGapBetweenSchedules(ScheduleOptaPlanner schedule1, ScheduleOptaPlanner schedule2) {
        int minGap = Integer.MAX_VALUE;

        for (DayAndTimeOptaPlanner time1 : schedule1.getDayAndTimes()) {
            for (DayAndTimeOptaPlanner time2 : schedule2.getDayAndTimes()) {
                if (time1.getDay().equals(time2.getDay())) {
                    int gap = Math.abs((int) Duration.between(time1.getEndTime(), time2.getStartTime()).toMinutes());
                    minGap = Math.min(minGap, gap);
                }
            }
        }

        return minGap == Integer.MAX_VALUE ? 0 : minGap;
    }

}
