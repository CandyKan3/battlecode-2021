package communication.MarsNet;

import battlecode.common.MapLocation;

public class Packet {
    public MessageType mType;
    private short data;

    public Packet() {
        this.mType = MessageType.NONE;
        this.data = 0;
    }

    public Packet(MessageType mType, short data) {
        this.mType = mType;
        this.data = data;
    }

    public Packet(MessageType mType, int data) {
        this.mType = mType;
        this.data = (short) data;
    }

    public boolean isSet() {
        return mType != MessageType.NONE;
    }

    public void setRaw(MessageType mType, short data) {
        if (this.mType == MessageType.NONE) {
            this.mType = mType;
            this.data = data;
        }
    }

    public short asRaw() {
        return data;
    }

    // Location casts
    protected static short packLocation(MapLocation loc) {
        return (short) ((loc.x & 0xFF) << 8 | loc.y & 0xFF);
    }

    public void setLocation(MessageType mType, MapLocation loc) {
        if (this.mType == MessageType.NONE) {
            this.mType = mType;
            this.data = packLocation(loc);
        }
    }

    public static Packet fromLocation(MessageType mType, MapLocation loc) {
        Packet p = new Packet();
        if (mType.dType == DataType.LOCATION)
            p.setLocation(mType, loc);
        return p;
    }

    protected static MapLocation unpackLocation(short loc) {
        MapLocation ret = MarsNet.rc.getLocation();
        int x = loc >> 8 | ret.x & 0xFFFF00;
        int y = loc & 0xFF | ret.y & 0xFFFF00;
        return new MapLocation(x,y);
    }

    public MapLocation asLocation() {
        if (mType.dType != DataType.LOCATION)
            return null;
        return unpackLocation(data);
    }

    // ID casts

    protected static short packID(int id) {
        return (short) id;
    }

    public void setID(MessageType mType, int id) {
        if (this.mType == MessageType.NONE) {
            this.mType = mType;
            this.data = packID(id);
        }
    }

    public static Packet fromID(MessageType mType, int id) {
        Packet p = new Packet();
        if (mType.dType == DataType.ID)
            p.setID(mType, id);
        return p;
    }

    protected static int unpackID(short id) {
        return (MarsNet.rc.getID() & 0xFFFF0000) | id;
    }

    public int asID() {
        if (mType.dType != DataType.ID)
            return 0;
        return unpackID(data);
    }
}
