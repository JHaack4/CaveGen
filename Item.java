class Item {
    
    String itemName;
    int min;
    int weight;

    SpawnPoint spawnPoint;
    float posX, posY, posZ, ang;

    int spawnListIdx = -1;

    Item spawn(MapUnit m, SpawnPoint sp) {
        Item i = new Item();
        i.itemName = itemName;
        i.min = min;
        i.weight = weight;
        i.spawnPoint = sp;
        i.spawnListIdx = spawnListIdx;
        return i;
    }
}
