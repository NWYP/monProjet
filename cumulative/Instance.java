package cumulative;

public class Instance {
    private Task[] tasks;
    private int C;
    private int Cmax;

    public Instance(Task[] tasks, int c) {
        this(tasks, c, 0);
    }
    public Instance(Task[] tasks, int c, int Cmax) {
        this.tasks = tasks;
        this.C = c;
        this.Cmax = Cmax;
    }
    /*public Instance getNegativeInstance(Instance instance) {
        int maxLct = Integer.MIN_VALUE;
        for (Task task : instance.getTasks()) {
            maxLct = Math.max(maxLct, task.getLct());
        }
        Task[] neg_tasks = new Task[instance.getTasks().length];
        for (int i = 0; i < instance.getTasks().length; i++) {
            int est = maxLct - instance.getTasks()[i].getLct();
            int lct = maxLct - instance.getTasks()[i].getEst();
            int p = instance.getTasks()[i].getP();
            int h = instance.getTasks()[i].getH();
            neg_tasks[i] = new Task(i+1, est, lct, p, h);
        }
        return new Instance(neg_tasks, instance.getC());
    }*/

    public Task[] getTasks() {
        return tasks;
    }

    public int getC() {
        return C;
    }
    
    public int getCmax() {
        return Cmax;
    }
    
    public void setCmax(int Cmax) {
        this.Cmax = Cmax;
        for(Task t: tasks) {
        	t.setLct(Cmax-t.getQ());
        }
    }
    public int getUB() {
    	int UB = 0;
        for(Task t: tasks) {
        	UB += t.getEst()+t.getP()+t.getQ();
        }
        return UB;
    }
    public int getLB0() {
    	int LB = 0;
        for(Task t: tasks) {
        	LB = Math.max(LB, t.getEst()+t.getP()+t.getQ());
        }
        return LB;
    }
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	builder.append(String.format("%d\t %d\t %d\n", tasks.length, C, Cmax));
    	for(Task t : tasks) {
    		builder.append(t.toString()+"\n");
    	}
    	return builder.toString();
    }
}
