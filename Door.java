import java.util.*;

class Door {
    int dirSide;
    int offsetSide;
    int score;
    int idx;
    int numDoorLinks;
    int wpIdx;
    ArrayList<DoorLink> doorLinks = new ArrayList<DoorLink>();
    MapUnit mapUnit = null;

    Door adjacentDoor = null;
    boolean markedAsCap = false;
    int doorScore = -1;
    int gateScore = -1;
    SpawnPoint spawnPoint = null;
    ArrayList<Integer> doorScoreByPhase = new ArrayList<Integer>();
    int offsetX, offsetZ;
    float posX, posY, posZ, ang; // global position in the sublevel

    Door copy() {
        Door d = new Door();
        d.dirSide = dirSide;
        d.offsetSide = offsetSide;
        d.score = score;
        d.idx = idx;
        d.numDoorLinks = numDoorLinks;
        d.wpIdx = wpIdx;
        for (DoorLink l: doorLinks) {
            d.doorLinks.add(l.copy());
        }
        return d;
    }

    void doorOffset() {
        mapUnit.doorOffset(this);
    }

    void doorPos() {
        mapUnit.doorPos(this);
    }

    static boolean isDoorClosed(Door d1) {
        return d1.adjacentDoor != null;
    }

    static boolean isDoorOpen(Door d1) {
        return d1.adjacentDoor == null;
    }

    static boolean doorDirsMatch(Door d1, Door d2) {
        return Math.abs(d1.dirSide - d2.dirSide) == 2;
    }

}

