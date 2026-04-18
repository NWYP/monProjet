import cumulative.Task;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Profile-based energetic overload checker with a filtering criterion for the
 * Cumulative Scheduling Problem (CuSP).
 *
 * <p>This class extends the ideas of {@link ProfileChecker} by first performing a
 * global overload check on the full scheduling horizon and collecting the set of
 * "critical" time bounds ({@code delta}) where overflow was detected.  Only those
 * bounds are then re-examined with the more expensive decomposed profile check.</p>
 *
 * <p>The additional filtering criterion is based on the height of the decomposed
 * profile at bound {@code b}: the decomposed check is executed only when the total
 * resource demand of tasks whose decomposed LCT equals {@code b} already exceeds
 * the resource capacity {@code C} — the so-called <em>peak height criterion</em>.</p>
 *
 * <p>This selective approach can significantly reduce the number of decomposed checks
 * while maintaining the same pruning power as the plain {@link ProfileChecker}.</p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * Task[] tasks = { new Task(0, 0, 10, 3, 2), new Task(1, 2, 8, 4, 3) };
 * ProfileCheckerWithCriteria checker = new ProfileCheckerWithCriteria(tasks, 5);
 * boolean feasible = checker.overloadCheck();
 * }</pre>
 *
 * @see ProfileChecker
 * @see ProfileCheckerWithCriteriaConstraint
 */
public class ProfileCheckerWithCriteria {

    /** Resource capacity (maximum height of the profile at any point in time). */
    protected final int capacity;

    /** Number of tasks. */
    protected final int n;

    /** Original task array. */
    protected final Task[] tasks;

    /** Internal resource-consumption profile (linked list of timepoints). */
    private Profile profile;

    /** Makespan upper bound (unused externally; kept for future extension). */
    protected int makespan;

    /**
     * Candidate bounds where overflow was detected during the global profile pass.
     * Only bounds in this set are further examined with the decomposed check.
     */
    protected ArrayList<Integer> delta;

    // -----------------------------------------------------------------------
    // Pre-sorted task views (sorted once in the constructor for efficiency)
    // -----------------------------------------------------------------------

    /** Tasks sorted by earliest start time (EST). */
    private final Task[] tasksByEst;

    /** Tasks sorted by earliest completion time (ECT). */
    private final Task[] tasksByEct;

    /** Tasks sorted by latest start time (LST). */
    private final Task[] tasksByLst;

    /** Tasks sorted by latest completion time (LCT). */
    private final Task[] tasksByLct;

    /**
     * Tasks sorted by the sum {@code EST + LCT} (used for the "middle" decomposition
     * category of partially-overlapping tasks).
     */
    private final Task[] tasksByMiddle;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new {@code ProfileCheckerWithCriteria} for the given set of tasks
     * and resource capacity.
     *
     * <p>The constructor pre-sorts copies of the task array according to the four
     * standard time-window bounds and the composite {@code EST + LCT} key. Sorting
     * is performed in {@code O(n log n)} and only once.</p>
     *
     * @param tasks    the array of tasks to schedule; must not be {@code null} or empty
     * @param capacity the maximum resource capacity; must be strictly positive
     * @throws NullPointerException     if {@code tasks} is {@code null}
     * @throws IllegalArgumentException if {@code tasks} is empty or {@code capacity <= 0}
     */
    public ProfileCheckerWithCriteria(Task[] tasks, int capacity) {
        if (tasks == null) throw new NullPointerException("tasks must not be null");
        if (tasks.length == 0) throw new IllegalArgumentException("tasks must not be empty");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be strictly positive");

        this.capacity = capacity;
        this.tasks    = tasks;
        this.profile  = new Profile();
        this.n        = tasks.length;
        this.makespan = Integer.MIN_VALUE;
        this.delta    = new ArrayList<>();

        this.tasksByEst    = Arrays.copyOf(tasks, n);
        this.tasksByEct    = Arrays.copyOf(tasks, n);
        this.tasksByLst    = Arrays.copyOf(tasks, n);
        this.tasksByLct    = Arrays.copyOf(tasks, n);
        this.tasksByMiddle = Arrays.copyOf(tasks, n);

        Arrays.sort(tasksByEst,    (t1, t2) -> Integer.compare(t1.getEst(), t2.getEst()));
        Arrays.sort(tasksByEct,    (t1, t2) -> Integer.compare(t1.getEct(), t2.getEct()));
        Arrays.sort(tasksByLst,    (t1, t2) -> Integer.compare(t1.getLst(), t2.getLst()));
        Arrays.sort(tasksByLct,    (t1, t2) -> Integer.compare(t1.getLct(), t2.getLct()));
        Arrays.sort(tasksByMiddle, (t1, t2) -> Integer.compare(
                t1.getEst() + t1.getLct(), t2.getEst() + t2.getLct()));
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Runs the criteria-filtered profile-based energetic overload check.
     *
     * <p>The algorithm proceeds in two phases:</p>
     * <ol>
     *   <li><b>Global pass:</b> Build the full resource-consumption profile and
     *       simulate scheduling. If a global overload is detected, return
     *       {@code false} immediately. Collect all "overflow" bounds into
     *       {@link #delta}.</li>
     *   <li><b>Selective decomposed pass:</b> For each bound {@code b} in
     *       {@link #delta}, apply the peak-height criterion. If the criterion is
     *       satisfied (peak height &gt; {@code C}), run the decomposed overload
     *       check for {@code b}. Return {@code false} if an overload is
     *       detected.</li>
     * </ol>
     *
     * @return {@code true} if no overload is detected (possibly feasible),
     *         {@code false} if an overload is detected (certainly infeasible)
     */
    public boolean overloadCheck() {
        int maxLct = buildGlobalTimeline();
        int span   = scheduleTasks(maxLct, true);
        if (span > maxLct) {
            return false;
        }
        for (int b : delta) {
            if (exceedsPeakHeight(b)) {
                int lct = buildDecomposedTimeline(b);
                if (lct != Integer.MAX_VALUE) {
                    int ect = scheduleTasks(lct, false);
                    if (ect > lct) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Peak-height criterion
    // -----------------------------------------------------------------------

    /**
     * Checks whether the total resource demand at bound {@code b} exceeds the
     * resource capacity.
     *
     * <p>For each task whose decomposed LCT equals {@code b}, its height is summed.
     * If the sum exceeds {@link #capacity}, the criterion is satisfied and the
     * decomposed overload check should be applied.</p>
     *
     * @param b the candidate bound to test
     * @return {@code true} if the total height at {@code b} exceeds {@code capacity}
     */
    private boolean exceedsPeakHeight(int b) {
        int totalHeight = 0;
        for (int i = 0; i < n; i++) {
            if (decomposedLct(i, b) == b) {
                totalHeight += decomposedHeight(i, b);
            }
        }
        return totalHeight > capacity;
    }

    // -----------------------------------------------------------------------
    // Timeline construction – decomposed variant
    // -----------------------------------------------------------------------

    /**
     * Classifies tasks with respect to bound {@code b} and builds the decomposed profile.
     *
     * <p>Tasks are split into five categories depending on whether they lie entirely
     * before {@code b}, entirely after {@code b}, or straddle {@code b} in one of
     * three characteristic ways (left-overlap, right-overlap, spanning).</p>
     *
     * @param b the right boundary of the interval under consideration
     * @return  the maximum LCT of the tasks that contribute to the interval, or
     *          {@link Integer#MAX_VALUE} if no task contributes
     */
    private int buildDecomposedTimeline(int b) {
        ArrayList<Integer> byEct = new ArrayList<>();
        ArrayList<Integer> byLct = new ArrayList<>();

        ArrayList<Integer> indexTasksByEst    = new ArrayList<>();
        ArrayList<Integer> indexTasksByMiddle = new ArrayList<>();
        ArrayList<Integer> indexTasksByLst    = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (tasksByLct[i].getLct() <= b || tasksByLct[i].getLst() < b) {
                byLct.add(tasksByLct[i].getId() + 1);
            }
            if (tasksByEst[i].getLct() <= b) {
                indexTasksByEst.add(tasksByEst[i].getId() + 1);
            }
            if (tasksByMiddle[i].getLct() > b
                    && tasksByMiddle[i].getLst() < b
                    && tasksByMiddle[i].getEct() < b) {
                indexTasksByMiddle.add(tasksByMiddle[i].getId() + 1);
            }
            if (tasksByLst[i].getLct() > b
                    && tasksByLst[i].getLst() < b
                    && tasksByLst[i].getEct() >= b) {
                indexTasksByLst.add(tasksByLst[i].getId() + 1);
            }
            if (tasksByEct[i].getLct() <= b || tasksByEct[i].getLst() < b) {
                byEct.add(tasksByEct[i].getId() + 1);
            }
        }
        return buildMergedTimeline(indexTasksByEst, indexTasksByMiddle, indexTasksByLst,
                byEct, byLct, b);
    }

    /**
     * Merges the five task lists into the profile linked list (Timeline).
     *
     * <p>The merge is performed in a single O(n) scan over the five sorted lists,
     * inserting a new {@link Timepoint} whenever the current time advances.</p>
     *
     * @param byEst1 tasks sorted by decomposed EST (fully inside)
     * @param byEst2 tasks sorted by decomposed EST (middle spanning)
     * @param byEst3 tasks sorted by decomposed EST (right spanning)
     * @param byEct  tasks sorted by decomposed ECT
     * @param byLct  tasks sorted by decomposed LCT
     * @param b      the right boundary of the interval
     * @return       the maximum LCT among contributing tasks, or
     *               {@link Integer#MAX_VALUE} if no task contributes
     */
    private int buildMergedTimeline(
            ArrayList<Integer> byEst1,
            ArrayList<Integer> byEst2,
            ArrayList<Integer> byEst3,
            ArrayList<Integer> byEct,
            ArrayList<Integer> byLct,
            int b) {

        if (byEct.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        profile.reset();
        int n1 = byEst1.size();
        int n2 = byEst2.size();
        int n3 = byEst3.size();
        int m  = byEct.size();

        int est1 = n1 > 0 ? decomposedEst(byEst1.get(0), b) : Integer.MAX_VALUE;
        int est2 = n2 > 0 ? decomposedEst(byEst2.get(0), b) : Integer.MAX_VALUE;
        int est3 = n3 > 0 ? decomposedEst(byEst3.get(0), b) : Integer.MAX_VALUE;
        int firstTime = min(est1, est2, est3);

        if (firstTime == est1) {
            profile.Add(new Timepoint(decomposedEst(byEst1.get(0), b), capacity));
        } else if (firstTime == est2) {
            profile.Add(new Timepoint(decomposedEst(byEst2.get(0), b), capacity));
        } else {
            profile.Add(new Timepoint(decomposedEst(byEst3.get(0), b), capacity));
        }

        Timepoint current = profile.first;
        int totalProcessing = 0;
        int j = 0, k = 0, i1 = 0, i2 = 0, i3 = 0;
        int maxLct = Integer.MIN_VALUE;

        while (i1 < n1 || i2 < n2 || i3 < n3 || j < m || k < m) {
            if (i1 < n1
                    && (j == m  || decomposedEst(byEst1.get(i1), b) <= decomposedEct(byEct.get(j), b))
                    && (k == m  || decomposedEst(byEst1.get(i1), b) <= decomposedLct(byLct.get(k), b))
                    && (i2 == n2 || decomposedEst(byEst1.get(i1), b) <= decomposedEst(byEst2.get(i2), b))
                    && (i3 == n3 || decomposedEst(byEst1.get(i1), b) <= decomposedEst(byEst3.get(i3), b))) {

                current = advanceOrInsert(current, decomposedEst(byEst1.get(i1), b));
                current.increment    += decomposedHeight(byEst1.get(i1), b);
                current.incrementMax += decomposedHeight(byEst1.get(i1), b);
                i1++;

            } else if (j < m
                    && (k == m  || decomposedEct(byEct.get(j), b) <= decomposedLct(byLct.get(k), b))
                    && (i2 == n2 || decomposedEct(byEct.get(j), b) <= decomposedEst(byEst2.get(i2), b))
                    && (i3 == n3 || decomposedEct(byEct.get(j), b) <= decomposedEst(byEst3.get(i3), b))) {

                current = advanceOrInsert(current, decomposedEct(byEct.get(j), b));
                current.increment -= decomposedHeight(byEct.get(j), b);
                maxLct = Math.max(maxLct, decomposedLct(byEct.get(j), b));
                totalProcessing += decomposedP(byEct.get(j), b);
                current.isBound = true;
                j++;

            } else if (k < m
                    && (i2 == n2 || decomposedLct(byLct.get(k), b) <= decomposedEst(byEst2.get(i2), b))
                    && (i3 == n3 || decomposedLct(byLct.get(k), b) <= decomposedEst(byEst3.get(i3), b))) {

                current = advanceOrInsert(current, decomposedLct(byLct.get(k), b));
                current.incrementMax -= decomposedHeight(byLct.get(k), b);
                current.isBound = true;
                k++;

            } else if (i2 < n2
                    && (i3 == n3 || decomposedEst(byEst2.get(i2), b) <= decomposedEst(byEst3.get(i3), b))) {

                current = advanceOrInsert(current, decomposedEst(byEst2.get(i2), b));
                current.increment    += decomposedHeight(byEst2.get(i2), b);
                current.incrementMax += decomposedHeight(byEst2.get(i2), b);
                i2++;

            } else {
                current = advanceOrInsert(current, decomposedEst(byEst3.get(i3), b));
                current.increment    += decomposedHeight(byEst3.get(i3), b);
                current.incrementMax += decomposedHeight(byEst3.get(i3), b);
                i3++;
            }
        }

        current.InsertAfter(new Timepoint(maxLct + totalProcessing, 0));
        return maxLct;
    }

    // -----------------------------------------------------------------------
    // Timeline construction – global variant
    // -----------------------------------------------------------------------

    /**
     * Builds the global resource-consumption profile using all tasks.
     *
     * <p>Initialises the profile over the full scheduling horizon
     * {@code [minEST, maxLCT + totalP]} by merging the three sorted views
     * (by EST, ECT, LCT) in a single O(n) pass.</p>
     *
     * @return the maximum LCT across all tasks
     */
    private int buildGlobalTimeline() {
        profile.reset();
        profile.Add(new Timepoint(tasksByEst[0].getEst(), capacity));
        Timepoint current = profile.first;

        int totalP = 0, i = 0, j = 0, k = 0;
        int maxLct = Integer.MIN_VALUE;

        while (i < n || j < n || k < n) {
            if (i < n
                    && (j == n || tasksByEst[i].getEst() <= tasksByEct[j].getEct())
                    && (k == n || tasksByEst[i].getEst() <= tasksByLct[k].getLct())) {

                current = advanceOrInsert(current, tasksByEst[i].getEst());
                current.increment    += tasksByEst[i].getH();
                current.incrementMax += tasksByEst[i].getH();
                totalP += tasksByEst[i].getP();
                maxLct = Math.max(maxLct, tasksByEst[i].getLct());
                i++;

            } else if (j < n && (k == n || tasksByEct[j].getEct() <= tasksByLct[k].getLct())) {

                current = advanceOrInsert(current, tasksByEct[j].getEct());
                current.increment    -= tasksByEct[j].getH();
                current.isBound       = true;
                current.isEctBound    = true;
                j++;

            } else {

                current = advanceOrInsert(current, tasksByLct[k].getLct());
                current.incrementMax -= tasksByLct[k].getH();
                current.isBound    = true;
                current.isLctBound = true;
                k++;
            }
        }

        current.InsertAfter(new Timepoint(maxLct + totalP, 0));
        return maxLct;
    }

    // -----------------------------------------------------------------------
    // Profile simulation (scheduling)
    // -----------------------------------------------------------------------

    /**
     * Simulates resource consumption along the profile and computes the earliest
     * completion time (ECT) of the scheduled tasks.
     *
     * @param maxLct     the right boundary of the simulation (maximum LCT)
     * @param trackDelta if {@code true}, timepoints with positive overflow are added
     *                   to {@link #delta}
     * @return the ECT of the last non-idle interval, or {@link Integer#MAX_VALUE}
     *         if an overflow is detected
     */
    private int scheduleTasks(int maxLct, boolean trackDelta) {
        int hReq = 0, hMaxInc = 0, overflow = 0, ect = Integer.MIN_VALUE;
        Timepoint t = profile.first;

        while (t.time < maxLct) {
            int slotLength = t.next.time - t.time;
            t.overflow = overflow;
            hMaxInc += t.incrementMax;
            int hMax = Math.min(hMaxInc, capacity);
            hReq    += t.increment;

            int hCons = Math.min(hReq + overflow, hMax);
            t.cons = hCons;

            if (overflow > 0 && overflow < (hCons - hReq) * slotLength) {
                slotLength = Math.max(1, overflow / (hCons - hReq));
                t.InsertAfter(new Timepoint(t.time + slotLength, t.capacity));
            }

            overflow   += (hReq - hCons) * slotLength;
            t.capacity  = capacity - hCons;

            if (trackDelta && t.isBound && t.overflow > 0) {
                delta.add(t.time);
            }
            if (t.capacity < capacity) {
                ect = t.next.time;
            }
            t = t.next;
        }

        t.overflow = overflow;
        if (trackDelta && t.isBound && t.overflow > 0) {
            delta.add(t.time);
        }
        return overflow > 0 ? Integer.MAX_VALUE : ect;
    }

    // -----------------------------------------------------------------------
    // Decomposed task accessors (0-based index)
    // -----------------------------------------------------------------------

    /**
     * Returns the decomposed earliest start time of task at index {@code idx}
     * relative to bound {@code b}.
     *
     * @param idx 0-based task index
     * @param b   the right boundary
     * @return the decomposed EST, or {@code -1} if the task does not contribute
     */
    private int decomposedEst(int idx, int b) {
        Task t = tasks[idx];
        if (t.getLct() <= b) {
            return t.getEst();
        }
        if (t.getLst() < b) {
            return t.getEct() < b ? t.getEst() + t.getLct() - b : t.getLst();
        }
        return -1;
    }

    /**
     * Returns the decomposed earliest completion time of task at index {@code idx}
     * relative to bound {@code b}.
     *
     * @param idx 0-based task index
     * @param b   the right boundary
     * @return the decomposed ECT, or {@code -1} if the task does not contribute
     */
    private int decomposedEct(int idx, int b) {
        Task t = tasks[idx];
        if (t.getLct() <= b) {
            return t.getEct();
        }
        if (t.getLst() < b) {
            return t.getEct() < b ? t.getEct() : b;
        }
        return -1;
    }

    /**
     * Returns the decomposed latest completion time of task at index {@code idx}
     * relative to bound {@code b}.
     *
     * @param idx 0-based task index
     * @param b   the right boundary
     * @return the decomposed LCT, or {@code -1} if the task does not contribute
     */
    private int decomposedLct(int idx, int b) {
        Task t = tasks[idx];
        if (t.getLct() <= b) {
            return t.getLct();
        }
        if (t.getLst() < b) {
            return b;
        }
        return -1;
    }

    /**
     * Returns the height (resource demand) of task at index {@code idx}.
     *
     * @param idx 0-based task index
     * @param b   the right boundary (unused, kept for API consistency)
     * @return the task height
     */
    private int decomposedHeight(int idx, int b) {
        return tasks[idx].getH();
    }

    /**
     * Returns the decomposed processing time of task at index {@code idx}
     * relative to bound {@code b}.
     *
     * @param idx 0-based task index
     * @param b   the right boundary
     * @return the portion of processing time within {@code [*, b]}
     */
    private int decomposedP(int idx, int b) {
        Task t = tasks[idx];
        return t.getLct() <= b ? t.getP() : Math.max(0, b - t.getLst());
    }

    // -----------------------------------------------------------------------
    // Helper utilities
    // -----------------------------------------------------------------------

    /**
     * Advances the current timepoint pointer to {@code time}, inserting a new
     * {@link Timepoint} if necessary.
     *
     * @param current the current position in the linked list
     * @param time    the target time
     * @return the (possibly newly inserted) timepoint at {@code time}
     */
    private Timepoint advanceOrInsert(Timepoint current, int time) {
        if (time > current.time) {
            current.InsertAfter(new Timepoint(time, capacity));
            current = current.next;
        }
        return current;
    }

    /**
     * Returns the minimum of the given integer values.
     *
     * @param values at least one integer value
     * @return the minimum value
     */
    private int min(int... values) {
        int min = Integer.MAX_VALUE;
        for (int v : values) {
            if (v < min) min = v;
        }
        return min;
    }
}
