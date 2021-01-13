package util;

import battlecode.common.MapLocation;

public class HashSet11 {
    private final int[] indexes = new int[14];
    private final int[] location = new int[11];

    public HashSet11() {
        for (int i = 0; i < 14; i++) {
            indexes[i] = 0;
        }
    }

    public void add(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % 13;
        int placeIdx = indexes[mod+1];
        for (int i = indexes[mod]; i < placeIdx; i++) {
            if (location[i] == hash)
                return;
        }
        for (int i = mod + 1; i < 14; i++)
            indexes[i]++;
        for (int i = 10; i > placeIdx; i--)
            location[i] = location[i-1];
        location[placeIdx] = hash;
    }

    public boolean contains(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % 13;
        int end = indexes[mod+1];
        for (int i = indexes[mod]; i < end; i++) {
            if (location[i] == hash)
                return true;
        }
        return false;
    }

    public void remove(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % 13;
        int end = indexes[mod+1];
        findAndRemove: {
            for (int i = indexes[mod]; i < end; i++) {
                if (location[i] == hash) {
                    location[i] = location[end - 1];
                    break findAndRemove;
                }
            }
            // Location not in structure, return;
            return;
        }
        for (int i = mod+1; i < 14; i++)
            indexes[i]--;
        for (int i = end; i < 11; i++)
            location[i-1] = location[i];
    }
}
