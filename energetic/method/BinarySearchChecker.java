package energetic.method;


import cumulative.Task;
import datastructures.IntervalChain;
import datastructures.SlackDatastructure;
import datastructures.SortingVector;
import datastructures.Vector;
import energetic.slack.InconsistentException;
import energetic.slack.LogarithmicSlackDatastructure;

@SuppressWarnings("Duplicates")
public class BinarySearchChecker {
    private Task[] tasks;
    private int C;
    private SlackDatastructure datastructure;

    private SortingVector originalO1;
    private SortingVector originalO2;
    private SortingVector originalOt;

    private Vector setO1;
    private Vector setO2;
    private Vector setOt;
    private Vector minimumIndexO2;
    private Vector minimumIndexOt;

    private int o1Size;
    private int maxUpper;
    private int minLower;
    private int n;

    private IntervalChain chain;


    private static final int INF = Integer.MAX_VALUE;
    private static final int NEG_INF = 0;


    public BinarySearchChecker(Task[] tasks, int C) {
        this(tasks, C, new LogarithmicSlackDatastructure(tasks, C));
    }

    public BinarySearchChecker(Task[] tasks, int C, SlackDatastructure datastructure) {
        this.tasks = tasks;
        this.C = C;
        this.datastructure = datastructure;


        this.n = tasks.length;

        int numberOfO1Intervals = 2;
        int numberOfO2Intervals = 3;

        this.originalO1 = new SortingVector(numberOfO1Intervals * n);
        this.originalO2 = new SortingVector(numberOfO2Intervals * n);
        this.originalOt = new SortingVector(n);
        this.setO1 = new Vector(originalO1.size());
        this.setO2 = new Vector(originalO2.size());
        this.setOt = new Vector(n);

        this.minimumIndexO2 = new Vector(originalO1.size());
        this.minimumIndexOt = new Vector(originalO1.size());

        o1Size = 0;

        for (int i = 0; i < n; i++) {
            originalO1.set(o1Size++, tasks[i].getEst());
            originalO1.set(o1Size++, tasks[i].getLst());

            originalO2.set(numberOfO2Intervals * i, tasks[i].getEct());
            originalO2.set(numberOfO2Intervals * i + 1, tasks[i].getLct());
            originalO2.set(numberOfO2Intervals * i + 2, tasks[i].getLst());

            originalOt.set(i, tasks[i].getEst() + tasks[i].getLct());
        }

        update();
    }

    public void update() {
        int numberOfO1Intervals = 2;
        int numberOfO2Intervals = 3;

        int count = 0;

        for (int i = 0; i < n; i++) {
            originalO1.setOriginal(count++, tasks[i].getEst());
            originalO1.setOriginal(count++, tasks[i].getLst());

            originalO2.setOriginal(numberOfO2Intervals * i, tasks[i].getEct());
            originalO2.setOriginal(numberOfO2Intervals * i + 1, tasks[i].getLct());
            originalO2.setOriginal(numberOfO2Intervals * i + 2, tasks[i].getLst());

            originalOt.setOriginal(i, tasks[i].getEst() + tasks[i].getLct());
        }

        originalO1.sort();
        originalO2.sort();
        originalOt.sort();

        removeDuplicatesInO1();
        setO2 = removeDuplicates(originalO2, setO2);
        setOt = removeDuplicates(originalOt, setOt);

        minimumIndexO2.resize(setO1.size());
        minimumIndexOt.resize(setO1.size());
        int j = 0;
        int k = 0;
        for (int i = 0; i < setO1.size(); i++) {
            while (j < setO2.size() && setO1.get(i) >= setO2.get(j)) {
                j++;
            }
            while (k < setOt.size() && setO1.get(i) >= setOt.get(k) - setO1.get(i)) {
                k++;
            }
            minimumIndexO2.set(i, Math.min(j, setO2.size()-1));
            minimumIndexOt.set(i, k);
            if (k >= setOt.size())
                minimumIndexOt.set(i, -1);
        }

        maxUpper = Math.max(setO2.get(setO2.size()-1), setOt.get(setOt.size()-1) - setO1.get(0));
        minLower = Math.min(setO2.get(0), setOt.get(0) - setO1.get(setO1.size()-1));
        minLower = Math.max(0, minLower);

        datastructure = new LogarithmicSlackDatastructure(tasks, C);
    }

    private void removeDuplicatesInO1() {
        int size = 1;
        for (int i = 1; i < originalO1.size(); i++) {
            if (originalO1.get(i) != originalO1.get(i-1))
                size++;
        }

        setO1.resize(size);
        setO1.set(0, originalO1.get(0));
        int k = 1;

        for (int i = 1; i < originalO1.size(); i++) {
            if (k < size && originalO1.get(i) != setO1.get(k-1)) {
                setO1.set(k++, originalO1.get(i));
            }
        }
    }

    private Vector removeDuplicates(SortingVector original, Vector set) {
        int size = 1;
        for (int i = 1; i < original.size(); i++) {
            if (original.get(i) != original.get(i-1))
                size++;
        }

        set.resize(size);
        set.set(0, original.get(0));
        int k = 1;

        for (int i = 1; i < original.size(); i++) {
            if (k < size && original.get(i) != set.get(k-1)) {
                set.set(k++, original.get(i));
            }
        }

        return set;
    }

    private int findUpperInO2(int lowerIndex) throws InconsistentException {
        int l = setO1.get(lowerIndex);
        int left = minimumIndexO2.get(lowerIndex);
        int right = this.setO2.size();

        int middle = -1;
        int min = -1;

        while (left < right) {
            middle = (left + right) / 2;
            int u = setO2.get(middle);
            int newValue = computeSlack(l, u);

            int lStar = setO1.get(chain.findAssociatedLower(u));
            int oldMin = computeSlack(lStar, u);

            if (newValue < oldMin) {
                left = middle + 1;
                min = middle;
            } else {
                right = middle;
            }
        }

        return min;
    }

    private int findUpperInOt(int lowerIndex) throws InconsistentException {
        int l = setO1.get(lowerIndex);
        int left = minimumIndexOt.get(lowerIndex);
        if (left == -1)
            return -1;

        int right = this.setOt.size();
        int middle = -1;
        int min = -1;

        while (left < right) {
            middle = (left + right) / 2;
            int u = setOt.get(middle);
            int newValue = computeSlack(l, u-l);

            int lStar = setO1.get(chain.findAssociatedLower(u-l));
            int oldMin = computeSlack(lStar, u - l);

            if (newValue < oldMin) {
                left = middle + 1;
                min = middle;
            } else {
                right = middle;
            }
        }

        return min;
    }

    private int findRealCrossingPoint(int l, int u, int indexO2, int indexOt)
    		throws InconsistentException {
    	int prevO2 = indexO2 < setO2.size() - 1 ? setO2.get(indexO2 + 1) : INF;
    	int prevOt = indexOt < setOt.size() - 1 ? setOt.get(indexOt + 1) - l : INF;
    	if (prevOt < 0) { // If l > Ot[i], it will be negative (and irrelevant)
    		prevOt = INF;
    	}

    	int p1Current = Math.min(prevO2, prevOt);

    	int lStar = setO1.get(chain.findAssociatedLower(u));
    	int indexPrevOtLStar = findIndexInOt(u, lStar, false) + 1;

    	int prevOtLStar = indexPrevOtLStar < setOt.size() ? setOt.get(indexPrevOtLStar) - lStar : INF;
    	int p1LStar = Math.min(prevOtLStar, prevO2);

    	if (p1Current == INF || p1LStar == INF) {
    		return u;
    	}

    	int p1 = Math.max(p1Current, p1LStar);

    	int p1CurrentSlack = computeSlack(l, p1Current);
    	int p2CurrentSlack = computeSlack(l, u);

    	int p1LStarSlack = computeSlack(lStar, p1LStar);
    	int p2LStarSlack = computeSlack(lStar, u);
    	int dxCurrent = u - p1Current;
    	int dxLStar = u - p1LStar;

    	if (dxCurrent == 0 || dxLStar == 0) {
    		return u;
    	}

    	float slopeCurrent = (p2CurrentSlack - p1CurrentSlack) / dxCurrent;
    	float bCurrent = p2CurrentSlack - slopeCurrent * u;

    	float slopeLStar = (p2LStarSlack - p1LStarSlack) / dxLStar;
    	float bLStar = p2LStarSlack - slopeLStar * u;

    	if (slopeCurrent - slopeLStar == 0) {
    		return u;
    	}

    	int intersection = (int) ((bLStar - bCurrent) / (slopeCurrent - slopeLStar));

    	if (intersection > p1) {
    		return u;
    	}

    	return intersection;
    }

    public boolean isConsistent() {
        return isConsistent(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public boolean isConsistent(int minO1, int maxO1, int minO2, int maxO2) {
        try {
            return _isConsistent(minO1, maxO1, minO2, maxO2);
        } catch (InconsistentException e) {
            return false;
        }
    }

    private boolean _isConsistent(int minO1, int maxO1, int minO2, int maxO2)
        throws InconsistentException {


        chain = new IntervalChain(minLower, maxUpper, setO2.size()- 1, 0, setO1.size());

        try {
            for (int i = 1; i < setO1.size() && setO1.get(i) <= maxO1; i++) {
                if (setO1.get(i) < minO1) {
                    continue;
                }

                int l = setO1.get(i);
                int indexO2 = findUpperInO2(i);
                int indexOt = findUpperInOt(i);
                if (indexO2 == -1 && indexOt == -1)
                    continue;

                int valueO2 = indexO2 >= 0 ? setO2.get(indexO2) : -1;
                int valueOt = indexOt >= 0 ? setOt.get(indexOt) - l : -1;
                int max = Math.max(valueO2, valueOt);
                max = findRealCrossingPoint(l, max, indexO2, indexOt);
                if (max > l+1) {
                    chain.replace(l+1, max, -1, -1, i);
                }
            }
        } catch (InconsistentException e) {
            return false;
        }

        int lowerIndexO2 = 0;
        IntervalChain.Interval current = chain.next();
        while (current != null) {
            int t1 = setO1.get(current.value);

            if (t1 <= maxO1) {
                int i;
                for (i = lowerIndexO2; i < setO2.size() && setO2.get(i) < current.u; i++) {
                    int t2 = setO2.get(i);
                    if (t2 > t1 && computeSlack(t1, t2) < 0)
                        return false;
                }
                lowerIndexO2 = i;

                t1 = setO1.get(current.value);
                int lowerIndex = findIndexInOt(current.l, t1, true);
                int upperIndex = findIndexInOt(current.u, t1, false);
                for (i = lowerIndex; i <= upperIndex; i++) {
                    int t2 = setOt.get(i) - t1;
                    if (t2 > t1 && computeSlack(t1, t2) < 0)
                        return false;
                }
            }

            current = chain.next();
        }

        return true;
    }

    private int findIndexInOt(int value, int l, boolean findGreater) {
        int left = 0;
        int right = setOt.size();
        int middle = -1;

        while (left < right) {
            middle = (left + right) / 2;
            if (setOt.get(middle) - l == value) {
                return middle;
            }
            if (value < setOt.get(middle) - l) {
                right = middle;
            } else {
                left = middle + 1;
            }
        }

        if (findGreater && value > setOt.get(middle) - l)
            return middle + 1;
        return middle;
    }

    private int findIndexInO2(int value, boolean findGreater) {
        int left = 0;
        int right = setO2.size();
        int middle = -1;

        while (left < right) {
            middle = (left + right) / 2;
            if (setO2.get(middle) == value) {
                return middle;
            }
            if (value < setO2.get(middle)) {
                right = middle;
            } else {
                left = middle + 1;
            }
        }

        if (findGreater && value > setO2.get(middle))
            return middle + 1;
        return middle;
    }

    private int computeSlack(int t1, int t2) throws InconsistentException {
       int slack = datastructure.querySlack(t1, t2);

       if (slack < 0 && t2 > t1) {
           throw new InconsistentException();
       }
       return slack;
    }



    public void setDatastructure(SlackDatastructure datastructure) {
        this.datastructure = datastructure;
    }
}
