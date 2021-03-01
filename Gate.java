class Gate {

    String gateName;
    float life;
    int weight;

    SpawnPoint spawnPoint = null;
    float posX, posY, posZ, ang;

    int spawnListIdx = -1;

    Gate gateInfoSpawnedFrom = null;

    boolean isInTheWay = false;

    Gate spawn(MapUnit m, SpawnPoint sp) {
        Gate g = new Gate();
        g.gateName = gateName;
        g.life = life;
        g.weight = weight;
        g.spawnPoint = sp;
        g.spawnListIdx = spawnListIdx;
        g.gateInfoSpawnedFrom = this;
        return g;
    }
}
