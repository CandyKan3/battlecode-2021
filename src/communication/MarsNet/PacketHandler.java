package communication.MarsNet;

public interface PacketHandler<T,E extends IGetDataType> {
    T handle(Packet<E> p);
}
