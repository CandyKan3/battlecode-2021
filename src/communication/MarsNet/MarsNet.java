package communication.MarsNet;

import battlecode.common.*;
import communication.HeaderProtocol.HeaderProtocol;

public strictfp class MarsNet<E extends Enum<E> & IGetDataType> {
    public static RobotController rc;

    public static void setRC(RobotController rc) {
        MarsNet.rc = rc;
    }

    private final HeaderProtocol<E> hp;

    /*
    Can't use getEnumConstants()...
    public MarsNet(Class<E> messageTypeEnum) {
        this.hp = new HeaderProtocol<>(messageTypeEnum, 24);

    }
    */

    public MarsNet(E[] enumValues) {
        this.hp = new HeaderProtocol<>(enumValues, 24);
    }

    public MarsNet(byte[] data, E[] enumValues) {
        this.hp = HeaderProtocol.deSerialize(data, enumValues);
    }

    public byte[] serializedHeaderProtocol() {
        return this.hp.serialize();
    }

    public boolean broadcastRaw(E mType, int data) {
        try {
            rc.setFlag(hp.getHeader(mType) | (data & hp.getDataMask(mType)));
        } catch (GameActionException ignored) { }
        return true;
    }

    public boolean broadcastLocation(E mType, MapLocation loc) {
        if (mType.getDataType() != DataType.LOCATION)
            return false;
        return broadcastRaw(mType, DataType.LOCATION.pack(loc));
    }

    public boolean broadcastXCoord(E mType, int coord) {
        if (mType.getDataType() != DataType.XCOORD)
            return false;
        return broadcastRaw(mType, DataType.XCOORD.pack(coord));
    }

    public boolean broadcastYCoord(E mType, int coord) {
        if (mType.getDataType() != DataType.YCOORD)
            return false;
        return broadcastRaw(mType, DataType.YCOORD.pack(coord));
    }

    public boolean broadcastID(E mType, int id) {
        if (mType.getDataType() != DataType.ID)
            return false;
        return broadcastRaw(mType, DataType.ID.pack(id));
    }

    public boolean broadcast(Packet<E> p) {
        return broadcastRaw(p.mType, p.asRaw());
    }

    private Packet<E> getPacket(int botID) throws GameActionException {
        int flag = rc.getFlag(botID);
        if (flag == 0)
            return null;
        E mType = hp.getType(flag);
        return new Packet<>(mType, flag & hp.getDataMask(mType));
    }

    public Packet<E> getPacketSafe(int botID) {
        try {
            if (rc.canGetFlag(botID))
                return getPacket(botID);
        } catch (GameActionException ignore) { }
        return null;
    }

    public <T> T getAndHandle(int botID, PacketHandler<T,E> handler) throws GameActionException {
        Packet<E> p = getPacket(botID);
        return handler.handle(p);
    }

    public <T> T getAndHandleF(int botID, Filter<E> filter, PacketHandler<T,E> handler) throws GameActionException {
        Packet<E> p = getPacket(botID);
        if (filter.isAllowed(p.mType))
            return handler.handle(p);
        return null;
    }

    public <T> T getAndHandleSafe(int botID, PacketHandler<T,E> handler) {
        Packet<E> p = getPacketSafe(botID);
        if (p == null)
            return null;
        return handler.handle(p);
    }

    public <T> T getAndHandleSafeF(int botID, Filter<E> filter, PacketHandler<T,E> handler) {
        Packet<E> p = getPacketSafe(botID);
        if (filter.isAllowed(p.mType))
            return handler.handle(p);
        return null;
    }
}
