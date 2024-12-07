package OptaTest.OptaPlanner.Prueba.controller;
import OptaTest.OptaPlanner.Prueba.dto.ScheduleAssignedDTO;
import OptaTest.OptaPlanner.Prueba.entity.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@RestController
@RequestMapping("/timetable")
public class TimeTableController {


    SolverConfig solverConfig = new SolverConfig()
            .withSolutionClass(TimeTableOptaPlanner.class) // Clase solución
            .withEntityClasses(CourseOptaPlanner.class) // Clases de entidades planificables
            .withConstraintProviderClass(TimeTableConstraintProvider.class)
            .withTerminationSpentLimit(Duration.ofSeconds(5));
    @PostMapping("/solve")
    public TimeTableOptaPlanner solve(@RequestBody TimeTableOptaPlanner problem){

        System.out.println(problem.getCourses());

        SolverFactory<TimeTableOptaPlanner> solverFactory = SolverFactory.create(solverConfig);
        Solver<TimeTableOptaPlanner> solver = solverFactory.buildSolver();

        TimeTableOptaPlanner solvedTimeTable = solver.solve(problem);

        return solvedTimeTable;

    }

    @PostMapping("/solver")
    public ResponseEntity<List<ScheduleAssignedDTO>> solver(@RequestBody TimeTableOptaPlanner problem) {

        SolverFactory<TimeTableOptaPlanner> solverFactory = SolverFactory.create(solverConfig);
        Solver<TimeTableOptaPlanner> solver = solverFactory.buildSolver();



        //System.out.println(problem.getCourses().toString());
        problem.sortSchedules();
        //System.out.println(problem.getCourses().toString());
        TimeTableOptaPlanner solvedTimeTable = solver.solve(problem);
        System.out.println(solvedTimeTable.getCourses().toString());


        // Mapear la solución a DTOs
        List<ScheduleAssignedDTO> assignedCoursesList = new ArrayList<>();
        mapperTimeTableToAssignedCoursesList(solvedTimeTable, assignedCoursesList);


        System.out.println(solvedTimeTable.getScore().toString());

        return ResponseEntity.ok(assignedCoursesList);
    }



    //Esto no deberia ir aqui, pero por fines de prueba lo dejo
    private static void  mapperTimeTableToAssignedCoursesList(TimeTableOptaPlanner solvedTimeTable, List<ScheduleAssignedDTO> assignedCoursesList) {
        solvedTimeTable.getCourses().forEach(
                c -> {
                    ScheduleAssignedDTO scheduleAssignedDTO = new ScheduleAssignedDTO();
                    scheduleAssignedDTO.setCourseName(c.getCourseName());
                    scheduleAssignedDTO.setCourseGroup(c.getAssignedSchedule().getCourseGroup());
                    String days = c.getAssignedSchedule().getDayAndTimes().stream()
                            .map(dayAndTime -> dayAndTime.getDay().toString())
                            .collect(Collectors.joining(", "));
                    scheduleAssignedDTO.setDay(days);
                    String hours = c.getAssignedSchedule().getDayAndTimes().stream()
                                    .map(dayAndTime -> dayAndTime.getStartTime().toString().concat("-").concat(dayAndTime.getEndTime().toString()))
                                            .collect(Collectors.joining(", "));
                    scheduleAssignedDTO.setHour(hours);
                    assignedCoursesList.add(scheduleAssignedDTO);
                }
        );
    }




}
