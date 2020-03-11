class SpawnPoint {
    int type;
    int spawnPointIdx; // order it appears in the file
    float x, y, z; // these are relative to the map unit (in the file)
    float angle, radius;
    int minNum, maxNum;
    MapUnit mapUnit = null;
    Door door = null;

    boolean filled = false;
    boolean filledFalling = false;
    boolean filledFallingPom = false;
    int scoreHole = -1;
    int scoreItem = -1;
    float probVisuallyEmpty = 1.0f;
    float posX, posZ, posY, ang; // these are "global positions" relative to the sublevel

    int spawnListIdx = -1;

    SpawnPoint copy() {
        SpawnPoint sp = new SpawnPoint();
        sp.spawnPointIdx = spawnPointIdx;
        sp.type = type;
        sp.x = x;
        sp.y = y;
        sp.z = z;
        sp.angle = angle;
        sp.radius = radius;
        sp.minNum = minNum;
        sp.maxNum = maxNum;
        sp.spawnListIdx = spawnListIdx;
        return sp;
    }

    void spawnPointPos() {
        if (door != null) {
            door.mapUnit.doorPos(door);
            posX = door.posX;
            posY = door.posY;
            posZ = door.posZ;
            ang = door.ang;
        }
        else 
            mapUnit.spawnPointPos(this);
    }
}
