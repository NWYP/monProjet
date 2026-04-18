package datastructures;

// Sorting Vector datastructure. Refers to notes for explainations.
public class SortingVector {
    private int[] a;
    private int[] b;
    private int[] array;

    public SortingVector(int n) {
        array = new int[n];
        a = new int[n];
        b = new int[n];

        for (int i = 0; i < n ; i++) {
            a[i] = b[i] = i;
        }
    }

    public void swap(int i, int j) {
        assert i < array.length;
        assert j < array.length;

        int temp = array[i];
        int beta = b[i];
        a[b[i]] = j;
        b[i] = b[j];
        array[i] = array[j];

        a[b[j]] = i;
        b[j] = beta;
        array[j] = temp;
    }

    public void setOriginal(int i, int value) {
        set(a[i], value);
    }

    public void set(int i, int value) {
        array[i] = value;
    }

    @SuppressWarnings("unchecked")
    public int get(int i) {
        return array[i];
    }

    public int getOriginal(int i) {
        return get(a[i]);
    }

    public int size() {
        return array.length;
    }

    public void sort() {
        for (int j = 1; j < size(); j++) {
            int key = get(j);
            int bKey = b[j];
            int i = j - 1;
            while (i >= 0 && key < get(i)) {
                array[i+1] = array[i];
                a[b[i]] = i+1;
                b[i+1] = b[i];
                i--;
            }
            array[i+1] = key;
            a[bKey] = i+1;
            b[i+1] = bKey;
        }
    }
}
