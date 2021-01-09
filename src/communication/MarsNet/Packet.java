package communication.MarsNet;

import battlecode.common.MapLocation;

import javax.xml.crypto.Data;

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
}
