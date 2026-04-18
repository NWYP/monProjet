package MongeMatrix;

import energetic.slack.InconsistentException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;


public class SMAWKOpt2 {

	protected SlackMatrixLight matrix;
	protected int numrows;
	protected int numcols;
	Integer [] rows;
	Integer [] cols;
	//protected int   access;
	//protected int   total_op;
	//protected int   nb_sub_mat;
	protected int [] rowMinima;
	protected int [] rowMinimaId;
//	protected int [] setO1;
//	protected int [] setO2;
	protected int [] lastCols;
	protected int [] lastRows;
	
	public SMAWKOpt2(SlackMatrixLight matrix) {
	//	setO1 = matrix.getO1();
	//	setO2 = matrix.getO2();
		//access = 0;
		//total_op = 0;
		//nb_sub_mat = 0;
		
		this.matrix = matrix;
		this.numrows = matrix.numRows();
		this.numcols = matrix.numColumns();
		rows = new Integer [numrows];
		cols = new Integer [numcols];
		lastCols = new int [numrows];
		lastRows =  new int [numcols];
		for (int i = 0; i < numrows; i++) 	rows[i] =i;
		for (int i = 0; i < numcols; i++) 	cols[numcols - i-1] = i;
		
		int j = numcols-1;
		for (int i = 0; i < numrows; i++) {
			while(j > 0 && matrix.getRow(rows[i]) > matrix.getCol(cols[j])) j--;
			lastCols[i] = j;
		}
		
		j = numrows-1;
		for (int i = 0; i < numcols; i++) {
			while(j > 0 && matrix.getRow(rows[j]) > matrix.getCol(cols[i])) j--;
			lastRows[i] = j;
		}
	}
	
	private int valueAt(int row, int col) throws InconsistentException {
		int val = matrix.valueAt(row, col);
	    /*
	    if (val < 0) 	{
	    	//System.out.println("["+row+","+col+"] : inconsistent");
	    	throw new InconsistentException();
	    }
	    */
		return val;
	}
	
	private int valueAt2(int row, int col){
		int val = matrix.valueAt(row, col);
		return val;
	}
	
	
	protected Integer[] reduce(Integer[] rows, Integer[] cols) throws InconsistentException {
		Integer[] reduced_cols = null;
		if (rows.length >= cols.length) 
			return cols;
		LinkedList<Integer> A = new LinkedList<Integer> (Arrays.asList(cols));
		Stack<Integer> stack = new Stack<Integer>();
		reduced_cols = new Integer[rows.length];
		
		while(rows.length < A.size() + stack.size()) {
			if(stack.isEmpty()) {stack.push(A.getFirst());			A.remove();}
			else {
				int row = (int)rows[stack.size()-1];
				if(stack.size() < rows.length && valueAt(row, stack.peek()) <= valueAt(row, A.getFirst())) {
					stack.push(A.getFirst());
					A.remove();
				}
				else if(stack.size() == rows.length && valueAt(row, stack.peek()) <= valueAt(row, A.getFirst())) {
					A.remove();
					//access++; //--------------------------------------------------------------------------------------------------------
				}
				else if (valueAt(row, stack.peek()) > valueAt(row, A.getFirst())) {
					if (!stack.isEmpty()) stack.pop();
					//access++; //--------------------------------------------------------------------------------------------------------
				}
				
			}
			
		}
		while(A.size()>0) {
			stack.push(A.getFirst());			A.remove();
		}
		reduced_cols = stack.toArray(new Integer[0]);
		return reduced_cols;
	}
	
	public void smawk(Integer[] rows, Integer[] cols) throws InconsistentException {
		int argmin, row, min, value;
		Integer[] reduced_cols;
		/// reduce phase: make number of columns at most equal to number of rows
		reduced_cols = reduce(rows,cols);
		if (rows.length == 1) {
			rowMinima[rows[0]] = reduced_cols[0];
			return;
		}	
		/// recursive call to search for every odd row
		Integer [] reduced_rows = new Integer[((rows.length)/2)];
		int l = 0;
		for (int i = 1; i < rows.length; i +=2) 	reduced_rows[l++] = rows[i];
		smawk(reduced_rows, reduced_cols);
		/// go back and fill in the even rows		
		int start= 0, stop, target;
		for (int r = 0; r < rows.length; r += 2) {
			row = rows[r];
			if (r == rows.length - 1) 
				stop = cols.length-1; /// if r is last row, search through last col
			else {
				stop = start;             /// otherwise only until pos of min in row r+1
				target = rowMinima[rows[r+1]]; 
				while (cols[stop] != target)		stop++;
			}
			argmin = cols[start];
			min = valueAt(row, argmin);
			for (int c = start; c <= stop; c++) {
				value = valueAt(row, cols[c]);
				if(min >= value) {
					argmin = cols[c];
					min = value;
				}
				//access++; //--------------------------------------------------------------------------------------------------------
			}
			
			rowMinima[row] = argmin;
	        start = stop;
		}
	}

	public int getMin(){
		rowMinima = new int [numrows];
		try {
			//findMinMG(rows, cols);
			//System.out.println("Monge Martix of size "+rows.length+"*"+cols.length+" : "+access);//-------------------------------------------------------------------
			//access = 0;
			//total_op = 0;
			//nb_sub_mat = 0;
			int x = findMinPartialMG(rows, cols,0,numrows-1,0,numcols-1);
			//System.out.println("Partial Monge Matrix of size "+numrows+"*"+numcols+" : "+total_op+" : "+nb_sub_mat); //-------------------------------------------------------------------
			return x;
			
		} catch (InconsistentException e) {
			//System.out.println("Test");
			return -1;
		}
	}
	
	protected int findMinPartialMG(Integer[] rows, Integer[] cols, 
			int rowStart, int rowEnd, int colStart, int colEnd) throws InconsistentException {
		if(rowStart == rowEnd) 	return valueAt(rows[rowStart], cols[colStart]);
		
		int CM = lastCols[(rowStart+rowEnd) / 2];
		int RM = lastRows[CM]; 
		
		Integer[] reduced_rows_1 = Arrays.copyOfRange(rows, rowStart, RM+1);
		Integer[] reduced_cols_1 = Arrays.copyOfRange(cols, colStart, CM+1);
//		for (int t1: Arrays.copyOfRange(rows, rowStart, rowEnd)){
//			for (int t2 : Arrays.copyOfRange(cols, colStart, colEnd)) {
//				System.out.print("S["+t1+","+t2+"] "+valueAt2(t1, t2) + "\t");
//				//System.out.print(valueAt2(t1, t2) + "\t");
//			}
//			System.out.print("\n");
//		}System.out.print("\n");
//		for (int t1: reduced_rows_1){
//			for (int t2 : reduced_cols_1) {
//				System.out.print("S["+t1+","+t2+"] "+valueAt2(t1, t2) + "\t");
//				//System.out.print(valueAt2(t1, t2) + "\t");
//			}
//			System.out.print("\n");
//		}System.out.print("\n");
		int M1 = 0;
		if (reduced_rows_1.length >= 1 && reduced_cols_1.length >= 1) {
			//access = 0; //-------------------------------------------------------------------			
			M1 = findMinMG(reduced_rows_1, reduced_cols_1);
			//System.out.println("Monge Martix of size "+reduced_rows_1.length+"*"+reduced_cols_1.length+" : ");
			//System.out.println("Monge Martix of size "+reduced_rows_1.length+"*"+reduced_cols_1.length+" : "+access);//-------------------------------------------------------------------
			//total_op += access; //-------------------------------------------------------------------
			//nb_sub_mat++;		//-------------------------------------------------------------------
		}
		if (CM+1 <= colEnd)  {
			RM = lastRows[CM+1];
			if(rowStart <= RM)	{
				M1 = Math.min(M1, findMinPartialMG(rows, cols, rowStart, RM,CM+1, colEnd));
				
			}
		}
		if(RM+1 <= rowEnd) {
			CM = lastCols[RM+1];
			if(colStart <= CM) {
				M1 = Math.min(M1, findMinPartialMG(rows, cols, RM+1, rowEnd,colStart, CM));
			}
		}
		return M1;
	}

	public int findMinMG(Integer[] rows, Integer[] cols) throws InconsistentException {
		smawk(rows, cols);
		int value = valueAt(rows[0], rowMinima[rows[0]]);
		for(int row : rows) 
			value = Math.min(value, valueAt(row, rowMinima[row]));
		return value;		
	}
	
	public void printMatrix (){
		
		for (int t1: rows){
			for (int t2 : cols) {
				//System.out.print("S["+matrix.getO1()[t1]+","+matrix.getO2()[t2]+"] "+valueAt2(t1, t2) + "\t");
				System.out.print("S["+t1+","+t2+"] "+valueAt2(t1, t2) + "\t");
				//System.out.print(valueAt2(t1, t2) + "\t");
			}
			System.out.print("\n");
		}System.out.print("\n");
	}
	
	public void printRowMin (){
		for (int i = 0; i < numrows; i++){
				System.out.print(valueAt2(rows[i], rowMinima[i]) + "\t");
			System.out.print("\n");
		}
	}
	public void test1() throws InconsistentException {
		int cpt = 0;
		while(true) {
			if(cpt == 1000) throw new InconsistentException();
			cpt++;
		}
	}
	public int test2() {
		try {
			test1();
			return 5;
		} catch (InconsistentException e) {
			System.out.println("----" );
			return -1;
		}
	}
	
}
