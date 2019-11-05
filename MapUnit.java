import java.util.*;

class MapUnit {

    int version;
    String name;
    int dX, dZ;
    int type;
    int flag0, flag1;
    int numDoors;
    ArrayList<Door> doors;
    ArrayList<SpawnPoint> spawnPoints;
    ArrayList<WayPoint> wayPoints;
    float[][] waterBoxes;
    int rotation = 0;

    int offsetX, offsetZ;
    int enemyScore = -1;
    int unitScore = -1;
    ArrayList<Integer> unitScoreByPhase = new ArrayList<Integer>();
    ArrayList<Integer> enemyScoreByPhase = new ArrayList<Integer>();

    MapUnit copy() {
        MapUnit m = new MapUnit();
        m.version = version;
        m.name = name;
        m.dX = dX;
        m.dZ = dZ;
        m.type = type;
        m.flag0 = flag0;
        m.flag1 = flag1;
        m.numDoors = numDoors;
        m.rotation = rotation;
        m.doors = new ArrayList<Door>();
        for (Door d: doors) 
            m.doors.add(d.copy());
        m.spawnPoints = new ArrayList<SpawnPoint>();
        for (SpawnPoint sp: spawnPoints)
            m.spawnPoints.add(sp.copy());
        m.wayPoints = new ArrayList<WayPoint>();
        for (WayPoint wp: wayPoints)
            m.wayPoints.add(wp.copy());
        for (Door d: m.doors)
            d.mapUnit = m;
        for (SpawnPoint sp: m.spawnPoints) 
            sp.mapUnit = m;
        for (WayPoint wp: m.wayPoints)
            wp.mapUnit = m;
        m.waterBoxes = new float[waterBoxes.length][6];
        for (int i = 0; i < waterBoxes.length; i++)
            for (int j = 0; j < 6; j++)
                m.waterBoxes[i][j] = waterBoxes[i][j];
        return m;
    }

    MapUnit rotate(int r) {
        MapUnit m = copy();
        m.rotation = (rotation + r) % 4;
        if (r == 1 || r == 3) {
            m.dX = dZ;
            m.dZ = dX;
        }
        for (Door d: m.doors) {
            switch(d.dirSide) {
            case 0:
            case 2:
                if (r == 2 || r == 3)
                    d.offsetSide = dX - 1 - d.offsetSide;
                break;
            case 1: 
            case 3:
                if (r == 1 || r == 2)
                    d.offsetSide = dZ - 1 - d.offsetSide;
            }
            d.dirSide = (d.dirSide + r) % 4;
        }
        return m;
    }

    void recomputePos() {
        for (Door d: doors) {
            doorOffset(d);
            doorPos(d);
            d.spawnPoint.spawnPointPos();
        }
        for (SpawnPoint sp: spawnPoints) {
            spawnPointPos(sp);
        }
        for (WayPoint wp: wayPoints) {
            wayPointPos(wp);
        }
    }

    void doorOffset(Door d) {
        int x = offsetX, z = offsetZ;

        switch(d.dirSide) {
        case 0: x += d.offsetSide; break;
        case 1: x += dX; z += d.offsetSide; break;
        case 2: x += d.offsetSide; z += dZ; break;
        case 3: z += d.offsetSide; break;
        }

        d.offsetX = x;
        d.offsetZ = z;
        //return new int[] {x, z}; 
    }

    void doorPos(Door d) {
        doorOffset(d);
        float x = d.offsetX, z = d.offsetZ;
       
        switch (d.dirSide) {
        case 0: case 2: x += 0.5; break;
        case 1: case 3: z += 0.5; break;
        }

        d.posX = 170.0f * x;
        d.posZ = 170.0f * z;
        d.posY = 0.0f;

        float ang = (float)((-Math.PI/2 * d.dirSide + 2*Math.PI) % (2*Math.PI));
        d.ang = ang;
    }

    void spawnPointPos(SpawnPoint sp) {
        float x = (offsetX + dX/2.0f) * 170.0f;
        float z = (offsetZ + dZ/2.0f) * 170.0f;
        switch(rotation) {
        case 0: x += sp.x; z += sp.z; break;
        case 1: x -= sp.z; z += sp.x; break;
        case 2: x -= sp.x; z -= sp.z; break;
        case 3: x += sp.z; z -= sp.x; break;
        }
        sp.posX = x;
        sp.posZ = z;
        sp.posY = sp.y;

        float ang = (float)((Math.PI/2 * (-rotation + sp.angle / 90.0) + Math.PI * 2) % (Math.PI*2));
        sp.ang = ang;
    }

    void wayPointPos(WayPoint wp) {
        float x = (offsetX + dX/2.0f) * 170.0f;
        float z = (offsetZ + dZ/2.0f) * 170.0f;
        switch(rotation) {
        case 0: x += wp.x; z += wp.z; break;
        case 1: x -= wp.z; z += wp.x; break;
        case 2: x -= wp.x; z -= wp.z; break;
        case 3: x += wp.z; z -= wp.x; break;
        }
        wp.posX = x;
        wp.posZ = z;
        wp.posY = wp.y;
    }

    float[][] waterBoxPos() {
        float[][] ret = new float[waterBoxes.length][6];
        float x = (offsetX + dX/2.0f) * 170.0f;
        float z = (offsetZ + dZ/2.0f) * 170.0f;
        for (int i = 0; i < ret.length; i++) {
            ret[i][1] = waterBoxes[i][1];
            ret[i][4] = waterBoxes[i][4];
            switch(rotation) {
            case 0:
                ret[i][0] = x + waterBoxes[i][0];
                ret[i][2] = z + waterBoxes[i][2];
                ret[i][3] = x + waterBoxes[i][3];
                ret[i][5] = z + waterBoxes[i][5];
                break;
            case 1:
                ret[i][0] = x - waterBoxes[i][2];
                ret[i][2] = z + waterBoxes[i][0];
                ret[i][3] = x - waterBoxes[i][5];
                ret[i][5] = z + waterBoxes[i][3];
                break;
            case 2:
                ret[i][0] = x - waterBoxes[i][0];
                ret[i][2] = z - waterBoxes[i][2];
                ret[i][3] = x - waterBoxes[i][3];
                ret[i][5] = z - waterBoxes[i][5];
                break;
            case 3:
                ret[i][0] = x + waterBoxes[i][2];
                ret[i][2] = z - waterBoxes[i][0];
                ret[i][3] = x + waterBoxes[i][5];
                ret[i][5] = z - waterBoxes[i][3];
                break;
            }
            if (ret[i][0] > ret[i][3]) {
                float temp = ret[i][0];
                ret[i][0] = ret[i][3];
                ret[i][3] = temp;
            }
            if (ret[i][2] > ret[i][5]) {
                float temp = ret[i][2];
                ret[i][2] = ret[i][5];
                ret[i][5] = temp;
            }
        }
        return ret;
    }

    
}
