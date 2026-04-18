package energetic.method;

import MongeMatrix.SMAWKOpt;
import MongeMatrix.SlackMatrixLight;
import cumulative.Task;
import datastructures.AVL;
import energetic.slack.LogarithmicSlackDatastructure;

import java.util.Arrays;
import java.util.Comparator;


public class SCJPChecker {
	private boolean useFullCheck;
	private int n;
    private int C;
    
    private Task[] tasks;
    private Task[] tasksByEst;
    private Task[] tasksByEct;
    private Task[] tasksByLst;

    private LogarithmicSlackDatastructure slack;
    //private SMAWK smawk;
    private SMAWKOpt smawk;
    
    public SCJPChecker(Task[] tasks, int C) {
        this(tasks, C, false);
    }
    public SCJPChecker(Task[] tasks) {
        this(tasks, 1, false);
    }

    public SCJPChecker(Task[] tasks, int C, boolean UFC) {
    	
    	this.useFullCheck = UFC;
        this.C = C;
        this.tasks = tasks;
        this.n = tasks.length;
        this.slack = new LogarithmicSlackDatastructure(this.tasks, this.C);
        
        //if(useFullCheck) this.smawk = new SMAWKOpt(new SlackMatrixLight(tasks, slack));

        initialiseSets();
    }
    
    public void initialiseSets() {
        this.update();
    }
    
    public void update() {
    	this.tasksByEst = Arrays.copyOf(tasks, n);
        this.tasksByEct = Arrays.copyOf(tasks, n);
        this.tasksByLst = Arrays.copyOf(tasks, n);
        
        Arrays.sort(tasksByEst, (Task t1, Task t2) -> Double.compare(t1.getEstD(), t2.getEstD()));
        Arrays.sort(tasksByEct, (Task t1, Task t2) -> Double.compare(t1.getEctD(), t2.getEctD()));
        Arrays.sort(tasksByLst, (Comparator<Task>) (Task t1, Task t2) -> Double.compare(t1.getLstD(n), t2.getLstD(n)));
                
    }

    public boolean isConsistent() {
    	if (useFullCheck) return  check1()&&check2();
		return check1();
    	//return check2();

    }
    
    public boolean check1() {
    		int iR = 0;
    		int iRp = 0;
    		int iDp = 0;
    		int Alpha, Delta;
    		AVL K = new AVL();
    		//boolean cons = true;
    		while (iR < n || iRp < n || iDp < n) {
    			double t2R = Double.MAX_VALUE;
    			double t2Rp = Double.MAX_VALUE;
    			double t2Dp = Double.MAX_VALUE;
    			if (iR  < n) 	t2R = tasksByEst[iR].getEstD();
    			if (iRp < n) 	t2Rp = tasksByEct[iRp].getEctD();
    			if (iDp < n) 	t2Dp = tasksByLst[iDp].getLstD(n);

    			double t2 = min(t2R, t2Rp, t2Dp);
    			int j = -1;
    			Task task = null;

    			if (t2 == t2R) {
    				task = tasksByEst[iR++];
    				j = 0;
    				Alpha = task.getEst();
    			} else if (t2 == t2Rp) {
    				task = tasksByEct[iRp++];
    				j = 1;
    				Alpha = task.getEct();
    			} else if (t2 == t2Dp) {
    				task = tasksByLst[iDp++];
    				j = 2;
    				Alpha = task.getLst();
    			} else {
    				Alpha = -1;
    				assert false;
    			}
    			
    			int i = task.getId();
    			double rank = task.getEstD()+task.getLctD(n);

    			if (j == 1) { 		//K.delete(i, rank);
    				if(task.getEctD() <= task.getLstD(n))	K.delete(i, rank);
    				else			    					K.delete(i, 0);
    			}
    			else {
    				if (j == 0) 							K.insertWeighted(i, task.getH(), rank);
    				else if (j == 2 && task.getLstD(n) < task.getEctD() ) {
    					K.delete(i, rank);
    					K.insertWeighted(i, task.getH(), 0);
    				}
    				else continue;
    				int k = K.searchMth(C+1);
    				if (k != -1) {
    					Delta = tasks[k].getEst()+tasks[k].getLct()-Alpha;

    					if (Alpha < Delta && computeSlack(Alpha, Delta) < 0) {
    						return false;
    					}
    				}
    			}
    		}


    	return true;

    }
    
    public boolean check2() {  
        this.smawk = new SMAWKOpt(new SlackMatrixLight(tasks, slack));
    	return this.smawk.getNeg() >= 0;
    }
    

    private double min(double... elems) {
        assert elems.length > 1;
        double min = Double.MAX_VALUE;
        for (double elem : elems) {
            if (elem < min) min = elem;
        }
        return min;
    }
    
    private int computeSlack(int t1, int t2) //throws InconsistentException 
    {
        int s = slack.querySlack(t1, t2);

        //if (t2 > t1 && s < 0) throw new InconsistentException();
       
        return s;
     }
}


