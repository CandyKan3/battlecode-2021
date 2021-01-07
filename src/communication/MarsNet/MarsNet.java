package communication.MarsNet;

import battlecode.common.*;

public strictfp class MarsNet {
    static RobotController rc;

    public static void setRC(RobotController rc) {
        MarsNet.rc = rc;
    }

    public static boolean broadcastRaw(MessageType mType, short data) {
        try {
            rc.setFlag(mType.getHeader(rc.getType()) | data);
        } catch (GameActionException ignored) { }
        return true;
    }

    public static boolean broadcastLocation(MessageType mType, MapLocation loc) {
        if (mType.dType != DataType.LOCATION)
            return false;
        return broadcastRaw(mType, Packet.packLocation(loc));
    }

    public static boolean broadcastID(MessageType mType, int id) {
        if (mType.dType != DataType.ID)
            return false;
        return broadcastRaw(mType, Packet.packID(id));
    }

    public static boolean broadcast(Packet p) {
        return broadcastRaw(p.mType, p.asRaw());
    }

    private static Packet getPacket(int botID, ComType srcCType) throws GameActionException {
        int flag = rc.getFlag(botID);
        MessageType mType = MessageType.values()[flag >> 16 & 0x7F];
        makePacket: {
            if (mType == MessageType.NONE)
                break makePacket;
            switch (mType.dst) {
                case NONE:
                    break makePacket;
                case EC:
                    if (rc.getType() != RobotType.ENLIGHTENMENT_CENTER)
                        break makePacket;
                    break;
                case BOT:
                    if (rc.getType() == RobotType.ENLIGHTENMENT_CENTER)
                        break makePacket;
                    break;
                case OTHER:
                    RobotType dstRType = rc.getType();
                    if (dstRType == srcCType.getRType(dstRType))
                        break makePacket;
                    break;
                case ALL:
                    break;
            }
            return new Packet(mType, (short) flag);
        }
        return null;
    }

    public static Packet getPacketSafe(int botID, ComType srcCType) {
        try {
            if (rc.canGetFlag(botID))
                return getPacket(botID, srcCType);
        } catch (GameActionException ignore) { }
        return null;
    }

    public static <T> T getAndHandle(int botID, ComType srcCType, PacketHandler<T> handler) throws GameActionException {
        Packet p = getPacket(botID, srcCType);
        if (p == null || p.mType == MessageType.NONE)
            return null;
        return handler.handle(p);
    }

    public static <T> T getAndHandleSafe(int botID, ComType srcCType, PacketHandler<T> handler) {
        Packet p = getPacketSafe(botID, srcCType);
        if (p == null || p.mType == MessageType.NONE)
            return null;
        return handler.handle(p);
    }
}
