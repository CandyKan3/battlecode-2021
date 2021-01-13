package util;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

public class ArraySet11 {
    private int locations[] = new int[11];
    private int size = 0;

    public void add(MapLocation loc) {
        if (!contains(loc))
            locations[size++] = loc.x << 16 | loc.y;
    }

    public boolean contains(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        return locations[0] == hash || locations[1] == hash || locations[2] == hash || locations[3] == hash || locations[4] == hash || locations[5] == hash || locations[6] == hash || locations[7] == hash || locations[8] == hash || locations[9] == hash || locations[10] == hash;
    }

    public void remove(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        for (int i = 0; i < size; i++) {
            if (locations[i] == hash) {
                locations[i] = locations[size-1];
                size--;
                break;
            }
        }
    }
}
