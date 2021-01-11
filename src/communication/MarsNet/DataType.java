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
    public static final DataTypeImpl<Void> NONE = new DataTypeImpl<Void>(0) {
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
            return ((loc.x & 0xFF) << 8) | (loc.y & 0xFF);
        }

        @Override
        public MapLocation unpack(int loc) {
            MapLocation me = MarsNet.rc.getLocation();
            int loc_x_lo = loc >>> 8;
            int loc_y_lo = loc & 0xFF;
            int dx = (me.x & 0xFF) - loc_x_lo;
            int dy = (me.y & 0xFF) - loc_y_lo;
            if (dx <= -128) {
                loc_x_lo -= 256;
            } else if (dx >= 128) {
                loc_x_lo += 256;
            }
            if (dy <= -128) {
                loc_y_lo -= 256;
            } else if (dy >= 128) {
                loc_y_lo += 256;
            }
            int x = loc_x_lo + (me.x & 0xFFFF00);
            int y = loc_y_lo + (me.y & 0xFFFF00);
            return new MapLocation(x,y);
        }
    };

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final DataTypeImpl<Integer> XCOORD = new DataTypeImpl<Integer>(8) {
        @Override
        public int pack(Integer coord) {
            return coord & 0xFF;
        }

        @Override
        public Integer unpack(int coord) {
            MapLocation me = MarsNet.rc.getLocation();
            int coord_lo = coord & 0xFF;
            int d = (me.x & 0xFF) - coord_lo;
            if (d <= -128) {
                coord_lo -= 256;
            } else if (d >= 128) {
                coord_lo += 256;
            }
            return coord_lo + (me.x & 0xFFFF00);
        }
    };

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final DataTypeImpl<Integer> YCOORD = new DataTypeImpl<Integer>(8) {
        @Override
        public int pack(Integer coord) {
            return coord & 0xFF;
        }

        @Override
        public Integer unpack(int coord) {
            MapLocation me = MarsNet.rc.getLocation();
            int coord_lo = coord & 0xFF;
            int d = (me.y & 0xFF) - coord_lo;
            if (d <= -128) {
                coord_lo -= 256;
            } else if (d >= 128) {
                coord_lo += 256;
            }
            return coord_lo + (me.y & 0xFFFF00);
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
