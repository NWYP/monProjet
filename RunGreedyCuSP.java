import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class RunGreedyCuSP {
    // checker
    private static final int BaptEnOC = 0;
    private static final int DerrEnOC = 1;
    private static final int OuelEnOC = 2;
    private static final int CarlEnOC = 3;
    private static final int profileOC = 4;
    private static final int profileCriteriaOC = 5;
    // search strategy used
    private static final int staticSearch = 0;
    private static final int dynamicSearchCOSDOWS = 1;
    private static final int dynamicSearchCOSSmallest = 2;
    private static final int dynamicSearchCOSFF = 3;

    String filename;
    int checker;
    int timelimit;
    int nbtasks;
    int C;
    Integer[] p;
    Integer[] h;
    int hor;
    private boolean status;
    private String checkerUsed;
    private String searchUsed;

    private final int[] solution;
    private final float elapsedTime;
    private final long backtracks;
    private final long nodes;
    private int first_makespan;
    private int idleArea;

    public RunGreedyCuSP(String filename, int checker, int timelimit) throws Exception {
        this.filename = filename;
        this.checker = checker;
        this.timelimit = timelimit;

        Model model = new Model("CuSP model");

        CuSPInstance1 data = new CuSPInstance1(filename);
        nbtasks = data.nbTasks;
        C = data.Capacity;
        p = data.duration;
        h = data.demand;
        hor = data.horizon;
        // variables of the problem
        IntVar[] starts = new IntVar[nbtasks];
        IntVar[] starts_makespan = new IntVar[nbtasks+1];
        IntVar[] durations = new IntVar[nbtasks];
        IntVar[] ends = new IntVar[nbtasks];
        IntVar[] heights = new IntVar[nbtasks];
        Task[] tasks = new Task[nbtasks];
        int usedArea = 0;

        for (int i = 0; i < nbtasks; i++) {
            starts[i] = model.intVar("s[" + i + "]", 0, hor-p[i], true);
            ends[i] = model.intVar("e[" + i + "]", p[i], hor, true);
            durations[i] = model.intVar("p[" + i + "]", p[i]);
            heights[i] = model.intVar("h[" + i + "]", h[i]);
            starts_makespan[i] = starts[i];
            tasks[i] = new Task(starts[i], durations[i], ends[i]);
            usedArea += p[i] * h[i];
        }
        // Makespan
        IntVar makespan = model.intVar("makespan", 0, hor, true);
        starts_makespan[nbtasks] = makespan;
        for (int i = 0; i < nbtasks; i++) {
            model.arithm(makespan, ">=", ends[i]).post();
        }


        switch(checker){
            case BaptEnOC:
                Constraint BaptEnOverloadCheck = new Constraint("Baptiste Energetic Reasoning overload checker algorithm",
                         new BaptisteEnergeticOverloadCheckConstraint(starts_makespan, ends, h, p, C));
                model.post(BaptEnOverloadCheck);
                checkerUsed = "BaptEnOC";
                break;
            case DerrEnOC:
                Constraint DerrEnOverloadCheck = new Constraint("Derrien Energetic Reasoning overload checker algorithm",
                         new DerrienEnergeticOverloadCheckConstraint(starts_makespan, ends, h, p, C));
                model.post(DerrEnOverloadCheck);
                searchUsed = "DerrEnOC";
                break;
            case OuelEnOC:
                Constraint OuelEnOverloadCheck = new Constraint("Ouellet Energetic Reasoning overload checker algorithm",
                        new OuelletEnergeticOverloadCheckConstraint(starts_makespan, ends, h, p, C));
                model.post(OuelEnOverloadCheck);
                searchUsed = "OuelEnOC";
                break;
            case CarlEnOC:
                Constraint CarlierERCheck = new Constraint("Carlier Energetic Reasoning overload checker algorithm",
                        new CarlierEnergeticOverloadCheckConstraint(starts_makespan, ends, h, p, C));
                model.post(CarlierERCheck);
                searchUsed = "CarlEnOC";
                break;
            case profileOC:
                Constraint HEEnOverloadCheckx = new Constraint("Horizontally Elastic Energetic Reasoning overload checker algorithm with two criteria",
                        new ProfileCheckerConstraint(starts_makespan, ends, h, p, C));
                model.post(HEEnOverloadCheckx);
                searchUsed = "KamEnOC";
                break;
            case profileCriteriaOC:
                Constraint HEEnCriteriaOverloadCheckx = new Constraint("Horizontally Elastic Energetic Reasoning overload checker algorithm with two criteria",
                        new ProfileCheckerWithCriteriaConstraint(starts_makespan, ends, h, p, C));
                model.post(HEEnCriteriaOverloadCheckx);
                searchUsed = "ImprKamEnOC";
                break;
            default:
                model.cumulative(tasks, heights, model.intVar("capacity", C), false, Cumulative.Filter.SWEEP).post();
                break;
        }
        model.cumulative(tasks, heights, model.intVar("capacity", C), false, Cumulative.Filter.SWEEP).post();



        Solver solver = model.getSolver();
        solver.limitTime(timelimit * 1000);

        solver.setSearch(Search.inputOrderLBSearch(starts_makespan));
        Solution sol = solver.findSolution();
        solution = new int[nbtasks];
        Arrays.fill(solution, 0);
        searchUsed = "Static";

        if (solver.solve()) {
            elapsedTime = solver.getTimeCount();
            backtracks = solver.getFailCount();
            nodes = solver.getNodeCount();
            first_makespan = sol.getIntVal(makespan);
            status = true;
            for (int i = 0; i < nbtasks; i++) {
                solution[i] = sol.getIntVal(starts[i]);
            }
            idleArea = first_makespan * C - usedArea;
        } else {
            status = false;
            first_makespan = -1;
            elapsedTime = solver.getTimeCount();
            backtracks = solver.getFailCount();
            nodes = solver.getNodeCount();
        }
        System.out.println(filename + " | " + first_makespan + " | " + elapsedTime + " | " + backtracks + " | " + idleArea + " | " + (first_makespan * C) + " | " + status + " | " + checkerUsed  + " | " + searchUsed);//  Arrays.toString(solution));
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 3) {
            throw new IllegalStateException("Please enter correct parameters.");
        }
        final String filename = args[0];
        final int checker = Integer.parseInt(args[1]);
        final int timelimite = Integer.parseInt(args[2]);
        RunGreedyCuSP sample = new RunGreedyCuSP(filename, checker, timelimite);
    }
}
