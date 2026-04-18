/*package energetic.algorithm;

import cumulative.Instance;
import cumulative.Task;
import ilog.concert.*;
import ilog.cp.*;

public class CPCheckerAlgorithm implements ICheckerAlgorithm {
	
	public IloCP cp;
	IloIntervalVar[] tasks;
	IloCumulFunctionExpr capacity;
	
	@Override
	public void initialize(Instance positiveInstance, Instance negativeInstance) {
		 int nbTasks = positiveInstance.getTasks().length;
		 try {
			 cp = new IloCP();
			 capacity = cp.cumulFunctionExpr(); 
			 tasks = new IloIntervalVar[nbTasks];
			 for (int i = 0; i < nbTasks; i++) {
				 Task task = positiveInstance.getTasks()[i];
				 tasks[i] = cp.intervalVar(task.getP());
				 tasks[i].setEndMax(task.getLct());
				 tasks[i].setStartMin(task.getEst());
				 if(task.getH() > 0)
					 capacity = cp.sum(capacity, cp.pulse(tasks[i], task.getH()));
			 }
			 cp.add(cp.le(capacity, positiveInstance.getC()));
			 cp.setOut(null);
	     }
	     catch (IloException e) {
	            System.err.println("Error: " + e);
	    }
	}

	@Override
	public boolean isConsistent() {
		try {
			if(cp.propagate()) {
				cp.end();
				return true;
			}
			else {
				cp.end();
				return false;
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}*/
