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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class RunRCPSP {
    // propagator used
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
    // parameters of the problem
    private final int m_numberOfTasks;
    private final int m_numberOfResources;
    // attributes of solutions provided
    private final int[] m_solution;
    private final float m_elapsedTime;
    private final long m_backtracksNum;
    private final long m_visitedNodes;
    private final int m_makespan;
    // parameters input for the cofiguration of solver
    private final int checker;
    private final int search;
    private final int timeLimit;
    private final String fileName;
    private boolean status;
    private String checkerUsed;
    private String searchUsed;

    public RunRCPSP(String fileName, int checker, int search, int timeLimit) throws Exception {
        this.fileName = fileName;
        this.checker = checker;
        this.search = search;
        this.timeLimit = timeLimit;
        // new model creation
        Model model = new Model("RCPSP Solver");
        // read data from a file
        RCPSPInstance data = new RCPSPInstance(fileName);

        this.m_numberOfTasks = data.numberOfTasks;
        this.m_numberOfResources = data.numberOfResources;
        // variables of the problem
        IntVar[] startingTimes = new IntVar[m_numberOfTasks];
        IntVar[] processingTimes = new IntVar[m_numberOfTasks];
        IntVar[] endingTimes = new IntVar[m_numberOfTasks];

        for (int i = 0; i < m_numberOfTasks; i++) {
            startingTimes[i] = model.intVar("s[" + i + "]", 0, data.horizon(), true);
            endingTimes[i] = model.intVar("e[" + i + "]", data.processingTimes[i], data.horizon(), true);
            processingTimes[i] = model.intVar("p[" + i + "]", data.processingTimes[i]);
        }
        // the dummy task 0 starts at time 0
        model.arithm(startingTimes[0], "=", 0).post();
        // Makespan
        IntVar makespan = model.intVar("makespan", 0, data.horizon(), true);
        for (int i = 0; i < m_numberOfTasks; i++) {
            model.arithm(endingTimes[i], "<=", makespan).post();
        }
        model.arithm(makespan, "=", endingTimes[m_numberOfTasks-1]).post();
        // propagation of precedence constraints
        for(int i = 0; i< m_numberOfTasks; i++)
        {
            for(int j = i+1; j< m_numberOfTasks; j++)
            {
                if(data.precedences[i][j] == 1)
                {
                    model.arithm(startingTimes[i], "+", processingTimes[i], "<=", startingTimes[j]).post();
                }
                else if(data.precedences[i][j] == 0)
                {
                    model.arithm(startingTimes[j], "+", processingTimes[j], "<=", startingTimes[i]).post();
                }
            }
        }
        // new auxillary variable extended startingTime variable with makespan
        IntVar[] startingTimes_and_makespan = new IntVar[m_numberOfTasks+1];
        System.arraycopy(startingTimes, 0, startingTimes_and_makespan, 0, m_numberOfTasks);
        startingTimes_and_makespan[m_numberOfTasks] = makespan;

        // propagate resource constraint
        for(int i = 0; i< m_numberOfResources; i++) {
            IntVar[] heights = new IntVar[m_numberOfTasks];
            for (int j = 0; j < m_numberOfTasks; j++) {
                heights[j] = model.intVar("h[" + i + "][" + j + "]", data.heights[i][j]);
            }
            // only consider tasks with positive heigth
            ArrayList<Integer> indices = new ArrayList<>();
            for (int j = 0; j < data.heights[i].length; j++) {
                if (data.heights[i][j] > 0) {
                    indices.add(j);
                }
            }
            // filtering variable by considering only those with positive heigth
            if (indices.size() != 0) {
                IntVar[] filtered_startingTimes_makespan = new IntVar[indices.size() + 1];
                IntVar[] filtered_endingTimes = new IntVar[indices.size()];
                Integer[] filtered_heights = new Integer[indices.size()];
                Integer[] filtered_processingTimes = new Integer[indices.size()];
                Task[] filtered_tasks = new Task[indices.size()];
                IntVar[] filtered_heights_var = new IntVar[indices.size()];

                for (int j = 0; j < indices.size(); j++) {
                    int index = indices.get(j);
                    // auxillary variable extraction
                    filtered_startingTimes_makespan[j] = startingTimes[index];
                    filtered_endingTimes[j] = endingTimes[index];
                    filtered_heights[j] = data.heights[i][index];
                    filtered_processingTimes[j] = data.processingTimes[index];
                    // convert variable to format requiere by choco
                    filtered_tasks[j] = new Task(startingTimes[index], processingTimes[index], endingTimes[index]);
                    filtered_heights_var[j] = heights[index];
                }
                // add makespan to the current startTime variable
                filtered_startingTimes_makespan[indices.size()] = makespan;

                // switch to differents propagators
                switch(checker){
                    case BaptEnOC:
                        Constraint BaptEnOverloadCheck = new Constraint("Baptiste Energetic Reasoning overload checker algorithm",
                                new BaptisteEnergeticOverloadCheckConstraint(filtered_startingTimes_makespan,
                                filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(BaptEnOverloadCheck);
                        checkerUsed = "BaptEnOC";
                        break;
                    case DerrEnOC:
                        Constraint DerrEnOverloadCheck = new Constraint("Derrien Energetic Reasoning overload checker algorithm",
                            new DerrienEnergeticOverloadCheckConstraint(filtered_startingTimes_makespan,
                            filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(DerrEnOverloadCheck);
                        checkerUsed = "DerrEnOC";
                        break;
                    case OuelEnOC:
                        Constraint OuelEnOverloadCheck = new Constraint("Ouellet Energetic Reasoning overload checker algorithm",
                           new OuelletEnergeticOverloadCheckConstraint(filtered_startingTimes_makespan,
                           filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(OuelEnOverloadCheck);
                        checkerUsed = "OuelEnOC";
                        break;
                    case CarlEnOC:
                        Constraint CarlierERCheck = new Constraint("Carlier Energetic Reasoning overload checker algorithm",
                            new CarlierEnergeticOverloadCheckConstraint(filtered_startingTimes_makespan,
                            filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(CarlierERCheck);
                        checkerUsed = "CarlEnOC";
                        break;
                    case profileOC:
                        Constraint profileOverloadCheck = new Constraint("Horizontally Elastic Energetic Reasoning overload checker algorithm",
                            new ProfileCheckerConstraint(filtered_startingTimes_makespan,
                            filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(profileOverloadCheck);
                        checkerUsed = "KamEnOC";
                        break;
                    case profileCriteriaOC:
                        Constraint profileOverloadCheckCriteria = new Constraint("Horizontally Elastic Energetic Reasoning overload checker algorithm with Criteria",
                                new ProfileCheckerWithCriteriaConstraint(filtered_startingTimes_makespan,
                                filtered_endingTimes,filtered_heights,filtered_processingTimes,data.capacities[i]));
                        model.post(profileOverloadCheckCriteria);
                        checkerUsed = "ImprKamEnOC";
                        break;
                    default:
                        model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.SWEEP).post();
                        break;
                }
                model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.SWEEP).post();
            }
        }
        model.setObjective(false, makespan);
        Solver solver = model.getSolver();

        // switch to different search
        switch(search) {
            case staticSearch:
                solver.setSearch(Search.inputOrderLBSearch(startingTimes_and_makespan));
                searchUsed = "Static";
                break;
            case dynamicSearchCOSDOWS:
                solver.setSearch(Search.conflictOrderingSearch(Search.domOverWDegSearch(startingTimes_and_makespan)));
                searchUsed = "DomOverWDeg";
                break;
            case dynamicSearchCOSSmallest:
                solver.setSearch(Search.conflictOrderingSearch(Search.intVarSearch( new SmallestVarOrder(model), new IntDomainMin(), startingTimes_and_makespan)));
                searchUsed = "Smallest";
                break;
            case dynamicSearchCOSFF:
                solver.setSearch(Search.conflictOrderingSearch(Search.minDomLBSearch(startingTimes_and_makespan)));
                searchUsed = "MinDomLB";
                break;
            default:
                System.out.println("no heuritic search");
        }
        solver.setRestartOnSolutions();
        solver.limitTime(timeLimit * 1000);
        // solution of the problem
        Solution best = solver.findOptimalSolution(makespan, false);
        m_solution = new int[m_numberOfTasks];
        if (solver.isObjectiveOptimal()) {
            m_makespan = best.getIntVal(makespan);
            for (int i = 0; i < m_numberOfTasks; i++){
                m_solution[i] = best.getIntVal(startingTimes[i]);
            }
            m_elapsedTime =  solver.getTimeCount();
            m_backtracksNum = solver.getBackTrackCount();
            m_visitedNodes = solver.getNodeCount(); //Retourne le nombre de noeuds visit�s dans l'arbre.
            status = true;
        } else {
            status = false;
            if (makespan.isInstantiated()) {
                m_makespan = makespan.getLB();
                m_elapsedTime =  solver.getTimeCount();
                m_backtracksNum = solver.getBackTrackCount();
                m_visitedNodes = solver.getNodeCount();
            } else {
                m_makespan = -1;
                m_elapsedTime =  solver.getTimeCount();
                m_backtracksNum = solver.getBackTrackCount();
                m_visitedNodes = solver.getNodeCount();
            }
        }

        for (int i = 0; i < m_numberOfResources; i++) {
            //On filtre les variables qui on un height null
            ArrayList<Integer> indices = new ArrayList<>();
            for(int j=0; j<data.heights[i].length; j++) {
                if(data.heights[i][j] > 0) {
                    indices.add(j);
                }
            }
        }
        System.out.println(fileName +  " | " +  m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + status + " | " + checkerUsed  + " | " + searchUsed +  " | "  + Arrays.toString(m_solution));
    }
    public float howMuchTime() {
        return m_elapsedTime;
    }
    public long howManyBacktracks() {
        return m_backtracksNum;
    }
    public long howManyVisitedNodes() {
        return m_visitedNodes;
    }
    public int makeSpanSolution() {
        return m_makespan;
    }
    public void printResults() {
        System.out.print(m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum +  "\t \t");
    }
    public String getResults() {
        return m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + "\t \t";
    }
    public void printSolution() {
        for (int i = 0; i < m_numberOfTasks; i++){
            System.out.print("s["+ i + "] = "+ m_solution[i] + " , ");
        }
        System.out.println();
    }
    public String parameter() {
        return checker +  " | " + search +  " | " + timeLimit ;
    }
    public void printAllResults() {
        System.out.println(fileName +  " | " +  m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum  + " | " + Arrays.toString(m_solution) + " | " +  parameter() + "\t \t");
    }
    public String AllResults() {
        return fileName +  " | " +  m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + m_visitedNodes + " | " + Arrays.toString(m_solution) + " | " +  parameter();
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 4) {
            throw new IllegalStateException("Please enter correct parameters.");
        }
        final String filename = args[0];
        final int checker = Integer.parseInt(args[1]);
        final int search = Integer.parseInt(args[2]);
        final int timelimite = Integer.parseInt(args[3]);
        RunRCPSP sample = new RunRCPSP(filename, checker, search, timelimite);
    }
}
