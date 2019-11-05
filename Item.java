class Item {
    
    String itemName;
    int min;
    int weight;

    MapUnit mapUnit;
    SpawnPoint spawnPoint;
    float posX, posY, posZ, ang;

    Item spawn(MapUnit m, SpawnPoint sp) {
        Item i = new Item();
        i.itemName = itemName;
        i.min = min;
        i.weight = weight;
        i.mapUnit = m;
        i.spawnPoint = sp;
        return i;
    }
}
