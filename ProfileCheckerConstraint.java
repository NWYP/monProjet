import cumulative.Task;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Choco-solver propagator that enforces the cumulative constraint using the
 * {@link ProfileChecker} energetic overload check.
 *
 * <p>At each propagation call the propagator:</p>
 * <ol>
 *   <li>Reads the current domains of all starting-time variables to build a
 *       set of {@link Task} objects whose EST/LCT are set to the current lower
 *       and upper bounds.</li>
 *   <li>Builds a "mirrored" task set (time axis reversed) to symmetrically
 *       handle both the left-to-right and right-to-left overload directions.</li>
 *   <li>Runs {@link ProfileChecker#overloadCheck()} on both task sets and
 *       throws a {@link ContradictionException} if either check fails.</li>
 * </ol>
 *
 * <p>This propagator is meant to be plugged into a Choco model as a custom
 * constraint. The variable array passed to the constructor must contain all
 * starting-time variables followed by the makespan variable as the last
 * element.</p>
 *
 * <p><b>Constructor signature:</b></p>
 * <pre>{@code
 * IntVar[] startAndMakespan = ArrayUtils.append(startingTimes, new IntVar[]{makespan});
 * new ProfileCheckerConstraint(startAndMakespan, endingTimes, heights, durations, capacity);
 * }</pre>
 *
 * @see ProfileChecker
 * @see ProfileCheckerWithCriteriaConstraint
 */
public class ProfileCheckerConstraint extends Propagator<IntVar> {

    /** Starting-time variables (one per task). */
    private final IntVar[] startingTimes;

    /** Ending-time variables (one per task, used only by {@link #isEntailed()}). */
    private final IntVar[] endingTimes;

    /** Makespan variable (last element of the variable array passed to the super constructor). */
    private final IntVar makespan;

    /** Fixed processing durations of the tasks. */
    private final Integer[] processingTimes;

    /** Fixed resource demands (heights) of the tasks. */
    private final Integer[] heights;

    /** Maximum resource capacity of the cumulative resource. */
    private final int capacity;

    /** Number of tasks. */
    private final int nbTasks;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new {@code ProfileCheckerConstraint} propagator.
     *
     * <p>The {@code startingTimes_makespan} array must contain all starting-time
     * variables first, with the makespan variable appended as the very last
     * element.  The length of that array is therefore {@code n + 1} where
     * {@code n} is the number of tasks.</p>
     *
     * @param startingTimes_makespan array of {@code n} starting-time variables
     *                               followed by the makespan variable
     * @param endingTimes            array of {@code n} ending-time variables
     * @param heights                resource demands of the {@code n} tasks
     * @param processingTimes        fixed durations of the {@code n} tasks
     * @param capacity               maximum resource capacity
     */
    public ProfileCheckerConstraint(
            IntVar[] startingTimes_makespan,
            IntVar[] endingTimes,
            Integer[] heights,
            Integer[] processingTimes,
            int capacity) {

        super(startingTimes_makespan);

        this.nbTasks          = startingTimes_makespan.length - 1;
        this.processingTimes  = processingTimes;
        this.heights          = heights;
        this.capacity         = capacity;
        this.endingTimes      = endingTimes;

        this.startingTimes = new IntVar[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            startingTimes[i] = vars[i];
        }
        this.makespan = vars[nbTasks];
    }

    // -----------------------------------------------------------------------
    // Propagator contract
    // -----------------------------------------------------------------------

    /**
     * Propagates the cumulative constraint by running the profile-based energetic
     * overload check (in both the forward and mirrored directions).
     *
     * <p>Tasks are built from the current lower/upper bounds of the starting-time
     * variables. If either the forward or the mirrored overload check fails, a
     * {@link ContradictionException} is thrown, which triggers back-tracking in
     * the Choco search engine.</p>
     *
     * @param evtmask a bit-mask encoding which events triggered this propagation
     *                (provided by the Choco framework; ignored here)
     * @throws ContradictionException if an overload is detected in either direction
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        Task[] tasks         = new Task[nbTasks];
        Task[] mirroredTasks = new Task[nbTasks];

        int maxLct = Integer.MIN_VALUE;
        for (int i = 0; i < nbTasks; i++) {
            final int est = startingTimes[i].getLB();
            final int lct = startingTimes[i].getUB() + processingTimes[i];
            tasks[i] = new Task(i, est, lct, processingTimes[i], heights[i]);
            maxLct   = Math.max(maxLct, lct);
        }

        // Build the mirrored task set (reversed time axis)
        for (int i = 0; i < nbTasks; i++) {
            final int est = startingTimes[i].getLB();
            final int lct = startingTimes[i].getUB() + processingTimes[i];
            mirroredTasks[i] = new Task(i,
                    -lct + maxLct,
                    -est + maxLct,
                    processingTimes[i],
                    heights[i]);
        }

        ProfileChecker forward  = new ProfileChecker(tasks,         capacity);
        ProfileChecker backward = new ProfileChecker(mirroredTasks, capacity);

        if (!forward.overloadCheck() || !backward.overloadCheck()) {
            throw new ContradictionException();
        }
    }

    /**
     * Determines whether the constraint is trivially entailed (satisfied),
     * falsified, or still undetermined given the current variable domains.
     *
     * <p>The method performs three checks in order:</p>
     * <ol>
     *   <li>Each task's time window is consistent: start + duration ∈ [end.LB, end.UB].</li>
     *   <li>The mandatory (compulsory) resource consumption does not exceed
     *       {@link #capacity} at any time unit.</li>
     *   <li>All starting-time variables are instantiated.</li>
     * </ol>
     *
     * @return {@link ESat#FALSE} if a violation is detected,
     *         {@link ESat#TRUE} if the constraint is certainly satisfied,
     *         {@link ESat#UNDEFINED} otherwise
     */
    @Override
    public ESat isEntailed() {
        int minStart = startingTimes[0].getUB();
        int maxEnd   = endingTimes[0].getLB();

        // 1. Check time-window consistency for each task
        for (int i = 0; i < nbTasks; i++) {
            minStart = Math.min(minStart, startingTimes[i].getUB());
            maxEnd   = Math.max(maxEnd,   endingTimes[i].getLB());

            boolean startTooLate  = startingTimes[i].getLB() + processingTimes[i] > endingTimes[i].getUB();
            boolean startTooEarly = startingTimes[i].getUB() + processingTimes[i] < endingTimes[i].getLB();
            if (startTooLate || startTooEarly) {
                return ESat.FALSE;
            }
        }

        // 2. Check mandatory resource consumption
        int maxLoad = 0;
        if (minStart <= maxEnd) {
            int[] mandatoryLoad = new int[maxEnd - minStart];
            for (int i = 0; i < nbTasks; i++) {
                for (int t = startingTimes[i].getUB(); t < endingTimes[i].getLB(); t++) {
                    mandatoryLoad[t - minStart] += heights[i];
                    if (mandatoryLoad[t - minStart] > capacity) {
                        return ESat.FALSE;
                    }
                    maxLoad = Math.max(maxLoad, mandatoryLoad[t - minStart]);
                }
            }
        }

        // 3. Check whether all starting-time variables are instantiated
        for (int i = 0; i < vars.length - 1; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }

        assert minStart <= maxEnd;
        return maxLoad <= capacity ? ESat.TRUE : ESat.UNDEFINED;
    }
}
