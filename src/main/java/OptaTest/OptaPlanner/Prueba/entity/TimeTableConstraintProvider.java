package OptaTest.OptaPlanner.Prueba.entity;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;



public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {

       Constraint[] constraint = new Constraint[]{
               noOverlapConstraint(constraintFactory),
               minimizeDistanceBetweenCourses(constraintFactory),
               groupSchedulesOnSameDay(constraintFactory)
       };
        return constraint;
    }

    Constraint noOverlapConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CourseOptaPlanner.class)
                .filter((course1, course2) -> {
                    ScheduleOptaPlanner schedule1 = course1.getAssignedSchedule();
                    System.out.println("Schedule 1 " +schedule1);
                    ScheduleOptaPlanner schedule2 = course2.getAssignedSchedule();
                    System.out.println("Schedule 2 "+ schedule2);
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

    private Constraint groupSchedulesOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CourseOptaPlanner.class)
                .join(CourseOptaPlanner.class,
                        Joiners.filtering((course1, course2) -> {
                            if (course1.getAssignedSchedule() == null || course2.getAssignedSchedule() == null) {
                                return false;
                            }
                            return course1.getAssignedSchedule().getDayAndTimes().stream()
                                    .anyMatch(dayTime1 -> course2.getAssignedSchedule().getDayAndTimes().stream()
                                            .anyMatch(dayTime2 -> dayTime1.getDay().equals(dayTime2.getDay())));
                        }))
                .filter((course1, course2) -> !course1.equals(course2))
                .reward(
                        HardSoftScore.ofSoft(100),
                        (course1, course2) -> 1 // Recompensa por agrupar cursos el mismo día
                )
                .asConstraint("Group schedules on the same day");
    }
}
