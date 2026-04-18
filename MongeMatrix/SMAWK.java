package MongeMatrix;

import energetic.slack.InconsistentException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;


public class SMAWK {

	protected SlackMatrix matrix;
	protected int numrows;
	protected int numcols;
	Integer [] rows;
	Integer [] cols;
	
	protected int [] rowMinima;
	protected int [] rowMinimaId;
	
	public SMAWK(SlackMatrix matrix) {
		this.matrix = matrix;
		this.numrows = matrix.numRows();
		this.numcols = matrix.numColumns();
		rows = new Integer [numrows];
		cols = new Integer [numcols];
		
		for (int i = 0; i < numrows; i++) {
			rows[i] =i;
		}

		for (int i = 0; i < numcols; i++) {
			cols[numcols - i-1] = i;
		}
	}
	

	private int valueAt2(int row, int col){
		int val = matrix.valueAt(row, col);
		return val;
	}
	
	public int getNeg() {
		rowMinima = new int [numrows];
		try {
			return getNegativePartialMG(rows, cols,0,numrows-1,0,numcols-1);
		} catch (InconsistentException e) {
			// TODO Auto-generated catch block
			return -1;
		}
	}
	public int findNegMG(Integer[] rows, Integer[] cols) throws InconsistentException {
		return smawkNeg(rows, cols);
	}
	
	protected int getNegativePartialMG(Integer[] rows, Integer[] cols, int rowStart, int rowEnd, int colStart, int colEnd) throws InconsistentException {
		if(rowStart == rowEnd) 	return valueAt(rows[rowStart], cols[colStart]);
		
		int rowMid = (rowStart+rowEnd) / 2; 
		int colMid = (colStart+colEnd) / 2;

		Integer[] reduced_rows_1 = Arrays.copyOfRange(rows, rowStart, rowMid+1);
		Integer[] reduced_cols_1 = Arrays.copyOfRange(cols, colStart, colMid+1);
			
		int M1 = 0;
		if (reduced_rows_1.length >= 1 && reduced_cols_1.length >= 1) {
			M1 = findNegMG(reduced_rows_1, reduced_cols_1);
			if(M1 < 0) return M1;
		}
		if (reduced_rows_1.length >= 1 && (colEnd-colMid)/2 >= 1)  {
			if (reduced_rows_1.length > (colEnd-colMid)/2) 
				M1 = Math.min(M1, getNegativePartialMG(rows, cols,rowStart, rowMid-1,colMid+1, colEnd));
			else
				M1 = Math.min(M1, getNegativePartialMG(rows, cols,rowStart, rowMid,colMid+1, colEnd));
			if (M1 < 0) return M1;
		}
		if ((rowEnd-rowMid)/2 >= 1 && reduced_cols_1.length >= 1) {
			if (reduced_cols_1.length > (rowEnd-rowMid)/2) 
				M1 = Math.min(M1, getNegativePartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid-1));
			else
				M1 = Math.min(M1, getNegativePartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid));
		}
		return M1;
	}
	
	public int smawkNeg(Integer[] rows, Integer[] cols) throws InconsistentException {
		int argmin, row, min, value;
		Integer[] reduced_cols;
		/// reduce phase: make number of columns at most equal to number of rows
		reduced_cols = reduce(rows,cols);
		if (rows.length == 1) {
			rowMinima[rows[0]] = reduced_cols[0];
			return rowMinima[rows[0]];
		}	
		/// recursive call to search for every odd row
		Integer [] reduced_rows = new Integer[((rows.length)/2)];
		int l = 0;
		for (int i = 1; i < rows.length; i +=2) 	
			reduced_rows[l++] = rows[i];
		int val = smawkNeg(reduced_rows, reduced_cols);
		if (val < 0) return val;
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
			min = matrix.valueAt(row, argmin);
			for (int c = start; c <= stop; c++) {
				if (min < 0) return min; 
				value = valueAt(row, cols[c]);
				if(min >= value) {
					argmin = cols[c];
					min = value;
				}
			}
			rowMinima[row] = argmin;
	        start = stop;
		}
		return 0;
	}
	
	
	
	
	public int getMin() {
		rowMinima = new int [numrows];
		try {
			return findMinPartialMG(rows, cols,0,numrows-1,0,numcols-1);
		} catch (InconsistentException e) {
			// TODO Auto-generated catch block
			return -1;
		}
	}
	
	protected int findMinPartialMG(Integer[] rows, Integer[] cols, int rowStart, int rowEnd, int colStart, int colEnd) throws InconsistentException {
		if(rowStart == rowEnd) 	return valueAt(rows[rowStart], cols[colStart]);
		
		int rowMid = (rowStart+rowEnd) / 2; 
		int colMid = (colStart+colEnd) / 2;

		Integer[] reduced_rows_1 = Arrays.copyOfRange(rows, rowStart, rowMid+1);
		Integer[] reduced_cols_1 = Arrays.copyOfRange(cols, colStart, colMid+1);
			
		int M1 = 0;
		if (reduced_rows_1.length >= 1 && reduced_cols_1.length >= 1) 
			M1 = Math.min(M1, findMinMG(reduced_rows_1, reduced_cols_1));
		if (reduced_rows_1.length >= 1 && (colEnd-colMid)/2 >= 1)  {
			if (reduced_rows_1.length > (colEnd-colMid)/2) 
				M1 = Math.min(M1, findMinPartialMG(rows, cols,rowStart, rowMid-1,colMid+1, colEnd));
			else
				M1 = Math.min(M1, findMinPartialMG(rows, cols,rowStart, rowMid,colMid+1, colEnd));
		}
		if ((rowEnd-rowMid)/2 >= 1 && reduced_cols_1.length >= 1) {
			if (reduced_cols_1.length > (rowEnd-rowMid)/2) 
				M1 = Math.min(M1, findMinPartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid-1));
			else
				M1 = Math.min(M1, findMinPartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid));
		}
		return M1;
	}
	
	
	

	
	public int findMinMG(Integer[] rows, Integer[] cols) {
		try {
			smawk(rows, cols);
			int value = valueAt(rows[0], rowMinima[rows[0]]);
			for(int row : rows) 
				value = Math.min(value, valueAt(row, rowMinima[row]));
			return value;
		} catch (InconsistentException e) {
			return -1;
		}
		
	}
	
	protected Integer[] reduce(Integer[] rows, Integer[] cols) throws InconsistentException{
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
				}
				else if (valueAt(row, stack.peek()) > valueAt(row, A.getFirst())) {
					if (!stack.isEmpty()) stack.pop();
				}
			}
		}
		while(A.size()>0) {
			stack.push(A.getFirst());			A.remove();
		}
		reduced_cols = stack.toArray(new Integer[0]);
		return reduced_cols;
	}
	
	public void smawk(Integer[] rows, Integer[] cols, Integer[] rowMinima) throws InconsistentException {
		int argmin, row, min, value;
		Integer[] reduced_cols;
		
		/// reduce phase: make number of columns at most equal to number of rows
		reduced_cols = reduce(rows,cols);
		
		if (rows.length == 1) {
			rowMinima[rows[0]] = reduced_cols[0];	return;
		}
				
		/// recursive call to search for every odd row
		Integer [] reduced_rows = new Integer[((rows.length)/2)];
		int l = 0;
		for (int i = 1; i < rows.length; i +=2) 	reduced_rows[l++] = rows[i];
		
		smawk(reduced_rows, reduced_cols, rowMinima);

		
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
			}
			
			rowMinima[row] = argmin;
	        start = stop;
		}

	}
	
	public void smawk(Integer[] rows, Integer[] cols) throws InconsistentException  {
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
			}
			
			rowMinima[row] = argmin;
	        start = stop;
		}
	}

	///-------------------------------------------------------
	private int valueAt(int row, int col) throws InconsistentException {
		int val = matrix.valueAt(row, col);
	    if (val < 0) {
	           throw new InconsistentException();
	    }
		return val;
	}
	
	public int negExist() {
		rowMinima = new int [numrows];
		try {
			return existNegPartialMG(rows, cols,0,numrows-1,0,numcols-1);
		} catch (InconsistentException e) {
			return -1;
		}
	}
	public int existNegMG(Integer[] rows, Integer[] cols) throws InconsistentException {
		return smawk2(rows, cols);
	}
	
	protected int existNegPartialMG(Integer[] rows, Integer[] cols, int rowStart, int rowEnd, int colStart, int colEnd) 
			throws InconsistentException {
		if(rowStart == rowEnd) 	return valueAt2(rows[rowStart], cols[colStart]);
		
		int rowMid = (rowStart+rowEnd) / 2; 
		int colMid = (colStart+colEnd) / 2;

		Integer[] reduced_rows_1 = Arrays.copyOfRange(rows, rowStart, rowMid+1);
		Integer[] reduced_cols_1 = Arrays.copyOfRange(cols, colStart, colMid+1);
			
		int M1 = 0;
		if (reduced_rows_1.length >= 1 && reduced_cols_1.length >= 1) {
			M1 = existNegMG(reduced_rows_1, reduced_cols_1);
			
		}
		if (reduced_rows_1.length >= 1 && (colEnd-colMid)/2 >= 1)  {
			if (reduced_rows_1.length > (colEnd-colMid)/2) 
				M1 = Math.min(M1, existNegPartialMG(rows, cols,rowStart, rowMid-1,colMid+1, colEnd));
			else
				M1 = Math.min(M1, existNegPartialMG(rows, cols,rowStart, rowMid,colMid+1, colEnd));
		}
		if ((rowEnd-rowMid)/2 >= 1 && reduced_cols_1.length >= 1) {
			if (reduced_cols_1.length > (rowEnd-rowMid)/2) 
				M1 = Math.min(M1, existNegPartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid-1));
			else
				M1 = Math.min(M1, existNegPartialMG(rows, cols, rowMid+1, rowEnd,colStart, colMid));
		}
		return 0;
	}
	public int smawk2(Integer[] rows, Integer[] cols) throws InconsistentException  {
		int argmin, row, min, value;
		Integer[] reduced_cols;
		/// reduce phase: make number of columns at most equal to number of rows
		reduced_cols = reduce(rows,cols);
		if (rows.length == 1) {
			rowMinima[rows[0]] = reduced_cols[0];
			int l = valueAt2(rows[0], rowMinima[rows[0]]);
			return l;
		}	
		/// recursive call to search for every odd row
		Integer [] reduced_rows = new Integer[((rows.length)/2)];
		int l = 0;
		for (int i = 1; i < rows.length; i +=2) 	reduced_rows[l++] = rows[i];
		smawk2(reduced_rows, reduced_cols);
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
	
			min = valueAt2(row, argmin);
			
			for (int c = start; c <= stop; c++) {
				value = valueAt2(row, cols[c]);
				if(min >= value) {
					argmin = cols[c];
					min = value;
				}
			}
			rowMinima[row] = argmin;
	        start = stop;
		}
		return 0;
	}
    
	public void printMatrix (){
		for (int t1: rows){
			for (int t2 : cols)
				System.out.print(valueAt2(t1, t2) + "\t");
			System.out.print("\n");
		}
	}
	
	public void printRowMin (){
		for (int i = 0; i < numrows; i++){
				System.out.print(valueAt2(rows[i], rowMinima[i]) + "\t");
			System.out.print("\n");
		}
	}
}
