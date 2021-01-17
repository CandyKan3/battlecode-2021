package util;

public class CircQueue<T> {
    private int N;
    private T[] values;
    private int start = 0;
    private int end = 0;

    public CircQueue(int N) {
        this.N = N;
        values = (T[]) new Object[N];
    }

    public void push(T elem) {
        values[end] = elem;
        end = (end + 1) % N;
    }

    public T pop() {
        if (start == end)
            return null;
        T ret = values[start];
        start = (start + 1) % N;
        return ret;
    }

    public T peek() {
        return values[start];
    }
}
