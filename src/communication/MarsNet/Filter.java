package communication.MarsNet;

public interface Filter<E> {
    boolean isAllowed(E item);
}
