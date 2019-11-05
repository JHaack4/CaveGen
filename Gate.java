class Gate {

    String gateName;
    float life;
    int weight;

    MapUnit mapUnit = null;
    SpawnPoint spawnPoint = null;
    float posX, posY, posZ, ang;

    Gate spawn(MapUnit m, SpawnPoint sp) {
        Gate g = new Gate();
        g.gateName = gateName;
        g.life = life;
        g.weight = weight;
        g.mapUnit = m;
        g.spawnPoint = sp;
        return g;
    }
}
