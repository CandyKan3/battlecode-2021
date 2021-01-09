package communication.MarsNet;

import battlecode.common.MapLocation;

public class Packet<E extends IGetDataType> {
    public E mType;
    private int data;

    private Packet() {
        mType = null;
        this.data = 0;
    }

    public Packet(E mType, int data) {
        this.mType = mType;
        this.data = data;
    }

    public boolean isSet() {
        return mType != null;
    }

    public void setRaw(E mType, short data) {
        if (this.mType == null) {
            this.mType = mType;
            this.data = data;
        }
    }

    public int asRaw() {
        return data;
    }

    // Location casts

    public void setLocation(E mType, MapLocation loc) {
        if (this.mType == null) {
            this.mType = mType;
            this.data = DataType.LOCATION.pack(loc);
        }
    }

    public static <E extends IGetDataType> Packet<E> fromLocation(E mType, MapLocation loc) {
        Packet<E> p = new Packet<>();
        if (mType.getDataType() == DataType.LOCATION)
            p.setLocation(mType, loc);
        return p;
    }

    public MapLocation asLocation() {
        if (mType.getDataType() != DataType.LOCATION)
            return null;
        return DataType.LOCATION.unpack(data);
    }

    // ID casts

    public void setID(E mType, int id) {
        if (this.mType == null) {
            this.mType = mType;
            this.data = DataType.ID.pack(id);
        }
    }

    public static <E extends IGetDataType> Packet<E> fromID(E mType, int id) {
        Packet<E> p = new Packet<>();
        if (mType.getDataType()== DataType.ID)
            p.setID(mType, id);
        return p;
    }

    public Integer asID() {
        if (mType.getDataType() != DataType.ID)
            return null;
        return DataType.ID.unpack(data);
    }

    // XCOORD casts

    public void setXCoord(E mType, int coord) {
        if (this.mType != null) {
            this.mType = mType;
            this.data = coord;
        }
    }

    public static <E extends IGetDataType> Packet<E> fromXCoord(E mType, int coord) {
        Packet<E> p = new Packet<>();
        if (mType.getDataType()== DataType.ID)
            p.setID(mType, coord);
        return p;
    }

    public Integer asXCoord() {
        if (mType.getDataType() != DataType.XCOORD)
            return null;
        return DataType.XCOORD.unpack(data);
    }

    // YCoord casts

    public void setYCoord(E mType, int coord) {
        if (this.mType != null) {
            this.mType = mType;
            this.data = coord;
        }
    }

    public static <E extends IGetDataType> Packet<E> fromYCoord(E mType, int coord) {
        Packet<E> p = new Packet<>();
        if (mType.getDataType()== DataType.ID)
            p.setID(mType, coord);
        return p;
    }

    public Integer asYCoord() {
        if (mType.getDataType() != DataType.YCOORD)
            return null;
        return DataType.YCOORD.unpack(data);
    }
}
