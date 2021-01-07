package util;


public class PriorityElement<T> implements Comparable<PriorityElement<T>> {
    private final Integer priority;
    private final T element;

    public PriorityElement(T element, Integer priority) {
        this.priority = priority;
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    @Override
    public int compareTo(PriorityElement<T> other) {
        return this.priority.compareTo(other.priority);
    }
}

