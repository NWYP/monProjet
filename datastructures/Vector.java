package datastructures;

public class Vector {
    protected int[] array;
    protected int size;

    public Vector(int capacity) {
        array = new int[capacity];
        size = capacity;
    }

    public void resize(int newSize) {
        assert newSize <= array.length;
        size = newSize;
    }

    public int get(int i) {
        assert i < size;
        return array[i];
    }

    public void set(int i, int value) {
        assert i < size;
        array[i] = value;
    }

    public int size() {
        return size;
    }
}
