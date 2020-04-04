import java.util.*;
import java.io.*;

// This code reads the files

class Parser {

    static String all[] = new String[] {"tutorial_1","tutorial_2","tutorial_3","forest_1",
                                        "forest_2","forest_3","forest_4","yakushima_1",
                                        "yakushima_2","yakushima_3","yakushima_4",
                                        "last_1","last_2","last_3",
                                        "ch_ABEM_tutorial", 
                                        "ch_NARI_07whitepurple", 
                                        "ch_NARI_03toy", 
                                        "ch_NARI_01kusachi", 
                                        "ch_ABEM_LeafChappy", 
                                        "ch_NARI_05start3easy", 
                                        "ch_MUKI_metal", 
                                        "ch_MAT_limited_time", 
                                        "ch_MAT_t_hunter_hana", 
                                        "ch_MUKI_damagumo", 
                                        "ch_MAT_t_hunter_enemy", 
                                        "ch_MAT_conc_cave", 
                                        "ch_NARI_04series", 
                                        "ch_MAT_t_hunter_otakara", 
                                        "ch_MUKI_bigfoot", 
                                        "ch_MIYA_oopan", 
                                        "ch_MAT_yellow_purple_white", 
                                        "ch_MUKI_redblue", 
                                        "ch_NARI_08tobasare", 
                                        "ch_NARI_02tile", 
                                        "ch_MAT_crawler", 
                                        "ch_MAT_route_rover", 
                                        "ch_MUKI_enemyzero", 
                                        "ch_NARI_09suikomi", 
                                        "ch_MUKI_houdai", 
                                        "ch_NARI_06start3hard", 
                                        "ch_MAT_flier", 
                                        "ch_MIYA_trap", 
                                        "ch_MUKI_bombing", 
                                        "ch_MUKI_king"};
    static String special[] = new String[] {"EC", "SCx", "FC", "HoB", "WFG", "BK", "SH",
                                            "CoS", "GK", "SR", "SC", "CoC", "HoH", "DD",
                                            "CH1","CH2","CH3","CH4","CH5",
                                            "CH6","CH7","CH8","CH9","CH10",
                                            "CH11","CH12","CH13","CH14","CH15",
                                            "CH16","CH17","CH18","CH19","CH20",
                                            "CH21","CH22","CH23","CH24","CH25",
                                            "CH26","CH27","CH28","CH29","CH30"};
    static String all251[] = new String[] {"abandoned_toybox","ivory_metalworks","aquatic_depths","Gamblers_den","flaming_trench",
                                            "wooden_fortress","grubdog_domain","steel_catacombs","yakushima_1",
                                            "yakushima_2","yakushima_3","yakushima_4",
                                            "last_1","last_2","last_3", "tutorial_4",
                                            "ch1_snow_tutorial", 
                                            "ch2_purplepikmin", 
                                            "ch3_casino_easy", 
                                            "ch4_bulbmin", 
                                            "ch5_sand", 
                                            "ch6_tutorial", 
                                            "ch7_forest", 
                                            "ch8_yakushima", 
                                            "ch9_last", 
                                            "ch10_special", 
                                            "ch11_breadbugs", 
                                            "ch12_newmetal_maze", 
                                            "ch13_coc8", 
                                            "ch14_orange", 
                                            "ch15_candypop", 
                                            "ch16_big_snow", 
                                            "ch17_snagrets", 
                                            "ch18_cavemetal05", 
                                            "ch19_BigFoot", 
                                            "ch20_crawbster", 
                                            "ch21_casino_large", 
                                            "ch22_big_tile", 
                                            "ch23_shady_garden", 
                                            "ch24_trap", 
                                            "ch25_8floor", 
                                            "ch26_waterwraith", 
                                            "ch27_conveyor", 
                                            "ch28_emperor", 
                                            "ch29_bigtreasure", 
                                            "ch30_gauntlet"};
    static String special251[] = new String[] {"AT", "IM", "AD", "GD", "FT", "WF", "GdD", "SC",
                                                "AS", "SS", "CK", "PoW", "PoM", "EA", "DD", "PP",
                                                "CH1","CH2","CH3","CH4","CH5",
                                                "CH6","CH7","CH8","CH9","CH10",
                                                "CH11","CH12","CH13","CH14","CH15",
                                                "CH16","CH17","CH18","CH19","CH20",
                                                "CH21","CH22","CH23","CH24","CH25",
                                                "CH26","CH27","CH28","CH29","CH30"};

    static String fromSpecial(String s) {
        if (CaveGen.p251) {
            all = all251;
            special = special251;
        }
        for (int i = 0; i < special.length; i++) {
            if (special[i].equalsIgnoreCase(s))
                return all[i] + ".txt";
        }
        return s;
    }

    static String toSpecial(String s){
        if (CaveGen.p251) {
            all = all251;
            special = special251;
        }
        for (int i = 0; i < all.length; i++) {
            if (s.equals(all[i] + ".txt"))
                return special[i];
        }
        return s;
    }

    CaveGen g;

    Parser(CaveGen g) {
        this.g = g;

        parseAll();
    }

    Scanner read(String fileName) {
        StringBuilder sb2 = new StringBuilder();
        try {
            Reader r = new BufferedReader(new InputStreamReader(
                              new FileInputStream(fileName), "US-ASCII"));
        
            int intch;
            while ((intch = r.read()) != -1) {
                char ch = (char) intch;
                sb2.append(""+ch);
            }
            r.close();
        } catch(Exception e) {
            if (!fileName.contains("layout.txt"))
                e.printStackTrace();
            return new Scanner("0");
        }

        try {
            if (CaveGen.prints) 
                System.out.println("File read: " + fileName);
            Scanner s = new Scanner(sb2.toString());
            StringBuilder sb = new StringBuilder();
            while (s.hasNextLine()){
                String st = s.nextLine().trim();
                int hash = st.indexOf("#");
                if (hash >= 0) st = st.substring(0,hash);
                sb.append(st + " ");
            }
            s.close();
            String sbs = sb.toString();
            // System.out.println(sbs);
            return new Scanner(sbs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    int nextInt(Scanner sc) {
        String s = sc.next();
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return nextInt(sc);
        }
    }

    String nextString(Scanner sc) {
        return sc.next();
    }

    float nextFloat(Scanner sc) {
        String s = sc.next();
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return nextInt(sc);
        }
    }

    String nextBrace(Scanner sc) {
        String s = sc.next();
        if (s.equals("{"))
            return "{";
        return nextBrace(sc);
    }

    String nextCloseBrace(Scanner sc) {
        String s = sc.next();
        if (s.equals("}"))
            return "}";
        return nextCloseBrace(sc);
    }

    void parseAll() {
        Scanner sc = read("files/" + g.fileSystem + "/" + "caveinfo/" + g.caveInfoName);

        nextCloseBrace(sc);
        int numSublevels = nextInt(sc);
        nextBrace(sc);

        assert g.sublevel <= numSublevels && g.sublevel > 0;
        g.isFinalFloor = g.sublevel == numSublevels;
        //System.out.println(numSublevels); 

        for (int i = 0; i < g.sublevel*5 - 5; i++) nextBrace(sc);

        
        while (true) {
            String id = nextString(sc);
            if (id.equals("{_eof}")) break;

            String inter = nextString(sc);
            switch(id) {
            case "{f000}": 
            case "{f001}": int sublevel = nextInt(sc);
                assert sublevel + 1 == g.sublevel; break;
            case "{f002}": g.maxMainTeki = nextInt(sc); break;
            case "{f003}": g.maxItem = nextInt(sc); break;    
            case "{f004}": g.maxGate = nextInt(sc); break;
            case "{f014}": g.capProb = nextInt(sc) / 100.0f; break;
            case "{f005}": g.maxRoom = nextInt(sc); break;
            case "{f006}": g.corridorProb = nextFloat(sc); break;
            case "{f007}": g.hasGeyser = 1 == nextInt(sc); break;
            case "{f008}": g.caveUnitFile = nextString(sc); break;
            case "{f009}": g.lightingFile = nextString(sc); break;
            case "{f00A}": g.skyboxFile = nextString(sc); break;
            case "{f010}": g.holeClogged = 1 == nextInt(sc); break;
            case "{f011}": g.echoStrength = nextInt(sc); break;
            case "{f012}": g.musicType = nextInt(sc); break;
            case "{f013}": g.hasFloorPlane = 1 == nextInt(sc); break;
            case "{f015}": g.allowCapSpawns = 1 == nextInt(sc); break;
            case "{f016}": g.waterwraithTimer = nextFloat(sc); break;
            case "{f017}": g.hasSeesaw = 0 != nextInt(sc); break;
            default: System.out.println("Unknown param: " + id + " "
                                        + inter + " -> " + nextString(sc));
            }

            
        }

        g.spawnMapUnits = new ArrayList<MapUnit>();
        g.spawnMapUnitsSorted = new ArrayList<MapUnit>();
        g.spawnMapUnitsSortedAndRotated = new ArrayList<MapUnit>();
        g.spawnTeki0 = new ArrayList<Teki>();
        g.spawnTeki1 = new ArrayList<Teki>();
        g.spawnTeki5 = new ArrayList<Teki>();
        g.spawnTeki8 = new ArrayList<Teki>();
        g.spawnTeki6 = new ArrayList<Teki>();
        g.spawnItem = new ArrayList<Item>();
        g.spawnGate = new ArrayList<Gate>();
        g.spawnCapTeki = new ArrayList<Teki>();
        g.spawnCapFallingTeki = new ArrayList<Teki>();
        g.spawnTekiConsolidated = new ArrayList<Teki>();

        int n;
        nextBrace(sc);
        n = nextInt(sc);

        for (int i = 0; i < n; i++) {
            String rawTekiName = nextString(sc);
            int rawWeight = nextInt(sc);
            int type = nextInt(sc);
            Teki t = new Teki();
            t.type = type;
            assert type >= 0 && type <= 8;
            t.min = rawWeight / 10;
            t.weight = rawWeight % 10;
            if (type == 6) { // plants use this field differently
                t.min = rawWeight;
                t.weight = 0;
            }
            int firstLetter = 0;
            char ch = rawTekiName.charAt(0);
            if (ch ==  '$') {
                firstLetter++;
                char ch2 = rawTekiName.charAt(1);
                if (ch2 >= '0' && ch2 <= '9') {
                    t.fallType = ch2 - '0';
                    firstLetter++;
                }
                else t.fallType = 1;
            } else t.fallType = 0;
            assert t.fallType >= 0 && t.fallType <= 5;
            rawTekiName = rawTekiName.substring(firstLetter);
            int i_ = rawTekiName.indexOf('_');
            if (i_ >= 0 && rawTekiName.length() > i_ + 2) {
                t.tekiName = rawTekiName.substring(0,i_);
                t.itemInside = rawTekiName.substring(i_+1);
            } else t.tekiName = rawTekiName;
            if (t.type == 0)
                g.spawnTeki0.add(t);
            if (t.type == 1)
                g.spawnTeki1.add(t);
            if (t.type == 5)
                g.spawnTeki5.add(t);
            if (t.type == 8)
                g.spawnTeki8.add(t);
            if (t.type == 6)
                g.spawnTeki6.add(t);
            g.spawnTekiConsolidated.add(t);
        }

        nextBrace(sc);
        n = nextInt(sc);

        for (int j = 0; j < n; j++) {
            String ItemName = nextString(sc);
            int rawWeight = nextInt(sc);
            Item i = new Item();
            i.itemName = ItemName;
            i.min = rawWeight / 10;
            i.weight = rawWeight % 10;
            g.spawnItem.add(i);
        }

        nextBrace(sc);
        n = nextInt(sc);

        for (int i = 0; i < n; i++) {
            String gateName = nextString(sc);
            float gateLife = nextFloat(sc);
            int weight = nextInt(sc);
            Gate t = new Gate();
            t.gateName = gateName;
            t.life = gateLife;
            t.weight = weight;
            g.spawnGate.add(t);
        }

        nextBrace(sc);
        n = nextInt(sc);

        for (int i = 0; i < n; i++) {
            int capType = nextInt(sc);
            String rawTekiName = nextString(sc);
            int rawWeight = nextInt(sc);
            int type = nextInt(sc);
            Teki t = new Teki();
            t.capType = capType;
            t.type = type;
            assert capType == 0;
            assert type == 0 || type == 1;
            t.min = rawWeight / 10;
            t.weight = rawWeight % 10;
            int firstLetter = 0;
            char ch = rawTekiName.charAt(0);
            if (ch ==  '$') {
                firstLetter++;
                char ch2 = rawTekiName.charAt(1);
                if (ch2 >= '0' && ch2 <= '9') {
                    t.fallType = ch2 - '0';
                    firstLetter++;
                }
                else t.fallType = 1;
            } else t.fallType = 0;
            assert t.fallType >= 0 && t.fallType <= 5;
            rawTekiName = rawTekiName.substring(firstLetter);
            int i_ = rawTekiName.indexOf('_');
            if (i_ >= 0 && rawTekiName.length() > i_ + 2) {
                t.tekiName = rawTekiName.substring(0,i_);
                t.itemInside = rawTekiName.substring(i_+1);
            } else t.tekiName = rawTekiName;

            if (t.fallType == 0 || g.isPomGroup(t)) g.spawnCapTeki.add(t);
            else g.spawnCapFallingTeki.add(t);
            g.spawnTekiConsolidated.add(t);
        }

        sc = read("files/" + g.fileSystem + "/" + "units/" + g.caveUnitFile);

        int numUnits = nextInt(sc);
        g.maxNumDoorsSingleUnit = 0;

        for (int i1 = 0; i1 < numUnits; i1++) {
            nextBrace(sc);
            MapUnit m = new MapUnit();
            m.version = nextInt(sc);
            m.name = nextString(sc);
            m.dX = nextInt(sc);
            m.dZ = nextInt(sc);
            m.type = nextInt(sc);
            m.flag0 = nextInt(sc);
            m.flag1 = nextInt(sc);
            m.numDoors = nextInt(sc);
            g.maxNumDoorsSingleUnit = Math.max(m.numDoors, g.maxNumDoorsSingleUnit);
            m.doors = new ArrayList<Door>();
            for (int i2 = 0; i2 < m.numDoors; i2++) {
                Door d = new Door();
                d.idx = nextInt(sc);
                d.dirSide = nextInt(sc);
                d.offsetSide = nextInt(sc);
                d.wpIdx = nextInt(sc);
                d.numDoorLinks = nextInt(sc);
                d.doorLinks = new ArrayList<DoorLink>();
                for (int i3 = 0; i3 < d.numDoorLinks; i3++) {
                    DoorLink l = new DoorLink();
                    l.dist = nextFloat(sc);
                    l.otherIdx = nextInt(sc);
                    l.tekiFlag = nextInt(sc);
                    d.doorLinks.add(l);
                }
                m.doors.add(d);
                d.mapUnit = m;
            }

            Scanner sc2 = read("files/" + g.fileSystem + "/" + "arc/" + m.name + "/texts.d/layout.txt");

            int numSpawnPoints = nextInt(sc2);

            m.spawnPoints = new ArrayList<SpawnPoint>();
            for (int i4 = 0; i4 < numSpawnPoints; i4++) {
                nextBrace(sc2);
                SpawnPoint sp = new SpawnPoint();
                sp.spawnPointIdx = i4;
                sp.type = nextInt(sc2);
                sp.x = nextFloat(sc2);
                sp.y = nextFloat(sc2);
                sp.z = nextFloat(sc2);
                sp.angle = nextFloat(sc2);
                sp.radius = nextFloat(sc2);
                sp.minNum = nextInt(sc2);
                sp.maxNum = nextInt(sc2);
                sp.spawnListIdx = m.spawnPoints.size();
                m.spawnPoints.add(sp);
                sp.mapUnit = m;
            }

            Scanner sc3 = read("files/" + g.fileSystem + "/" + "arc/" + m.name + "/texts.d/waterbox.txt");

            nextBrace(sc3);
            int numWaterBoxes = nextInt(sc3);
            m.waterBoxes = new float[numWaterBoxes][6];

            for (int i4 = 0; i4 < numWaterBoxes; i4++) {
                for (int i5 = 0; i5 < 6; i5++)
                    m.waterBoxes[i4][i5] = nextFloat(sc3);
            }

            Scanner sc4 = read("files/" + g.fileSystem + "/" + "arc/" + m.name + "/texts.d/route.txt");
            int numWaypoints = nextInt(sc4);
            m.wayPoints = new ArrayList<WayPoint>();

            for (int i4 = 0; i4 < numWaypoints; i4++) {
                nextBrace(sc4);
                WayPoint wp = new WayPoint();
                wp.idx = nextInt(sc4);
                int numLinks = nextInt(sc4);
                wp.links = new ArrayList<Integer>();
                for (int i5 = 0; i5 < numLinks; i5++) {
                    wp.links.add(nextInt(sc4));
                }
                wp.x = nextFloat(sc4);
                wp.y = nextFloat(sc4);
                wp.z = nextFloat(sc4);
                wp.radius = nextFloat(sc4);
                wp.mapUnit = m;
                m.wayPoints.add(wp);
            }

            g.spawnMapUnits.add(m);

            sc2.close();
            sc3.close();
            sc4.close();
        }

        sc.close();

        for (int i = 0; i < g.spawnTeki0.size(); i++)
            g.spawnTeki0.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnTeki1.size(); i++)
            g.spawnTeki1.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnTeki5.size(); i++)
            g.spawnTeki5.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnTeki8.size(); i++)
            g.spawnTeki8.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnTeki6.size(); i++)
            g.spawnTeki6.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnItem.size(); i++)
            g.spawnItem.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnGate.size(); i++)
            g.spawnGate.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnCapTeki.size(); i++)
            g.spawnCapTeki.get(i).spawnListIdx = i;
        for (int i = 0; i < g.spawnCapFallingTeki.size(); i++)
            g.spawnCapFallingTeki.get(i).spawnListIdx = i;

        return;
    }

    static HashMap<String, Integer> tekiDifficulty = new HashMap<String, Integer>();
    static HashMap<String, Integer> minCarry = new HashMap<String, Integer>();
    static HashMap<String, Integer> maxCarry = new HashMap<String, Integer>();
    static HashMap<String, Integer> pokos = new HashMap<String, Integer>();
    static HashMap<String, Integer> seeds = new HashMap<String, Integer>();
    static HashMap<String, Integer> seedsMin = new HashMap<String, Integer>();
    static HashMap<String, Integer> depth = new HashMap<String, Integer>();


    static void readConfigFiles() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/" + CaveGen.fileSystem + "/config/teki_difficulty.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                Scanner sc = new Scanner(line);
                sc.useDelimiter(",");
                String id = sc.next();
                String hexId = sc.next();
                String commonName = sc.next();
                String internalName = sc.next();
                int difficulty = Integer.parseInt(sc.next());
                tekiDifficulty.put(internalName.toLowerCase(), difficulty);
                sc.close();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/" + CaveGen.fileSystem + "/config/config_" + CaveGen.region + ".txt"));
            String line;
            while ((line = br.readLine()) != null) {
                Scanner sc = new Scanner(line);
                sc.useDelimiter(",");
                String internalName = sc.next();
                minCarry.put(internalName.toLowerCase(), Integer.parseInt(sc.next()));
                maxCarry.put(internalName.toLowerCase(), Integer.parseInt(sc.next()));
                seeds.put(internalName.toLowerCase(), Integer.parseInt(sc.next()));
                seedsMin.put(internalName.toLowerCase(), Integer.parseInt(sc.next()));
                pokos.put(internalName.toLowerCase(), Integer.parseInt(sc.next()));
                depth.put(internalName.toLowerCase(), (int)Double.parseDouble(sc.next()));
                sc.close();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static int[] scUnitTypes;
    static int[] scRots;
    static int[] scUnitIdsFrom;
    static int[] scDoorsFrom;
    static int[] scDoorsTo;

    static void parseShortCircuitString() {
        String[] tok1 = CaveGen.requireMapUnitsConfig.split(";");
        int N = tok1.length;
        scUnitTypes = new int[N];
        scRots = new int[N];
        scUnitIdsFrom = new int[N];
        scDoorsFrom = new int[N];
        scDoorsTo = new int[N];
        for (int i = 0; i < N; i++) {
            String[] tok2 = tok1[i].split(",");
            scUnitTypes[i]      = tok2[0].equals("_") ? -1 : Integer.parseInt(tok2[0]);
            scRots[i]           = tok2[1].equals("_") ? -1 : Integer.parseInt(tok2[1]);
            scUnitIdsFrom[i]    = tok2[2].equals("_") ? -1 : Integer.parseInt(tok2[2]);
            scDoorsFrom[i]      = tok2[3].equals("_") ? -1 : Integer.parseInt(tok2[3]);
            scDoorsTo[i]        = tok2[4].equals("_") ? -1 : Integer.parseInt(tok2[4]);
        }
    }
}
