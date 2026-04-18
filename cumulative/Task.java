package cumulative;

import profile.Timepoint;

public class Task extends disjunctive.ATask {
    private int h;
    private int tail;
    public Task(int id, int est, int lct, int p, int h, int tail) {
        super(id, est, lct, p);
        this.h = h;
        this.tail = tail;
    }
    public Task(int id, int est, int lct, int p, int h) {
        this(id, est, lct, p, h, 0);
    }

    public Task(int id, int est, int lct, int p) {
        this(id, est, lct, p, 1);
    }

    public Task(Task task) {
        this(task.getId()+1, task.getEst(), task.getLct(), task.getP(), task.getH(), task.getQ());
    }

    public void update(int est, int lct) {
        this.est = est;
        this.lct = lct;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }
    public int getQ() {
        return tail;
    }

    public void setQ(int q) {
        this.tail = q;
    }
    
    public int getE() {
        return getP() * h;
    }

    public boolean hasFixedPart() { return this.lct - p < this.est + p; }

    public int getEnv(int C) {
        return C * getEst() + getE();
    }

    public int getEnvc(int C, int c) {
        return (C-c) * getEst() + getE();
    }

    public int computeLeftShift(int t1, int t2) {
        return Math.max(0, getH() * (Math.min(t2, getEct()) - Math.max(t1, getEst())));
    }

    public int computeRightShift(int t1, int t2) {
        return Math.max(0, getH() * (Math.min(t2, getLct()) - Math.max(t1, getLst())));
    }

    public int computeMinimumIntersection(int t1, int t2) {
        return Math.min(computeLeftShift(t1, t2), computeRightShift(t1, t2));
    }
    


//    @Override
//    public String toString() {
//        return String.format("%d\t %d\t %d\t %d\t %d",
//                              getId(), getEst(), getLct(), getP(), h);
//    }
    @Override
    public String toString() {
        return "tasks[" + (this.getId()+1) + "] = new Task("+ (this.getId()+1) + "," + this.getEst() + "," + this.getLct() + "," + this.getP() + "," + this.getH()  + ");";
    }
    
    public String print() {
        return String.format("Id: %d, est: %d, ect: %d, lst: %d, lct: %d, p: %d, h: %d, est+lct: %d",
                             getId()+1, getEst(), getEct(), getLst(), getLct(), getP(), h, getEst() + getLct());
    }
    
    public double getEstD() {
    	return (double)this.getEst()+ (double)(getId()+1)/Integer.MAX_VALUE;
    }
    public double getEctD() {
    	return (double)this.getEct()+ ((double)(getId()+1)/Integer.MAX_VALUE);
    }
    public double getLstD(int n) {
    	return (double)this.getLst()+ ((double)(n+1)*(getId()+1)/Integer.MAX_VALUE);
    }
    public double getLctD(int n) {
    	return (double)this.getLct()+ ((double)(n+1)*(getId()+1)/Integer.MAX_VALUE);
    }
    public int F1() {
    	return this.getQ()+ this.getP();
    }
    public int F3() {
    	return this.getQ() - this.getEst();
    }
    public int F4() {
    	return this.getP()+ this.getEst();
    }
	public int getW(int cmax, int alpha, int gamma) {
		int Pp = Math.min(Math.max(0, getEst() + getP() - alpha), getP());
		int Pm = Math.min(Math.max(0, getQ() + getP() - gamma), getP());
		return getH()*Math.min(Math.min(Pp,Pm),cmax-alpha-gamma);
	}
	public boolean isInWarmingMode(int cmax, int alpha, int gamma) {
		if (alpha >= getEst() + getP() || gamma > getQ() + getP() || gamma <= getQ()) 
			return false;
		
		if (alpha <= getEst()) return true;
		if (alpha < cmax - getQ() - getP()) return gamma > alpha - getEst() + getQ(); 
		else return gamma > cmax - getEst() - getP();
	}

    public Timepoint est_to_timepoint;
    public Timepoint ect_to_timepoint;
    public Timepoint lct_to_timepoint;
}
