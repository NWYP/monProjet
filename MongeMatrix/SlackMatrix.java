package MongeMatrix;

import cumulative.Instance;
import cumulative.Task;
import energetic.slack.LogarithmicSlackDatastructure;

import java.util.Arrays;

public class SlackMatrix{

	private LogarithmicSlackDatastructure slack;
	private int [] setO;
	
	
	
	public SlackMatrix(LogarithmicSlackDatastructure slack, int[] setO) {
		super();
		this.slack = slack;
		this.setO = setO;
	}
	
	public SlackMatrix(Task [] tasks, LogarithmicSlackDatastructure s) {
		slack = s;
        int n = tasks.length;
		setO = new int[n * 4];
        for (int i = 0; i < n; i++) {
            Task task = tasks[i];
            int position = i * 4;
            setO[position] = task.getEst();
            setO[position+1] = task.getLst();
            setO[position+2] = task.getEct();
            setO[position+3] = task.getLct();

        }
        Arrays.sort(setO);
        setO = removeDuplicates(setO);     
	}
	
	public SlackMatrix(Instance I, LogarithmicSlackDatastructure s) {
		this(I.getTasks(), s);
	}
	
	public SlackMatrix(Instance I) {
		this(I.getTasks(), new LogarithmicSlackDatastructure(I.getTasks(), I.getC()));
	}
	
	
	public int valueAt(int row, int col) {
		if (row < numRows() && row >= 0 && col < numColumns() && col >= 0)
			if (setO[row] <= setO[col])
				return slack.querySlack(setO[row], setO[col]);
			else 
				return -slack.getInfinity();
		return 0;
	}

	public int[] getO() {
		return setO;
	}


	public int numRows() {
		return setO.length;
	}

	public int numColumns() {
		return setO.length;
	}
	
	public void printMatrix (){
		for (int t1 : setO){
			for (int t2 : setO)
				if (t1 <= t2) 
					//System.out.print("S["+t1+","+t2+"] : "+slack.querySlack(t1, t2) + "\t\t");
					System.out.print(slack.querySlack(t1, t2) + "\t");
				else {
					//System.out.print("S["+t1+","+t2+"] : "+slack.getInfinity() + "\t\t");
					System.out.print(slack.getInfinity() + "\t");
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
