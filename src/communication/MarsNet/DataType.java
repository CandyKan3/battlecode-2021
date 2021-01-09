package communication.MarsNet;

import battlecode.common.MapLocation;
import communication.HeaderProtocol.IGetNumBits;

public abstract class DataType implements IGetNumBits {
    public final int numBits;

    abstract static class DataTypeImpl<E> extends DataType {
        private DataTypeImpl(int numBits) {
            super(numBits);
        }
        public abstract int pack(E data);
        public abstract E unpack(int data);
    }
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final DataTypeImpl<Void> RAW = new DataTypeImpl<Void>(0) {
        @Override
        public int pack(Void data) {
            return 0;
        }

        @Override
        public Void unpack(int data) {
            return null;
        }
    };

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final DataTypeImpl<MapLocation> LOCATION = new DataTypeImpl<MapLocation>(16) {
        @Override
        public int pack(MapLocation loc) {
            return (loc.x & 0xFF) << 8 | loc.y & 0xFF;
        }

        @Override
        public MapLocation unpack(int loc) {
            MapLocation ret = MarsNet.rc.getLocation();
            int x = loc >> 8 | ret.x & 0xFFFF00;
            int y = loc & 0xFF | ret.y & 0xFFFF00;
            return new MapLocation(x,y);
        }
    };

    // May need to change number of bits here...
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final DataTypeImpl<Integer> ID = new DataTypeImpl<Integer>(20) {
        @Override
        public int pack(Integer id) {
            return id & 0xFFFFF;
        }

        @Override
        public Integer unpack(int id) {
            return (MarsNet.rc.getID() & 0xFFF00000) | id;
        }
    };

    private DataType(int numBits) {
        this.numBits = numBits;
    }

    @Override
    public int getNumBits() {
        return numBits;
    }
}
