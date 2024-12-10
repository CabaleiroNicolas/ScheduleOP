package OptaTest.OptaPlanner.Prueba.entity;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.time.Duration;
import java.time.LocalTime;

public class TimeTableScoreCalculator implements EasyScoreCalculator<TimeTableOptaPlanner, HardSoftScore> {
    @Override
    public HardSoftScore calculateScore(TimeTableOptaPlanner timeTable) {
        int hardScore = 0;
        int softScore = 0;

        // Restricción: Evitar solapamiento
        hardScore += calculateNoOverlapPenalty(timeTable);

        // Restricción: Minimizar distancia entre cursos
        softScore += calculateMinimizeDistancePenalty(timeTable);

        // Restricción: Priorizar rango horario específico
//        softScore += calculatePreferredTimeRangePenalty(timeTable);

//        // Restricción: Priorizar profesor/comisión específica
//        softScore += calculatePreferredProfessorPenalty(timeTable);
//
//        // Restricción: Priorizar día libre
//        softScore += calculateDayOffPenalty(timeTable);

        return HardSoftScore.of(hardScore, softScore);
    }

    private int calculateNoOverlapPenalty(TimeTableOptaPlanner timeTable) {
        int penalty = 0;
        for (CourseOptaPlanner course1 : timeTable.getCourses()) {
            for (CourseOptaPlanner course2 : timeTable.getCourses()) {
                if (!course1.equals(course2) && overlaps(course1, course2)) {
                    penalty -= 10; // Penalización por cada solapamiento
                }
            }
        }
        return penalty;
    }

    private boolean overlaps(CourseOptaPlanner course1, CourseOptaPlanner course2) {
        for (DayAndTimeOptaPlanner time1 : course1.getAssignedSchedule().getDayAndTimes()) {
            for (DayAndTimeOptaPlanner time2 : course2.getAssignedSchedule().getDayAndTimes()) {
                if (time1.getDay().equals(time2.getDay()) &&
                        time1.getEndTime().isAfter(time2.getStartTime()) &&
                        time2.getEndTime().isAfter(time1.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int calculateMinimizeDistancePenalty(TimeTableOptaPlanner timeTable) {
        int reward = 0;
        for (CourseOptaPlanner course1 : timeTable.getCourses()) {
            for (CourseOptaPlanner course2 : timeTable.getCourses()) {
                if (!course1.equals(course2)) {
                    reward += calculateDistanceReward(course1, course2);
                }
            }
        }
        return reward;
    }

    private int calculateDistanceReward(CourseOptaPlanner course1, CourseOptaPlanner course2) {
        ScheduleOptaPlanner schedule1 = course1.getAssignedSchedule();
        System.out.println("Schedule 1 " +schedule1);
        ScheduleOptaPlanner schedule2 = course2.getAssignedSchedule();
        System.out.println("Schedule 2 "+ schedule2);
        for (DayAndTimeOptaPlanner time1 : schedule1.getDayAndTimes()) {
            for (DayAndTimeOptaPlanner time2 : schedule2.getDayAndTimes()) {
                if (time1.getDay().equals(time2.getDay())) {
                    long gap = Math.abs(Duration.between(time1.getEndTime(), time2.getStartTime()).toMinutes());
                    return -Math.toIntExact(gap / 10); // Penalizar espacios grandes
                }
            }
        }
        return 0;
    }

//    private int calculatePreferredTimeRangePenalty(TimeTableOptaPlanner timeTable) {
//        int reward = 0;
//        for (CourseOptaPlanner course : timeTable.getCourses()) {
//            for (DayAndTimeOptaPlanner dayAndTime : course.getAssignedSchedule().getDayAndTimes()) {
//                if (isWithinPreferredTimeRange(
//                        timeTable.getPreferredStartTime(),
//                        timeTable.getPreferredEndTime(),
//                        dayAndTime.getStartTime(),
//                        dayAndTime.getEndTime())) {
//                    reward += 5; // Recompensa por estar en el rango
//                } else {
//                    reward -= 5; // Penalización por estar fuera del rango
//                }
//            }
//        }
//        return reward;
//    }

    private boolean isWithinPreferredTimeRange(LocalTime preferredStart, LocalTime preferredEnd, LocalTime startTime, LocalTime endTime) {
        return !startTime.isBefore(preferredStart) && !endTime.isAfter(preferredEnd);
    }

//    private int calculatePreferredProfessorPenalty(TimeTableOptaPlanner timeTable) {
//        int reward = 0;
//        for (CourseOptaPlanner course : timeTable.getCourses()) {
//            if (course.getPreferredProfessor() != null &&
//                    course.getAssignedSchedule() != null &&
//                    course.getAssignedSchedule().getProfessor().equals(course.getPreferredProfessor())) {
//                reward += 10; // Recompensa por asignar al profesor/comisión preferida
//            }
//        }
//        return reward;
//    }
//
//    private int calculateDayOffPenalty(TimeTableOptaPlanner timeTable) {
//        int reward = 0;
//        for (CourseOptaPlanner course : timeTable.getCourses()) {
//            for (DayAndTimeOptaPlanner dayAndTime : course.getAssignedSchedule().getDayAndTimes()) {
//                if (!dayAndTime.getDay().equals(timeTable.getPreferredDayOff())) {
//                    reward += 5; // Recompensa por respetar el día libre
//                } else {
//                    reward -= 10; // Penalización por asignar en el día libre
//                }
//            }
//        }
//        return reward;
//    }

}
