import java.util.*;

class Aggregator {

    static HashMap<String, Aggregator> aggs;
    static int numLayoutsAggregated;
    
    static void reset() {
        aggs = new HashMap<String, Aggregator>();
        numLayoutsAggregated = 0;
    }

    static void process(CaveGen g) { // need to use cavegen's anyways due to locations!
        String hash = toLayoutHash(g);
        Aggregator agg;
        if (aggs.containsKey(hash)) {
            agg = aggs.get(hash);
        } else {
            agg = new Aggregator(g, hash);
            aggs.put(hash, agg);
        }
        agg.add(g);
        numLayoutsAggregated += 1;
    }

    private static int relativeX(MapUnit m, MapUnit first) {
        switch(first.rotation) {
            case 0:
                return m.offsetX - first.offsetX;
            case 1:
                return  m.offsetZ - first.offsetZ;
            case 2:
                return -m.offsetX - m.dX - (-first.offsetX - first.dX);
            case 3:
                return -m.offsetZ - m.dZ - (-first.offsetZ - first.dZ);
        }
        return 0;
    }
    private static int relativeZ(MapUnit m, MapUnit first) {
        switch(first.rotation) {
            case 0:
                return m.offsetZ - first.offsetZ;
            case 1:
                return -m.offsetX - m.dX - (-first.offsetX - first.dX);
            case 2:
                return -m.offsetZ - m.dZ - (-first.offsetZ - first.dZ);
            case 3:
                return m.offsetX - first.offsetX;
        }
        return 0;
    }

    static String toLayoutHash(CaveGen g) {
        StringBuilder s = new StringBuilder();
        final MapUnit first = g.placedMapUnits.get(0);
        int lastRoomIdx = 0;
        ArrayList<MapUnit> sortMapUnits = new ArrayList<MapUnit>();
        for (MapUnit m: g.placedMapUnits) {
            sortMapUnits.add(m);
            if (m.type == 1) {
                lastRoomIdx = m.placedListIdx;
            }
        }
        final int lastRoomIdx_ = lastRoomIdx;
        Collections.sort(sortMapUnits, new Comparator<MapUnit>() {
            public int compare(MapUnit m1, MapUnit m2) {
                if (m1.placedListIdx == 0) return -1;
                if (m2.placedListIdx == 0) return 1;
                if (m1.placedListIdx <= lastRoomIdx_ && m2.placedListIdx > lastRoomIdx_) return -1;
                if (m2.placedListIdx <= lastRoomIdx_ && m1.placedListIdx > lastRoomIdx_) return 1;
                int dx = relativeX(m1, first) - relativeX(m2, first);
                int dz = relativeZ(m1, first) - relativeZ(m2, first);
                if (dx != 0) return dx;
                return dz;
            }
        });
        for (MapUnit m: sortMapUnits) {
            if (CaveGen.aggFirst && m.placedListIdx != 0) continue;
            if (CaveGen.aggHalls && m.placedListIdx > lastRoomIdx) continue;
            if (CaveGen.aggRooms && m.type != 1) continue;
            int r = (m.rotation - first.rotation + 4) % 4;
            int x = relativeX(m, first), z = relativeZ(m, first);
            s.append("|" + m.spawnListIdx + "r" + r + "x" + x + "z" + z);
        }
        return s.toString();
    }

    /*class Loc {
        int x,y;
        Loc(int x, int y) {
            this.x=x;
            this.y=y;
        }
    }

    class Locations {
        ArrayList<Loc> locs;
        Locations() {
            locs = new ArrayList<Loc>();
        }
        void add(Loc loc) {
            locs.add(loc);
        }
    }

    HashMap<Teki, Locations> tekiLocations;
    HashMap<Item, Locations> itemLocations;
    HashMap<Gate, Locations> gateLocations; 
    int numInstances;
    String hash;
    int idx;

    Aggregator(CaveGen init, String hash) {
        numInstances = 0;
        idx = -1;
        this.hash = hash;
        tekiLocations = new HashMap<Teki, Locations>();
        itemLocations = new HashMap<Item, Locations>();
        gateLocations = new HashMap<Gate, Locations>();

        for (Teki t: init.spawnTeki0) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnTeki1) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnTeki5) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnTeki6) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnTeki8) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnCapTeki) 
            tekiLocations.put(t, new Locations());
        for (Teki t: init.spawnCapFallingTeki) 
            tekiLocations.put(t, new Locations());
        for (Item t: init.spawnItem) 
            itemLocations.put(t, new Locations());
        for (Gate t: init.spawnGate) 
            gateLocations.put(t, new Locations());
        
    }

    void add(CaveGen g) {
        for (Teki t: g.placedTekis) {
            tekiLocations.get(t.tekiInfoSpawnedFrom).add(calcLoc(g,t.spawnPoint));
        }
        for (Item t: g.placedItems) {
            itemLocations.get(t.itemInfoSpawnedFrom).add(calcLoc(g,t.spawnPoint));
        }
        for (Gate t: g.placedGates) {
            gateLocations.get(t.gateInfoSpawnedFrom).add(calcLoc(g,t.spawnPoint));
        }
        numInstances += 1;
    }
    
    Loc calcLoc(CaveGen g, SpawnPoint sp) {
        return new Loc((int)sp.posX, (int)sp.posZ); // TODO rotation invariant
    }*/

    final static Object startObj = new Object();
    final static Object holeObj = new Object();
    final static Object geyserObj = new Object();

    class Loc {
        int x,z;
        Loc(int x, int z) {
            this.x=x;
            this.z=z;
        }
    }

    private class Placement {
        HashMap<Object, Integer> placements;
        Placement() {
            placements = new HashMap<Object, Integer>();
        }
        void add(Object o) {
            if (placements.containsKey(o)) {
                placements.put(o, placements.get(o) + 1);
            } else {
                placements.put(o, 1);
            }
        }
    }

    private HashMap<String, Loc> uniqueLocations;
    private HashMap<Loc, Placement> placements;

    int numInstances;
    String hash;
    int idx;

    Aggregator(CaveGen init, String hash) {
        numInstances = 0;
        idx = -1;
        this.hash = hash;
        uniqueLocations = new HashMap<String, Loc>();
        placements = new HashMap<Loc, Placement>();        
    }

    private void add(CaveGen g) {
        if (g.placedStart != null) {
            addLoc(startObj, calcLoc(g, g.placedStart));
        }
        if (g.placedHole != null) {
            addLoc(holeObj, calcLoc(g, g.placedHole));
        }
        if (g.placedGeyser != null) {
            addLoc(geyserObj, calcLoc(g, g.placedGeyser));
        }
        for (Teki t: g.placedTekis) {
            addLoc(t.tekiInfoSpawnedFrom, calcLoc(g,t.spawnPoint));
        }
        for (Item t: g.placedItems) {
            addLoc(t.itemInfoSpawnedFrom, calcLoc(g,t.spawnPoint));
        }
        for (Gate t: g.placedGates) {
            addLoc(t.gateInfoSpawnedFrom, calcLoc(g,t.spawnPoint));
        }
        numInstances += 1;
    }

    private void addLoc(Object o, Loc loc) {
        Placement p = placements.get(loc);
        p.add(o);
    }

    Loc calcLoc(CaveGen g, SpawnPoint sp) {
        MapUnit first = g.placedMapUnits.get(0);
        float xx = sp.posX - first.offsetX * 170.0f;
        float zz = sp.posZ - first.offsetZ * 170.0f;
        float x=0,z=0;
        switch(g.placedMapUnits.get(0).rotation) {
            case 0:
                x = xx;
                z = zz;
                break;
            case 1:
                x = zz;
                z = -xx + first.dX * 170.0f;
                break;
            case 2:
                x = -xx + first.dX * 170.0f;
                z = -zz + first.dZ * 170.0f;
                break;
            case 3:
                x = -zz + first.dZ * 170.0f;
                z = xx;
                break;
        }
        x = Math.round(x);
        z = Math.round(z);
        String hash = (int)x + "," + (int)z;
        if (uniqueLocations.containsKey(hash)) {
            return uniqueLocations.get(hash);
        } else {
            Loc loc = new Loc((int)x, (int)z);
            uniqueLocations.put(hash, loc);
            placements.put(loc, new Placement());
            return loc;
        }
    }

    class Placed {
        Teki teki = null;
        Item item = null;
        Gate gate = null;
        boolean start = false;
        boolean hole = false;
        boolean geyser = false;
        int count = 0;
        Placed() {};
    }

    ArrayList<Loc> locsList() {
        ArrayList<Loc> l = new ArrayList<Loc>();
        for (Loc ll: placements.keySet())
            l.add(ll);
        return l;
    }

    ArrayList<Placed> toPlacedList(Loc loc) {
        Placement placement = placements.get(loc);
        ArrayList<Placed> placed = new ArrayList<Placed>();
        for (Object o: placement.placements.keySet()) {
            Placed p = new Placed();
            p.count = placement.placements.get(o);
            if (o == startObj)
                p.start = true;
            else if (o == holeObj) 
                p.hole = true;
            else if (o == geyserObj)
                p.geyser = true;
            else if (o instanceof Teki)
                p.teki = (Teki)o;
            else if (o instanceof Item)
                p.item = (Item)o;
            else if (o instanceof Gate)
                p.gate = (Gate)o;
            placed.add(p);
        }
        Collections.sort(placed, new Comparator<Placed>() {
            public int compare(Placed p1, Placed p2) {
                return p2.count - p1.count;
            }
        });

        return placed;
    }

}