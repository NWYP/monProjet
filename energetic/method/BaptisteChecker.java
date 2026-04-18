package energetic.method;

import cumulative.Task;

import java.util.Arrays;

@SuppressWarnings("Duplicates")
//public class BaptisteEnergeticChecker implements FilteringAlgorithm {
public class BaptisteChecker {
    private boolean useDerrienCaracterisation;
    
    private int n;
    private int C;
    
    private Task[] tasks;
    private Task[] tasksByLct;
    private Task[] tasksByEct;
    private Task[] tasksByLst;
    private Task[] tasksMiddle;
    private int[] setO1;


    public BaptisteChecker(Task[] tasks, int C, boolean der) {
        this.useDerrienCaracterisation = der;
        this.C = C;
        this.tasks = tasks;
        n = tasks.length;
        initialiseSets();
    }
    public BaptisteChecker(Task[] tasks, int C) {
        this(tasks, C, false);
    }
    public BaptisteChecker(Task[] tasks) {
        this(tasks, 1, false);
    }

    public void initialiseSets() {
        this.update();
    }
    
    public void update() {
        this.tasksByLct = Arrays.copyOf(tasks, n);
        this.tasksByEct = Arrays.copyOf(tasks, n);
        this.tasksByLst = Arrays.copyOf(tasks, n);
        this.tasksMiddle = Arrays.copyOf(tasks, n);

        Arrays.sort(tasksByLct, (Task t1, Task t2) -> t1.getLct() - t2.getLct());
        Arrays.sort(tasksByEct, (Task t1, Task t2) -> t1.getEct() - t2.getEct());
        Arrays.sort(tasksByLst, (Task t1, Task t2) -> t1.getLst() - t2.getLst());
        Arrays.sort(tasksMiddle, (Task t1, Task t2) -> t1.getEst() + t1.getLct() - t2.getEst() - t2.getLct());
        makeSets();
    }

    public boolean isConsistent() {
        assert setO1 != null;
        int n = tasksByLct.length;
        int[][] markers = new int[n][];
        for (int i = 0; i < n; i++) {
            markers[i] = new int[4];
        }

        for (int t1 : setO1) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < 4; j++) {
                    markers[i][j] = -1;
                }
            }

            int iLct = 0;
            int iEct = 0;
            int iLst = 0;
            int iMiddle = 0;
            int W = 0;
            int t2Old = t1;
            int slope = computeSlope(t1);

            while (iLct < n || iEct < n || iLst < n || iMiddle < n) {
                int t2Lct = Integer.MAX_VALUE;
                int t2Ect = Integer.MAX_VALUE;
                int t2Lst = Integer.MAX_VALUE;
                int t2Middle = Integer.MAX_VALUE;
                if (iLct < n) {
                    t2Lct = tasksByLct[iLct].getLct();
                }
                if (iEct < n) {
                    t2Ect = tasksByEct[iEct].getEct();
                }
                if (iLst < n) {
                    t2Lst = tasksByLst[iLst].getLst();
                }
                if (iMiddle < n) {
                    t2Middle = tasksMiddle[iMiddle].getEst() + tasksMiddle[iMiddle].getLct() - t1;
                }

                int t2 = min(t2Lct, t2Ect, t2Lst, t2Middle);
                int j = -1;

                Task task = null;
                if (t2 == t2Lct) {
                    task = tasksByLct[iLct++];
                    j = 0;
                } else if (t2 == t2Ect) {
                    task = tasksByEct[iEct++];
                    j = 1;
                } else if (t2 == t2Lst) {
                    task = tasksByLst[iLst++];
                    j = 2;
                } else if (t2 == t2Middle) {
                    task = tasksMiddle[iMiddle++];
                    j = 3;
                } else {
                    assert false;
                }

                int i = task.getId();
                if (t1 < t2 && t2 != markers[i][0] && t2 != markers[i][1] && t2 != markers[i][2] && t2 != markers[i][3]) {
                    markers[i][j] = t2;
                    W += slope * (t2 - t2Old);
                    if (C * (t2 - t1) - W < 0) {
//                    	System.out.println("t1 - t2: "+t1+"-"+t2);
//                        if (t2 == t2Lct) {
//                        	System.out.println("t2Lct "+task.getId());
//                        } else if (t2 == t2Ect) {
//                        	System.out.println("t2Ect "+task.getId());
//
//                        } else if (t2 == t2Lst) {
//                        	System.out.println("t2Lst "+task.getId());
//                        } else if (t2 == t2Middle) {
//                        	System.out.println("t2Middle "+task.getId());
//                        } 
                        return false;
                    }
                    t2Old = t2;
                    slope += minimumIntersection(task, t1, t2+1)
                        - 2 * minimumIntersection(task, t1, t2)
                        + minimumIntersection(task, t1, t2 - 1);
                }
            }
        }
        return true;
    }
    
    private int min(int... elems) {
        assert elems.length > 1;

        int min = Integer.MAX_VALUE;
        for (int elem : elems) {
            if (elem < min) {
                min = elem;
            }
        }
        return min;
    }

    private int computeSlope(int t1) {
        int slope = 0;
        for (int j = 0; j < n; j++) {
            slope += minimumIntersection(tasksByEct[j], t1, t1+1);
        }
        return slope;
    }

    private int minimumIntersection(Task task, int t1, int t2) {
        return task.getH() * Math.max(0, min(
                t2 - t1,
                task.getP(),
                task.getEct() - t1,
                t2 - task.getLst())
        );
    }

    private void makeSets() {
        int size = useDerrienCaracterisation ? 2 : 3;
        setO1 = new int[n * size];

        for (int i = 0; i < n; i++) {
            Task task = tasksByEct[i];
            int position = i * size;
            setO1[position] = task.getEst();
            setO1[position+1] = task.getLst();
            if (!useDerrienCaracterisation) {
                setO1[position+2] = task.getEct();
            }
        }

        Arrays.sort(setO1);

        for (int i = 0; i < n*size-1; i++)
            assert(setO1[i] <= setO1[i+1]);
    }

        
}
