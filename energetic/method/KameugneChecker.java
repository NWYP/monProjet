package energetic.method;

import cumulative.Task;
import profile.Profile;
import profile.Timepoint;

import java.util.ArrayList;
import java.util.Arrays;

public class KameugneChecker {
    Integer C;
    int n;
    Task[] tasks;
    private Profile tl;
    Integer makespan;
    ArrayList<Integer> delta;

    private final Task[] tasksByEst;
    private final Task[] tasksByEct;
    private final Task[] tasksByLst;
    private final Task[] tasksByLct;
    private final Task[] tasksByMiddle;
    public KameugneChecker(Task[] tasks, int C) {
        this.C = C;
        this.tasks = tasks;
        this.tl = new Profile();
        n = tasks.length;
        makespan = Integer.MIN_VALUE;
        delta = new ArrayList<>();

        this.tasksByEst = Arrays.copyOf(tasks, n);
        this.tasksByEct = Arrays.copyOf(tasks, n);
        this.tasksByLst = Arrays.copyOf(tasks, n);
        this.tasksByLct = Arrays.copyOf(tasks, n);
        this.tasksByMiddle = Arrays.copyOf(tasks, n);

        Arrays.sort(tasksByEst, (Task t1, Task t2) -> Integer.compare(t1.getEst(), t2.getEst()));
        Arrays.sort(tasksByEct, (Task t1, Task t2) -> Integer.compare(t1.getEct(), t2.getEct()));
        Arrays.sort(tasksByLst, (Task t1, Task t2) -> Integer.compare(t1.getLst(), t2.getLst()));
        Arrays.sort(tasksByLct, (Task t1, Task t2) -> Integer.compare(t1.getLct(), t2.getLct()));
        Arrays.sort(tasksByMiddle, (Task t1, Task t2) -> Integer.compare(t1.getEst() + t1.getLct(), t2.getEst() + t2.getLct()));

    }

    public boolean isConsistent()
    {
        int maxLct = InitializeTimeLine();
        int span = ScheduleTasks(maxLct, true);
        if(span > maxLct)
            return false;
        for (Integer b : delta) {
            int Lct = decomposedSortedTasksInitializeTimeLine(b);
            if (Lct != Integer.MAX_VALUE) {
                int ect = ScheduleTasks(Lct, false);
                if (ect > Lct)
                    return false;
            }
        }
        return true;
    }

    private int decomposedSortedTasksInitializeTimeLine(Integer b) {
        int n = tasks.length;
        ArrayList<Integer> byEst = new ArrayList<>();
        ArrayList<Integer> byEct = new ArrayList<>();
        ArrayList<Integer> byLct = new ArrayList<>();

        ArrayList<Integer> indexTasksByEst = new ArrayList<>();
        ArrayList<Integer> indexTasksByMiddle = new ArrayList<>();
        ArrayList<Integer> indexTasksByLst = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (tasksByLct[i].getLct() <= b) {
                int id = tasksByLct[i].getId();
                byLct.add(id);
            } else {
                if (tasksByLct[i].getLst() < b) {
                    int id = tasksByLct[i].getId();
                    byLct.add(id);
                }
            }
            if (tasksByEst[i].getLct() <= b) {
                int id = tasksByEst[i].getId();
                indexTasksByEst.add(id);
            }
            if (tasksByMiddle[i].getLct() > b && tasksByMiddle[i].getLst() < b && tasksByMiddle[i].getEct() < b) {
                int id = tasksByMiddle[i].getId();
                indexTasksByMiddle.add(id);
            }
            if (tasksByLst[i].getLct() > b && tasksByLst[i].getLst() < b && tasksByLst[i].getEct() >= b) {
                int id = tasksByLst[i].getId();
                indexTasksByLst.add(id);
            }
            if (tasksByEct[i].getLct() <= b) {
                int id = tasksByEct[i].getId();
                byEct.add(id);
            } else {
                if (tasksByEct[i].getLst() < b) {
                    int id = tasksByEct[i].getId();
                    byEct.add(id);
                }
            }
        }
        return InitializeTimeLine1(indexTasksByEst,indexTasksByMiddle,indexTasksByLst,byEct,byLct,b);
    }


    private int InitializeTimeLine1(ArrayList<Integer> by_est1, ArrayList<Integer> by_est2, ArrayList<Integer> by_est3, ArrayList<Integer> by_ect, ArrayList<Integer> by_lct, int b)
    {
        tl.reset();
        int n1 = by_est1.size();
        int n2 = by_est2.size();
        int n3 = by_est3.size();
        int n = by_ect.size();

        if (!by_est1.isEmpty())
            tl.Add(new Timepoint(decomposedTasksEst(by_est1.get(0), b), C));
        else {
            if (!by_est2.isEmpty() && by_est3.isEmpty()) {
                tl.Add(new Timepoint(decomposedTasksEst(by_est2.get(0), b), C));
            }
            if (by_est2.isEmpty() && !by_est3.isEmpty()) {
                tl.Add(new Timepoint(decomposedTasksEst(by_est3.get(0), b), C));
            }
            if (!by_est2.isEmpty() && !by_est3.isEmpty()) {
                if (decomposedTasksEst(by_est2.get(0), b) < decomposedTasksEst(by_est3.get(0),b))
                    tl.Add(new Timepoint(decomposedTasksEst(by_est2.get(0), b), C));
                else tl.Add(new Timepoint(decomposedTasksEst(by_est3.get(0), b), C));
            }
        }
        Timepoint t = tl.first;

        int p, j, k, i1, i2, i3;
        p = j = k = i1 = i2 = i3 = 0;

        int maxLCT = Integer.MIN_VALUE;
        if (n != 0) {
            while (i1 < n1 || i2 < n2 || i3 < n3 || j < n || k < n) {
                if (i1 < n1 && (j == n || decomposedTasksEst(by_est1.get(i1), b) <= decomposedTasksEct(by_ect.get(j), b)) &&
                        (k == n || decomposedTasksEst(by_est1.get(i1), b) <= decomposedTasksLct(by_lct.get(k), b)) &&
                        (i2 == n2 || decomposedTasksEst(by_est1.get(i1), b) <= decomposedTasksEst(by_est2.get(i2), b)) &&
                        (i3 == n3 || decomposedTasksEst(by_est1.get(i1), b) <= decomposedTasksEst(by_est3.get(i3), b))) {
                    if (decomposedTasksEst(by_est1.get(i1), b) > t.time) {
                        t.InsertAfter(new Timepoint(decomposedTasksEst(by_est1.get(i1), b), C));
                        t = t.next;
                    }
                    t.increment += decomposedTasksHeight(by_est1.get(i1), b);
                    t.incrementMax += decomposedTasksHeight(by_est1.get(i1), b);
                    i1++;
                } else if (j < n && (k == n || decomposedTasksEct(by_ect.get(j), b) <= decomposedTasksLct(by_lct.get(k), b)) &&
                        (i2 == n2 || decomposedTasksEct(by_ect.get(j), b) <= decomposedTasksEst(by_est2.get(i2), b)) &&
                        (i3 == n3 || decomposedTasksEct(by_ect.get(j), b) <= decomposedTasksEst(by_est3.get(i3), b))) {
                    if (decomposedTasksEct(by_ect.get(j), b) > t.time) {
                        t.InsertAfter(new Timepoint(decomposedTasksEct(by_ect.get(j), b), C));
                        t = t.next;
                    }
                    t.increment -= decomposedTasksHeight(by_ect.get(j), b);
                    maxLCT = Math.max(maxLCT, decomposedTasksLct(by_ect.get(j), b));
                    p += decomposedTasksP(by_ect.get(j), b);
                    t.isBound = true;
                    j++;
                } else if (k < n && (i2 == n2 || decomposedTasksLct(by_lct.get(k), b) <= decomposedTasksEst(by_est2.get(i2), b)) &&
                        (i3 == n3 || decomposedTasksLct(by_lct.get(k), b) <= decomposedTasksEst(by_est3.get(i3), b))) {
                    if (decomposedTasksLct(by_lct.get(k), b) > t.time) {
                        t.InsertAfter(new Timepoint(decomposedTasksLct(by_lct.get(k), b), C));
                        t = t.next;
                    }
                    t.incrementMax -= decomposedTasksHeight(by_lct.get(k), b);
                    t.isBound = true;
                    k++;
                } else if (i2 < n2 && (i3 == n3 || decomposedTasksEst(by_est2.get(i2), b) <= decomposedTasksEst(by_est3.get(i3), b))) {
                    if (decomposedTasksEst(by_est2.get(i2), b) > t.time) {
                        t.InsertAfter(new Timepoint(decomposedTasksEst(by_est2.get(i2), b), C));
                        t = t.next;
                    }
                    t.increment += decomposedTasksHeight(by_est2.get(i2), b);
                    t.incrementMax += decomposedTasksHeight(by_est2.get(i2), b);
                    i2++;
                } else {
                    if (decomposedTasksEst(by_est3.get(i3), b) > t.time) {
                        t.InsertAfter(new Timepoint(decomposedTasksEst(by_est3.get(i3), b), C));
                        t = t.next;
                    }
                    t.increment += decomposedTasksHeight(by_est3.get(i3), b);
                    t.incrementMax += decomposedTasksHeight(by_est3.get(i3), b);
                    i3++;
                }
            }
            t.InsertAfter(new Timepoint(maxLCT + p, 0));
            return maxLCT;
        } else
            return Integer.MAX_VALUE;
    }

    private int decomposedTasksEst(int i, int b) {
        if (tasks[i].getLct() <= b) {
            return tasks[i].getEst();
        } else {
            if (tasks[i].getLst() < b) {
                if (tasks[i].getEct() < b) {
                    return tasks[i].getEst() + tasks[i].getLct() - b;
                } else {
                    return tasks[i].getLst();
                }
            }
        }
        return -1;
    }

    private int decomposedTasksEct(int i, int b) {
        if (tasks[i].getLct() <= b) {
            return tasks[i].getEct();
        } else {
            if (tasks[i].getLst() < b) {
                if (tasks[i].getEct() < b) {
                    return tasks[i].getEct();
                } else {
                    return b;
                }
            }
        }
        return -1;
    }

    private int decomposedTasksLct(int i, int b) {
        if (tasks[i].getLct() <= b) {
            return tasks[i].getLct();
        } else {
            return b;
        }
    }
    private int decomposedTasksHeight(int i, int b) {
        return tasks[i].getH();
    }
    private int decomposedTasksP(int i, int b) {
        if (tasks[i].getLct() <= b) {
            return tasks[i].getP();
        } else {
            return Math.max(0, b - tasks[i].getLst());
        }
    }



    private int ScheduleTasks(int maxLCT, boolean with_delta)
    {
        int hreq, hmaxInc, ov, ect;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = 0;
        Timepoint t = tl.first;

        while(t.time < maxLCT)
        {
            int l = t.next.time - t.time;
            t.overflow = ov;
            hmaxInc += t.incrementMax;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;

            int hcons = Math.min(hreq + ov, hmax);
            t.cons = hcons;

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;

            t.capacity = C - hcons;

            if (t.isBound && t.overflow > 0 && with_delta) {
                delta.add(t.time);
            }
            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        t.overflow = ov;
        if (t.isBound && t.overflow > 0 && with_delta) {
            delta.add(t.time);
        }
        if(ov > 0)
            return Integer.MAX_VALUE;

        return ect;
    }

    private int InitializeTimeLine()
    {
        tl.reset();
        int n = tasks.length;
        tl.Add(new Timepoint(tasksByEst[0].getEst(), C));
        Timepoint t = tl.first;

        int p,i,j,k;
        p = i = j = k = 0;

        int maxLCT = Integer.MIN_VALUE;

        while(i < n || j < n || k < n)
        {
            if(i<n && (j == n || tasksByEst[i].getEst() <= tasksByEct[j].getEct()) &&
                    (k == n || tasksByEst[i].getEst() <= tasksByLct[k].getLct()))
            {
                if(tasksByEst[i].getEst() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByEst[i].getEst(), C));
                    t = t.next;
                }
                t.increment += tasksByEst[i].getH();
                t.incrementMax += tasksByEst[i].getH();
                p += tasksByEst[i].getP();
                maxLCT = Math.max(maxLCT, tasksByEst[i].getLct());
                i++;
            }
            else if(j < n && (k==n || tasksByEct[j].getEct() <= tasksByLct[k].getLct()))
            {
                if(tasksByEct[j].getEct() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByEct[j].getEct(), C));
                    t = t.next;
                }
                t.increment -= tasksByEct[j].getH();
                t.isBound = true;
                j++;
            }
            else
            {
                if(tasksByLct[k].getLct() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByLct[k].getLct(), C));
                    t = t.next;
                }
                t.incrementMax -= tasksByLct[k].getH();
                t.isBound = true;
                k++;
            }
        }
        t.InsertAfter(new Timepoint(maxLCT + p, 0));
        return maxLCT;
    }




    /* ------------ Utility Functions --------------*/

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
}
