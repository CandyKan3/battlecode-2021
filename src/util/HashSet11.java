package util;

import battlecode.common.MapLocation;

public class HashSet11 {
    private static final int N = 13;
    private final int[] indexes = new int[N+1];
    private final int[] location = new int[11];

    public boolean add(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % N;
        int placeIdx = indexes[mod+1];
        for (int i = indexes[mod]; i < placeIdx; i++) {
            if (location[i] == hash)
                return false;
        }
        for (int i = mod + 1; i < N+1; i++)
            indexes[i]++;
        for (int i = 10; i > placeIdx; i--)
            location[i] = location[i-1];
        location[placeIdx] = hash;
        return true;
    }

    public boolean contains(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % N;
        int end = indexes[mod+1];
        for (int i = indexes[mod]; i < end; i++) {
            if (location[i] == hash)
                return true;
        }
        return false;
    }

    public boolean remove(MapLocation loc) {
        int hash = loc.x << 16 | loc.y;
        int mod = hash % N;
        int end = indexes[mod+1];
        findAndRemove: {
            for (int i = indexes[mod]; i < end; i++) {
                if (location[i] == hash) {
                    location[i] = location[end - 1];
                    break findAndRemove;
                }
            }
            // Location not in structure, return;
            return false;
        }
        for (int i = mod+1; i < N+1; i++)
            indexes[i]--;
        for (int i = end; i < 11; i++)
            location[i-1] = location[i];
        location[10] = 0;
        return true;
    }

    public MapLocation getClosest(MapLocation to) {
        if (location[0] == 0)
            return null;
        int lochash = location[0];
        MapLocation closest = new MapLocation(lochash >>> 16, lochash & 0xFFFF);
        int minDist = to.distanceSquaredTo(closest);
        int dist;
        MapLocation other;
        int i = 1;
        lochash = location[i];
        while (lochash != 0) {
            other = new MapLocation(lochash >>> 16, lochash & 0xFFFF);
            dist = to.distanceSquaredTo(other);
            if (dist < minDist) {
                minDist = dist;
                closest = other;
            }
            if (i == 10)
                break;
            i++;
            lochash = location[i];
        }
        return closest;
    }
}
