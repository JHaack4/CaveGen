class Teki {

    String tekiName;
    String itemInside = null;
    int fallType;
    int min;
    int weight;
    int type;
    int capType;

    MapUnit mapUnit = null;
    SpawnPoint spawnPoint = null;
    float posX, posY, posZ, ang;

    int numToSpawn = 1; // used only as a return value for getRandCapTeki

    Teki spawn(MapUnit m, SpawnPoint sp) {
        Teki t = new Teki();
        t.tekiName = tekiName;
        t.itemInside = itemInside;
        t.fallType = fallType;
        t.min = min;
        t.weight = weight;
        t.type = type;
        t.capType = type;
        t.mapUnit = m;
        t.spawnPoint = sp;
        return t;
    }

}
