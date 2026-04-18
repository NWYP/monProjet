package energetic.slack;

import cumulative.Task;
import datastructures.PartialSum;
import datastructures.Point;
import datastructures.RangeTree;
import datastructures.SlackDatastructure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LogarithmicSlackDatastructure implements SlackDatastructure {
    private Task[] tasksWithtoutCompulsary;
    private PartialSum compulsaryPartialSum;
    private RangeTree treeS1;
    private RangeTree treeS2;
    private int infinity;
    private int C;
    private HashMap<Integer, Integer> cache;
    //private CumulativeArguments args;

    //public LogarithmicSlackDatastructure(Task[] tasks, int C, CumulativeArguments args) {
    public LogarithmicSlackDatastructure(Task[] tasks, int C) {
        this.C = C;
        //this.args = args;
        int n = tasks.length;

        Point[] s1 = new Point[n];
        Point[] s2 = new Point[n];
        List<PartialSum.Tuple> compulsary = new LinkedList<>();

        tasksWithtoutCompulsary = new Task[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Task t = tasks[i];
            Task tPrime = tasksWithtoutCompulsary[i] = new Task(t.getId() + 1, t.getEst(), t.getLct(),
                    t.getP() - Math.max(0, t.getEct() - t.getLst()),
                    t.getH());


            s1[i] = new Point(-tPrime.getLct(), -tPrime.getLst(), -1 * (tPrime.getEst() + tPrime.getLct()), tPrime.getH());
            s2[i] = new Point(tPrime.getEst(), tPrime.getEct(), -1 * (-tPrime.getEst() - tPrime.getLct()), tPrime.getH());

            if (t.getEct() > t.getLst()) {
                compulsary.add(new PartialSum.Tuple(t.getLst(), t.getH()));
                compulsary.add(new PartialSum.Tuple(t.getEct(), -t.getH()));
            }

            if (t.getLct() > infinity)
                infinity = t.getLct();
        }

        treeS1 = new RangeTree(s1);
        treeS2 = new RangeTree(s2);
        compulsaryPartialSum = new PartialSum(compulsary.toArray(new PartialSum.Tuple[compulsary.size()]));

        infinity *= 3;

        cache = new HashMap<>();
        //if (args.useCache) {
        //    cache = new HashMap<>();
        //}

        //if (args.useVirtualInitialisation) {
        //    args.virtualCache.reinitialise();
        //}
    }

    public int query(int l, int u) {
        return treeS1.query(-u, infinity, -(l+u))
                + treeS2.query(l, infinity, (l + u - 1))
                + compulsaryPartialSum.computePartialSum(l, u);
    }

    /*public int querySlack(int l, int u) {
        if (!(args.useCache || args.useVirtualInitialisation)) {
            return C * (u - l) - query(l, u);
        }
        int key = l * infinity + u;

        int slack;

        if (args.useVirtualInitialisation) {
            if (args.virtualCache.containsKey(key)) {
                return args.virtualCache.get(key);
            }
            slack = C * (u - l) - query(l, u);
            args.virtualCache.put(key, slack);

            return slack;
        }

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        slack = C * (u - l) - query(l, u);
        cache.put(key, slack);

        return slack;
    }*/
    public int querySlack(int l, int u) {
    	//return C * (u - l) - query(l, u);
    	
    	int key = l * infinity + u;
    	int slack;
        if (cache.containsKey(key)) return cache.get(key);
        slack = C * (u - l) - query(l, u);
        cache.put(key, slack);
        return slack;
        
    }

    public int getInfinity() {
        return infinity;
    }
}
