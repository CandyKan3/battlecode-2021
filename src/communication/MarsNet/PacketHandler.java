package communication.MarsNet;

public interface PacketHandler<T> {
    T handle(Packet p);
}
