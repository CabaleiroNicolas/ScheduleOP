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
import java.util.stream.Stream;

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        Constraint[] constraint = new Constraint[] {
                noOverlapConstraint(constraintFactory),
                minimizeDistanceBetweenCourses(constraintFactory)
                //avoidScheduleOverlap(constraintFactory),
                //minimizeTimeGaps(constraintFactory),
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
         return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                 .penalize(HardSoftScore.ONE_SOFT, (course1, course2) -> {
                     List<DayAndTimeOptaPlanner> combinedDayAndTimes = Stream.concat(
                                     course1.getAssignedSchedule().getDayAndTimes().stream(),
                                     course2.getAssignedSchedule().getDayAndTimes().stream()
                             )
                             .distinct() // Evitar duplicados
                             .sorted(Comparator.comparing(DayAndTimeOptaPlanner::getDay)
                                     .thenComparing(DayAndTimeOptaPlanner::getStartTime))
                             .toList();

                     long totalGapMinutes = 0;

                     for (int i = 0; i < combinedDayAndTimes.size() - 1; i++) {
                         DayAndTimeOptaPlanner current = combinedDayAndTimes.get(i);
                         DayAndTimeOptaPlanner next = combinedDayAndTimes.get(i + 1);

                         if (current.getDay().equals(next.getDay())) { // Comparar horarios del mismo día
                             long gapMinutes = Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();
                             if (gapMinutes > 0) { // Penalizar solo gaps positivos
                                 totalGapMinutes += gapMinutes;
                             }
                         }
                     }

                     return (int) totalGapMinutes;
                 })
                 .asConstraint("Minimizar espacios entre materias consecutivas en el mismo día");
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

    private Constraint noOverlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                .filter((course1, course2) -> course1.getAssignedSchedule()
                        .getDayAndTimes().stream().anyMatch(dayAndTime1 ->
                                course2.getAssignedSchedule()
                                        .getDayAndTimes().stream().anyMatch(dayAndTime1::overlaps)))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No overlap");
    }

    Constraint noOverlapConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                .filter((course1, course2) -> {
                    ScheduleOptaPlanner schedule1 = course1.getAssignedSchedule();
                    ScheduleOptaPlanner schedule2 = course2.getAssignedSchedule();

                    if (schedule1 == null || schedule2 == null) {
                        return false; // Ignorar cursos sin horarios asignados
                    }

                    // Verificar si se solapan los horarios de los cursos
                    for (DayAndTimeOptaPlanner dayTime1 : schedule1.getDayAndTimes()) {
                        for (DayAndTimeOptaPlanner dayTime2 : schedule2.getDayAndTimes()) {
                            if (dayTime1.getDay().equals(dayTime2.getDay())
                                    && dayTime1.getStartTime().isBefore(dayTime2.getEndTime())
                                    && dayTime1.getEndTime().isAfter(dayTime2.getStartTime())) {
                                return true; // Existe un solapamiento
                            }
                        }
                    }
                    return false;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No overlap");
    }




    private Constraint minimizeDistanceBetweenCourses(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                .filter((course1, course2) -> course1.getAssignedSchedule()
                        .getDayAndTimes().stream().anyMatch(dayAndTime1 ->
                                course2.getAssignedSchedule()
                                        .getDayAndTimes().stream().noneMatch(dayAndTime1::overlaps)))
                .penalize(
                        HardSoftScore.ONE_SOFT,
                        (course1, course2) ->
                                calculateDistance(course1, course2))
                .asConstraint("Minimize distance between courses");
    }

    private int calculateDistance(CourseOptaPlanner course1, CourseOptaPlanner course2) {
        return course1.getAssignedSchedule().getDayAndTimes().stream()
                .flatMap(dayAndTime1 -> course2.getAssignedSchedule()
                        .getDayAndTimes().stream()
                        .map(dayAndTime2 -> Math.abs(
                                (int) java.time.Duration.between(
                                        dayAndTime1.getEndTime(), dayAndTime2.getStartTime()).toMinutes())))
                .min(Integer::compareTo)
                .orElse(0);
    }




}
