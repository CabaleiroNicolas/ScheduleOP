package OptaTest.OptaPlanner.Prueba.controller;

import OptaTest.OptaPlanner.Prueba.entity.CourseOptaPlanner;
import OptaTest.OptaPlanner.Prueba.entity.TimeTableConstraintProvider;
import OptaTest.OptaPlanner.Prueba.entity.TimeTableOptaPlanner;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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


}
