package MongeMatrix;

import cumulative.Instance;
import cumulative.Task;
import energetic.slack.LogarithmicSlackDatastructure;

import java.util.Arrays;

public class SlackMatrixLight{

	private LogarithmicSlackDatastructure slack;

	private int [] setO1;
	private int [] setO2;
	
	
	
	public SlackMatrixLight(LogarithmicSlackDatastructure slack) {
		super();
		this.slack = slack;
	}
	
	public SlackMatrixLight(Task [] tasks, LogarithmicSlackDatastructure s) {
		slack = s;
        int n = tasks.length;
		setO1 = new int[n * 2];
		setO2 = new int[n * 2];
        for (int i = 0; i < n; i++) {
            Task task = tasks[i];
            int position = i * 2;
            setO1[position] = task.getEst();
            setO1[position+1] = task.getLst();
            setO2[position] = task.getEct();
            setO2[position+1] = task.getLct();

        }
        Arrays.sort(setO1);
        Arrays.sort(setO2);
        setO1 = removeDuplicates(setO1);     
        setO2 = removeDuplicates(setO2);  
	}
	
	public SlackMatrixLight(Instance I, LogarithmicSlackDatastructure s) {
		this(I.getTasks(), s);
	}
	
	public SlackMatrixLight(Instance I) {
		this(I.getTasks(), new LogarithmicSlackDatastructure(I.getTasks(), I.getC()));
	}
	
	
	public int valueAt(int row, int col) {
		if (row < numRows() && row >= 0 && col < numColumns() && col >= 0)
			if (setO1[row] <= setO2[col])
				return slack.querySlack(setO1[row], setO2[col]);
			else 
				return -slack.getInfinity();
		return 0;
	}
	
	public int getRow(int row) {
		if (row < numRows() && row >= 0) return setO1[row];
		return -1;
	}
	public int getCol(int col) {
		if (col < numColumns() && col >= 0) return setO2[col];
		return -1;
	}
	
	public int[] getO1() {
		return setO1;
	}
	public int[] getO2() {
		return setO2;
	}

	public int numRows() {
		return setO1.length;
	}

	public int numColumns() {
		return setO2.length;
	}
	
	public void printMatrix (){
		for (int t1 : setO1){
			for (int t2 : setO2)
				if (t1 <= t2) 
					System.out.print(slack.querySlack(t1, t2) + "\t");
				else {
					//System.out.print(slack.getInfinity() + "\t");
					System.out.print("-\t");
				}
				
			System.out.print("\n");
		}
	}

	private int[] removeDuplicates(int[] set) {
        

        int start = 0;
        while (start < set.length && set[start] <= -1) {
            start++;
        }

        if (start == set.length) {
            return new int[0];
        }
        
        int size = 1;
        for (int i = start+1; i < set.length; i++) {
            if (set[i] != set[i-1])
                size++;
        }

        int[] temp = new int[size];
        temp[0] = set[start];
        int k = 1;

        for (int i = start+1; i < set.length; i++) {
            if (k < size && set[i] != set[i-1]) {
                temp[k++] = set[i];
            }
        }

        return temp;
    }
	public static void main(String[] args) {
		System.out.print("\n");
	}
}
