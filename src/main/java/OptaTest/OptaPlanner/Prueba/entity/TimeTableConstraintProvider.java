package OptaTest.OptaPlanner.Prueba.entity;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        Constraint[] constraint = new Constraint[] {
                avoidScheduleOverlap(constraintFactory),
                minimizeTimeGaps(constraintFactory),
                //compactSchedules(constraintFactory)
        };
        return constraint;
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


     //Restricción blanda: minimizar espacios entre horarios
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


//    private Constraint minimizeScheduleValue(ConstraintFactory constraintFactory) {
//        return constraintFactory.forEach(CourseOptaPlanner.class)
//                .penalize(HardSoftScore.ONE_SOFT,
//                        course -> {
//                            ScheduleOptaPlanner schedule = course.getAssignedSchedule();
//                            if (schedule == null) {
//                                return 0; // Sin horario asignado, no penalizamos
//                            }
//
//                            // Calcula la suma de las horas de inicio y fin de cada horario
//                            int totalValue = schedule.getDayAndTimes().stream()
//                                    .mapToInt(dayAndTime -> convertTimeToInt(dayAndTime.getStartTime()) +
//                                            convertTimeToInt(dayAndTime.getEndTime()))
//                                    .sum();
//                            return Math.abs(totalValue);
//                        })
//                .asConstraint("Minimizar valor total de horarios");
//    }
//    // Metodo para convertir horas (HH:mm:ss) a un entero (HHmm)
//    private int convertTimeToInt(java.time.LocalTime time) {
//        return time.getHour() * 100 + time.getMinute();
//    }



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
