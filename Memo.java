import java.util.*;
import java.io.*;

public class Memo {
    
    Memo() {}

    // ---------------------------- SHORT CIRCUIT -----------------------------

    // Check for a short circuit (returns true to short circuit)
    boolean checkForShortCircuit(CaveGen g) {
        int i = g.placedMapUnits.size() - 1;
        MapUnit m = g.placedMapUnits.get(i);
        if (i >= Parser.scUnitTypes.length) return false;
        if (Parser.scUnitTypes[i] != -1) {
            String targetName = g.spawnMapUnitsSorted.get(Parser.scUnitTypes[i]).name;
            if (!targetName.equals(m.name))
                return true;
        } 
        if (Parser.scRots[i] != -1) {
            if (m.rotation != Parser.scRots[i])
                return true;
        } 
        if (Parser.scUnitIdsFrom[i] != -1 
                && Parser.scDoorsFrom[i] != -1
                && Parser.scDoorsTo[i] != -1) {
            Door d = m.doors.get(Parser.scDoorsTo[i]);
            if (d.adjacentDoor == null || d.adjacentDoor.mapUnit == null)
                return true;
            MapUnit o = d.adjacentDoor.mapUnit;
            if (g.placedMapUnits.indexOf(o) != Parser.scUnitIdsFrom[i])
                return true;
            if (o.doors.indexOf(d.adjacentDoor) != Parser.scDoorsFrom[i])
                return true;
        }
        else {
            if (Parser.scDoorsTo[i] != -1) {
                Door d = m.doors.get(Parser.scDoorsTo[i]);
                if (d.adjacentDoor == null || d.adjacentDoor.mapUnit == null)
                    return true;
            }
            if (Parser.scUnitIdsFrom[i] != -1 
                    && Parser.scDoorsFrom[i] != -1) {
                boolean isGood = false;
                for (Door d: m.doors) {
                    if (d.adjacentDoor == null || d.adjacentDoor.mapUnit == null)
                        continue;
                    MapUnit o = d.adjacentDoor.mapUnit;
                    if (g.placedMapUnits.indexOf(o) != Parser.scUnitIdsFrom[i])
                        continue;
                    if (o.doors.indexOf(d.adjacentDoor) != Parser.scDoorsFrom[i])
                        continue;
                    isGood = true;
                }
                if (!isGood) return true;
            }
            else if (Parser.scUnitIdsFrom[i] != -1) {
                boolean isGood = false;
                for (Door d: m.doors) {
                    if (d.adjacentDoor == null || d.adjacentDoor.mapUnit == null)
                        continue;
                    MapUnit o = d.adjacentDoor.mapUnit;
                    if (g.placedMapUnits.indexOf(o) != Parser.scUnitIdsFrom[i])
                        continue;
                    isGood = true;
                }
                if (!isGood) return true;
            }
        }
        return false;
    }

    // ---------------------------- EXPECTATION TESTS -----------------------------

    // The purpose of these functions is to compare the output from the currect run
    // with the "expected_output.txt" file, and see if they are the same.
    // This is helpful to see if debugging has unexpectedly produced changes to the tool.
    ArrayList<String> expect = new ArrayList<String>();
    
    void setupExpectTests() {
        expect = new ArrayList<String>();

        CaveGen.caveInfoName = "both";
        CaveGen.numToGenerate = 100;
        CaveGen.firstGenSeed = 0;
        CaveGen.prints = false;
        CaveGen.images = false;
        CaveGen.folderSeed = false;
        CaveGen.folderCave = false;
        CaveGen.region = "us";
        CaveGen.fileSystem = "gc";
    }

    void outputSublevelForExpect(CaveGen g) {
        String s = CaveGen.specialCaveInfoName + " " + CaveGen.sublevel + " on seed " + Drawer.seedToString(g.initialSeed);
        System.out.println("Checking: " + s);
        expect.add("Generating " + s);
        for (MapUnit m: g.placedMapUnits) {
            expect.add("Placed: " + m.name + " " + m.rotation + " " + m.offsetX + "," + m.offsetZ);
        }
        for (Teki t: g.placedTekis) {
            expect.add("Spawned: " + t.tekiName + " " + t.type + (t.fallType == 0 ? "" : "f") + " " + t.posX + "," + t.posZ); 
        }
        for (Item t: g.placedItems) {
            expect.add("Spawned: " + t.itemName + " " + "2" + " " + t.posX + "," + t.posZ); 
        }
        for (Gate t: g.placedGates) {
            expect.add("Spawned: " + "gate" + t.life + " " + "5g" + " " + t.posX + "," + t.posZ); 
        }
    }


    void checkExpectation() {
        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter("files/expected_output_this.txt"));
            for (String s: expect) wr.write(s + "\n");
            wr.close();

            Scanner sc = new Scanner(new FileReader("files/expected_output.txt"));
            for (int i = 0; i < expect.size(); i++) {
                if (sc.hasNextLine()) {
                    String s = sc.nextLine();
                    if (!s.equals(expect.get(i))) {
                        System.out.println("Expectation test failed on line " + (i+1));
                        System.out.println("Expected: " + s);
                        System.out.println("Got:      " + expect.get(i));
                        return;
                    }
                } else {
                    System.out.println("Expectation test failed (output too long");
                    return;
                }
            }
            if (sc.hasNext()) {
                System.out.println("Expectation test failed (output too short)");
                return;
            }
            
            sc.close();
            System.out.println("Expectation test passed!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Expectation test failed");
        } 
    }

    
    // ---------------------------- MEMO (SUBLEVEL to .txt generation) -----------------------------

    // write the contents of the generated sublevel to a txt file for later recovery.
    void writeMemo(CaveGen g) {
        try {
            new File("output/!memo").mkdir();
            BufferedWriter wr = new BufferedWriter(new FileWriter("output/!memo/" 
                + g.specialCaveInfoName + "-" + g.sublevel + ".txt", true));
            
            wr.write(">" + Drawer.seedToString(g.initialSeed) + "\n");
            wr.write("M");
            for (MapUnit m: g.placedMapUnits) {
                wr.write("|" + m.spawnListIdx + "r" + m.rotation + "x" + m.offsetX + "z" + m.offsetZ);
            }
            wr.write("\n");
            if (g.placedStart != null) {
                wr.write("S|" + logSpawnPoint(g.placedStart) + "\n");
            }
            if (g.placedHole != null) {
                wr.write("H|" + logSpawnPoint(g.placedHole) + "\n");
            }
            if (g.placedGeyser != null) {
                wr.write("G|" + logSpawnPoint(g.placedGeyser) + "\n");
            }
            for (int type: new int[] {5,8,1,0,6}) {
                boolean placed = false;
                for (Teki t: g.placedTekis) {
                    if (t.type == type && t.spawnPoint.type != 9) {
                        if (!placed) {
                            wr.write(type + "");
                            placed = true;
                        }
                        wr.write("|" + t.spawnListIdx + logSpawnPoint(t.spawnPoint));
                    }
                }
                if (placed) wr.write("\n");
            }
            {
                boolean placed = false;
                for (Item t: g.placedItems) {
                        if (!placed) {
                            wr.write(2 + "");
                            placed = true;
                        }
                        wr.write("|" + t.spawnListIdx + logSpawnPoint(t.spawnPoint));
                }
                if (placed) wr.write("\n");
            }
            for (char type: new char[] {'c','f'}) {
                boolean placed = false;
                for (Teki t: g.placedTekis) {
                    if (t.spawnPoint.type == 9) {
                        boolean c = type == 'c' && (t.fallType == 0 || g.isPomGroup(t));
                        boolean f = type == 'f' && !(t.fallType == 0 || g.isPomGroup(t));
                        if (c || f) {
                            if (!placed) {
                                wr.write(type + "");
                                placed = true;
                            }
                            wr.write("|" + t.spawnListIdx + logSpawnPoint(t.spawnPoint));
                        }
                    }
                }
                if (placed) wr.write("\n");
            }
            {
                boolean placed = false;
                for (Gate t: g.placedGates) {
                        if (!placed) {
                            wr.write("g");
                            placed = true;
                        }
                        wr.write("|" + t.spawnListIdx + logSpawnPoint(t.spawnPoint));
                }
                if (placed) wr.write("\n");
            }
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Log failed");
        } 
    }

    String logSpawnPoint(SpawnPoint sp) {
        if (sp.door != null) 
            return "m" + sp.door.mapUnit.placedListIdx + "d" + sp.door.idx;
        else return "m" + sp.mapUnit.placedListIdx + "s" + sp.spawnListIdx;
    }

    void readMemo(CaveGen g) {
        String sublevel = g.specialCaveInfoName + "-" + g.sublevel;
        String seed = Drawer.seedToString(g.initialSeed);
        ArrayList<String> memo = readStoreFile(sublevel, seed);

        // first line is always map units
        g.sortAndRotateMapUnits();
        String[] ms = memo.get(0).split("[|]");
        for (int i = 1; i < ms.length; i++) {
            String[] m = ms[i].split("[rxz]");
            int type = Integer.parseInt(m[0]);
            int r = Integer.parseInt(m[1]);
            int x = Integer.parseInt(m[2]);
            int z = Integer.parseInt(m[3]);
            MapUnit mPlaced = g.spawnMapUnitsSortedAndRotated.get(type * 4 + r).copy();
            mPlaced.offsetX = x;
            mPlaced.offsetZ = z;
            mPlaced.placedListIdx = g.placedMapUnits.size();
            g.placedMapUnits.add(mPlaced);
            g.closeDoorCheck(mPlaced);
        }
        g.addSpawnPoints();
        //g.buildWayPointGraph();

        for (int j = 1; j < memo.size(); j++) {
            ms = memo.get(j).split("[|]");
            for (int i = 1; i < ms.length; i++) {
                String[] m = ms[i].split("[msd]");
                int type = m[0].length() == 0 ? -1 : Integer.parseInt(m[0]);
                int mm = Integer.parseInt(m[1]);
                int sd = Integer.parseInt(m[2]);
                MapUnit mu = g.placedMapUnits.get(mm);
                SpawnPoint spot = ms[i].contains("d") 
                    ? mu.doors.get(sd).spawnPoint
                    : mu.spawnPoints.get(sd);
                Teki spawn;
                switch (ms[0].charAt(0)) {
                case 'S':
                    g.placedStart = spot;
                    break;
                case 'H':
                    g.placedHole = spot;
                    break;
                case 'G':
                    g.placedGeyser = spot;
                    break;
                case '5':
                    spawn = g.spawnTeki5.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case '8':
                    spawn = g.spawnTeki8.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case '1':
                    spawn = g.spawnTeki1.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case '0':
                    spawn = g.spawnTeki0.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    float radius = spot.maxNum == 1 ? 0 : spot.radius * 0.67f;
                    float ang = (float)Math.PI * 2 * (((i % spot.maxNum) * 1.0f / spot.maxNum + spot.posX * 12.34567f + spot.posZ * 98.76543f) % 1);
                    spawn.posX += Math.sin(ang) * radius;
                    spawn.posZ += Math.cos(ang) * radius;
                    spawn.ang = ang;
                    g.placedTekis.add(spawn);
                    break;
                case '6':
                    spawn = g.spawnTeki6.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case 'c':
                    spawn = g.spawnCapTeki.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case 'f':
                    spawn = g.spawnCapFallingTeki.get(type).spawn(mu, spot);
                    g.setSpawnTekiPos(spawn, spot, false);
                    g.placedTekis.add(spawn);
                    break;
                case '2':
                    Item spawnI = g.spawnItem.get(type).spawn(mu, spot);
                    g.setSpawnItemPos(spawnI, spot);
                    g.placedItems.add(spawnI);
                    break;
                case 'g':
                    Gate spawnG = g.spawnGate.get(type).spawn(mu, spot);
                    g.setSpawnGatePos(spawnG, spot);
                    g.placedGates.add(spawnG);
                    break;
                }
            }
            
        }
        
    }

    String storeSublevel = "";
    ArrayList<String> storeLines;
    HashMap<String, Integer> storeSeeds;

    ArrayList<String> readStoreFile(String sublevel, String seed) {
        if (!sublevel.equals(storeSublevel)) {
            try {
                storeSublevel = sublevel;
                storeLines = new ArrayList<String>();
                storeSeeds = new HashMap<String,Integer>();
                Scanner sc = new Scanner(new FileReader("output/!memo/" + sublevel + ".txt"));
                while (sc.hasNextLine()) {
                    String s = sc.nextLine();
                    storeLines.add(s);
                    if (s.contains(">")) {
                        storeSeeds.put(s, storeLines.size()-1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("readStoreFile failed");
            }
        }

        ArrayList<String> ret = new ArrayList<String>();
        for (int i = storeSeeds.get(">" + seed) + 1; i < storeLines.size(); i++) {
            if (storeLines.get(i).contains(">"))
                break;
            ret.add(storeLines.get(i));
        }
        return ret;
    }

    

    
}