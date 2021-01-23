import java.util.*;
import java.io.*;

public class CaveGen {

    // Tool parameters
    static String caveInfoName, specialCaveInfoName, region, fileSystem, countObject, judgeType;
    static int sublevel, firstGenSeed, numToGenerate, indexBeingGenerated;
    static boolean hardMode, challengeMode, images, prints, showStats, seedOrder, storyModeOverride, challengeModeOverride,
        folderSeed, folderCave, showCaveInfo, drawSpawnPoints,
        drawWayPoints, drawWayPointVertDists, drawWayPointEdgeDists, drawWPEdges,
        drawScores, drawAngles, drawPodAngle, drawTreasureGauge,
        drawNoPlants, drawNoFallType, drawWaterBox, drawQuickGlance,
        drawDoorLinks, drawDoorIds, drawSpawnOrder, drawNoObjects, 
        drawNoBuriedItems, drawNoItems, drawNoTeki, drawNoGates, drawNoOnions,
        drawNoGateLife, drawNoHoles, drawHoleProbs, p251, colossal, ultraRandomizer,
        drawSH6Bulborb, drawFlowPaths, drawTreasurePaths, drawAllPaths,
        drawEnemyScores, drawUnitHoleScores, drawUnitItemScores,
        findGoodLayouts, requireMapUnits, expectTest, noWayPointGraph,
        writeMemo, readMemo, aggregator, aggFirst, aggRooms, aggHalls,
        judgeActive, judgeCombine, judgeRankFile, judgeVsAvg;
    static double findGoodLayoutsRatio, judgeFilterScore, judgeFilterScoreSign, judgeFilterRank;
    static String requireMapUnitsConfig, seedFile, rotateForDraw;
    static boolean shortCircuitMap, imageToggle;

    static Drawer drawer;
    static Stats stats;
    static Seed seedCalc;
    static Memo memo;

    public static void main(String args[]) {
        run(args);
    }

    static void resetParams() {
        region = "us"; fileSystem = "gc"; countObject = "";
        hardMode = true; challengeMode = false; images = true; prints = true; showStats = true; seedOrder = false;
        storyModeOverride = false; challengeModeOverride = false;
        folderSeed = true; folderCave = true; showCaveInfo = false; drawSpawnPoints = false;
        drawWayPoints = false; drawWayPointVertDists = false; drawWayPointEdgeDists = false; drawWPEdges = false;
        drawScores = false; drawAngles = false; drawTreasureGauge = false; drawPodAngle = false;
        drawNoPlants = false; drawNoFallType = false; drawWaterBox = true; drawQuickGlance = false;
        drawDoorLinks = false; drawDoorIds = false; drawSpawnOrder = false; drawNoObjects = false;
        drawNoBuriedItems = false; drawNoItems = false; drawNoTeki = false; drawNoGates = false; drawNoOnions = false;
        drawNoGateLife = false; drawNoHoles = false; drawHoleProbs = false; p251 = false; colossal = false; ultraRandomizer = false;
        drawSH6Bulborb = false; drawFlowPaths = false; drawTreasurePaths = false; drawAllPaths = false;
        drawEnemyScores = false; drawUnitHoleScores = false; drawUnitItemScores = false;
        findGoodLayouts = false; requireMapUnits = false; expectTest = false; noWayPointGraph = false;
        writeMemo = false; readMemo = false; aggregator = false; aggFirst = false; aggRooms = false; aggHalls = false;
        judgeActive = false; judgeCombine = false; judgeRankFile = false; judgeVsAvg = false;
        findGoodLayoutsRatio = 0.01; judgeFilterScore = 0; judgeFilterRank = 0; judgeFilterScoreSign = 0;
        requireMapUnitsConfig = ""; judgeType = "default"; seedFile = ""; rotateForDraw = "";
        firstGenSeed = 0; numToGenerate = 1; indexBeingGenerated = 0;
        imageToggle = true;
        seedCalc = new Seed();
    }

    static void run(String args[]) {
        resetParams();

        boolean allStoryMode = false, allChallengeMode = false;
        String caveArg = "";
        String sublevelArgSt = "";
        try {
            if (args[0].equalsIgnoreCase("-expectTest")) {
                expectTest = true;
            }
            else {
                if (args[0].equalsIgnoreCase("seed")) {
                    folderCave = false;
                } else if (args[0].equalsIgnoreCase("cave")) {
                    folderSeed = false;
                } else if (args[0].equalsIgnoreCase("both")) {
                    // pass
                } else if (args[0].equalsIgnoreCase("none")) {
                    prints = false;
                    images = false;
                    folderSeed = false;
                    folderCave = false;
                } else {
                    System.out.println("Bad argument: " + args[0]);
                    throw new Exception();
                }

                caveArg = args[1];
                sublevelArgSt = args[2];
                if (caveArg.equals("cmal") || caveArg.equals("chall")
                    || caveArg.equals("chal") || caveArg.equals("cmat")) {
                    allChallengeMode = true;
                }
                if (caveArg.equals("small")
                    || caveArg.equals("pod") || caveArg.equals("at")
                    || caveArg.equals("story")) {
                    allStoryMode = true;
                }
                if (caveArg.equals("both") || caveArg.equals("all")) {
                    allStoryMode = true;
                    allChallengeMode = true;
                }

                sublevel = allChallengeMode || allStoryMode || sublevelArgSt.contains("-") ? 0 : Integer.parseInt(sublevelArgSt);
                int startParse = allChallengeMode || allStoryMode || sublevelArgSt.contains("-") ? 2 : 3;

                for (int i = startParse; i < args.length; i++) {
                    String s = args[i];
                    if (s.equalsIgnoreCase("-num"))
                        numToGenerate = (int)(Long.decode(args[++i]).longValue());
                    else if (s.equalsIgnoreCase("0"))
                        continue;
                    else if (s.equalsIgnoreCase("-seed")) {
                        String sd = args[++i];
                        if (sd.charAt(0) == 'n') {
                            firstGenSeed = (int)seedCalc.nth_inv(Long.decode(sd.substring(1)).longValue());
                        }
                        firstGenSeed = (int)(Long.decode(sd).longValue());
                    }
                    else if (s.equalsIgnoreCase("-region"))
                        region = args[++i].toLowerCase();
                    else if (s.equalsIgnoreCase("-251")) {
                        p251 = true;
                        fileSystem = "251";
                    }
                    else if (s.equalsIgnoreCase("-ultraRandomizer")) {
                        ultraRandomizer = true;
                    }
                    else if (s.equalsIgnoreCase("-challengeMode"))
                        challengeModeOverride = true;
                    else if (s.equalsIgnoreCase("-storyMode"))
                        storyModeOverride = true;
                    else if (s.equalsIgnoreCase("-noImages"))
                        images = false;
                    else if (s.equalsIgnoreCase("-noPrint") || s.equalsIgnoreCase("-noPrints"))
                        prints = false;
                    else if (s.equalsIgnoreCase("-noStats"))
                        showStats = false;
                    else if (s.equalsIgnoreCase("-drawSpawnPoints"))
                        drawSpawnPoints = true;
                    else if (s.equalsIgnoreCase("-drawScores"))
                        drawScores = true;
                    else if (s.equalsIgnoreCase("-drawEnemyScores"))
                        drawEnemyScores = true;
                    else if (s.equalsIgnoreCase("-drawUnitHoleScores"))
                        drawUnitHoleScores = true;
                    else if (s.equalsIgnoreCase("-drawUnitItemScores"))
                        drawUnitItemScores = true;
                    else if (s.equalsIgnoreCase("-drawDoorLinks"))
                        drawDoorLinks = true;
                    else if (s.equalsIgnoreCase("-drawAllScores")) {
                        drawScores = true;
                        drawEnemyScores = true;
                        drawUnitHoleScores = true;
                        drawDoorLinks = true;
                        drawUnitItemScores = true;
                    }
                    else if (s.equalsIgnoreCase("-drawDoorIds"))
                        drawDoorIds = true;
                    else if (s.equalsIgnoreCase("-drawWayPoints"))
                        drawWayPoints = true;
                    else if (s.equalsIgnoreCase("-drawWPVertexDists"))
                        drawWayPointVertDists = true;
                    else if (s.equalsIgnoreCase("-drawWPEdgeDists"))
                        drawWayPointEdgeDists = true;
                    else if (s.equalsIgnoreCase("-drawWPEdges"))
                        drawWPEdges = true;
                    else if (s.equalsIgnoreCase("-drawAllWayPoints")) {
                        drawWayPoints = true;
                        drawWayPointVertDists = true;
                        drawWayPointEdgeDists = true;
                    }
                    else if (s.equalsIgnoreCase("-drawAngles"))
                        drawAngles = true;
                    else if (s.equalsIgnoreCase("-drawPodAngle"))
                        drawPodAngle = true;
                    else if (s.equalsIgnoreCase("-consecutiveSeeds"))
                        seedOrder = true;
                    else if (s.equalsIgnoreCase("-drawTreasureGauge"))
                        drawTreasureGauge = true;
                    else if (s.equalsIgnoreCase("-drawNoPlants"))
                        drawNoPlants = true;
                    else if (s.equalsIgnoreCase("-drawSpawnOrder"))
                        drawSpawnOrder = true;
                    else if (s.equalsIgnoreCase("-drawNoFallType"))
                        drawNoFallType = true;
                    else if (s.equalsIgnoreCase("-drawNoObjects"))
                        drawNoObjects = true;
                    else if (s.equalsIgnoreCase("-drawNoWaterBox"))
                        drawWaterBox = false;
                    else if (s.equalsIgnoreCase("-drawNoBuriedItems"))
                        drawNoBuriedItems = true;
                    else if (s.equalsIgnoreCase("-drawNoItems"))
                        drawNoItems = true;
                    else if (s.equalsIgnoreCase("-drawNoTekis"))
                        drawNoTeki = true;
                    else if (s.equalsIgnoreCase("-drawNoGates"))
                        drawNoGates = true;
                    else if (s.equalsIgnoreCase("-drawNoOnions"))
                        drawNoGates = true;
                    else if (s.equalsIgnoreCase("-drawNoHoles"))
                        drawNoHoles = true;
                    else if (s.equalsIgnoreCase("-quickglance"))
                        drawQuickGlance = true;
                    else if (s.equalsIgnoreCase("-caveInfoReport"))
                        showCaveInfo = true;
                    else if (s.equalsIgnoreCase("-drawNoGateLife"))
                        drawNoGateLife = true;
                    else if (s.equalsIgnoreCase("-drawSH6Bulborb"))
                        drawSH6Bulborb = true;
                    else if (s.equalsIgnoreCase("-drawFlowPaths"))
                        drawFlowPaths = true;
                    else if (s.equalsIgnoreCase("-drawTreasurePaths"))
                        drawTreasurePaths = true;
                    else if (s.equalsIgnoreCase("-drawAllPaths"))
                        drawAllPaths = true;
                    else if (s.equalsIgnoreCase("-drawHoleProbs"))
                        drawHoleProbs = true;
                    else if (s.equalsIgnoreCase("-findGoodLayouts")) {
                        findGoodLayouts = true;
                        findGoodLayoutsRatio = Double.parseDouble(args[++i]);
                    }
                    else if (s.equalsIgnoreCase("-requireMapUnits")) {
                        requireMapUnits = true;
                        requireMapUnitsConfig = args[++i];
                        Parser.parseShortCircuitString();
                    }
                    else if (s.equalsIgnoreCase("-noWayPointGraph"))
                        noWayPointGraph = true;
                    else if (s.equalsIgnoreCase("-memo") || s.equalsIgnoreCase("-writeMemo"))
                        writeMemo = true;
                    else if (s.equalsIgnoreCase("-readMemo")) {
                        readMemo = true;
                        writeMemo = false;
                    }
                    else if (s.equalsIgnoreCase("-agg") || s.equalsIgnoreCase("-aggregator")) {
                        aggregator = true;
                    }
                    else if (s.equalsIgnoreCase("-aggFirst")) {
                        aggFirst = true;
                        aggregator = true;
                    }
                    else if (s.equalsIgnoreCase("-aggRooms")) {
                        aggRooms = true;
                        aggregator = true;
                    }
                    else if (s.equalsIgnoreCase("-aggHalls")) {
                        aggHalls = true;
                        aggregator = true;
                    }
                    else if (s.equalsIgnoreCase("-count")) {
                        countObject = args[++i].toLowerCase();
                    }
                    else if (s.equalsIgnoreCase("-seedFile")) {
                        seedFile = args[++i];
                    }
                    else if (s.equalsIgnoreCase("-rotate")) {
                        rotateForDraw = args[++i];
                    }
                    else if (s.equalsIgnoreCase("-judge")) {
                        judgeActive = true;
                        while (i < args.length-1) {
                            i++;
                            if (args[i].equalsIgnoreCase("combine")) {
                                judgeCombine = true;
                            } else if (args[i].equalsIgnoreCase("rankfile")) {
                                judgeRankFile = true;
                            } else if (args[i].equalsIgnoreCase("vsavg")) {
                                judgeVsAvg = true;
                            } else if (args[i].equalsIgnoreCase("pod")) {
                                judgeType = "pod";
                            } else if (args[i].equalsIgnoreCase("colossal")) {
                                judgeType = "colossal";
                            }else if (args[i].equalsIgnoreCase("at")) {
                                judgeType = "at";
                            } else if (args[i].equalsIgnoreCase("key")) {
                                judgeType = "key";
                            } else if (args[i].equalsIgnoreCase("cmat")) {
                                judgeType = "cmat";
                            } else if (args[i].equalsIgnoreCase("score")) {
                                judgeType = "score";
                            }  else if (args[i].equalsIgnoreCase("attk") || args[i].equalsIgnoreCase("sa") || args[i].equalsIgnoreCase("scoreattack")) {
                                judgeType = "attk";
                            } else if (args[i].charAt(0) == '>' || args[i].charAt(0) == '<') {
                                int l = args[i].length();
                                if (args[i].charAt(l-1) == '%')
                                    judgeFilterRank = (args[i].charAt(0) == '>' ? 1 : -1) * Double.parseDouble(args[i].substring(1,l-1));
                                else {
                                    judgeFilterScore = Double.parseDouble(args[i].substring(1));
                                    judgeFilterScoreSign =  (args[i].charAt(0) == '>' ? 1 : -1);
                                }
                            }  else if (args[i].contains("-")) {
                                i--;
                                break;
                            } else {
                                System.out.println("Bad judge argument: " + args[i]);
                                throw new Exception();
                            }
                        }
                        if (judgeType.equals("attk")) {
                            judgeFilterScore *= -1;
                            judgeFilterScoreSign *= -1;
                        }
                    }
                    else if (s.equalsIgnoreCase("-judgefilter")) {
                        i++;
                        int l = args[i].length();
                        if (args[i].charAt(l-1) == '%')
                            judgeFilterRank = (args[i].charAt(0) == '>' ? 1 : -1) * Double.parseDouble(args[i].substring(1,l-1));
                        else {
                            judgeFilterScore = Double.parseDouble(args[i].substring(1));
                            judgeFilterScoreSign =  (args[i].charAt(0) == '>' ? 1 : -1);
                        }
                    }
                    else {
                        System.out.println("Bad argument: " + s);
                        throw new Exception();
                    }
                }

                if (judgeActive && judgeType.equals("default"))
                    throw new Exception();

                if (caveArg.equalsIgnoreCase("colossal")) {
                    fileSystem = "colossal";
                    caveArg = "colossal";
                    colossal = true;
                }

                caveInfoName = Parser.fromSpecial(caveArg);
                fileSystem = fileSystem.toLowerCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Parser.helpText();

            for (String s: Parser.helpText)
                System.out.println(s);
            
            if (CaveViewer.active) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                CaveViewer.caveViewer.reportBuffer.append(errors.toString());
                CaveViewer.caveViewer.update();
                return;
            } else {
                System.exit(0);
            }
        }

        drawer = new Drawer();
        stats = new Stats(args);
        memo = new Memo();
        Parser.readConfigFiles();

        if (expectTest) {
            memo.setupExpectTests();
            allStoryMode = true;
            allChallengeMode = true;
            caveArg = "both";
        }

        if (seedFile.equals("")) {
            int maxStory = p251 ? 16 : 14;
            if (allStoryMode) {
                int upperBound = caveArg.equals("pod") ? 9 : maxStory;
                for (int j = 0; j < upperBound; j++) {
                    caveInfoName = Parser.all[j] + ".txt";
                    for (int k = 0; k < 10000; k++) {
                        sublevel = k+1;
                        CaveGen g = new CaveGen();
                        if (g.isFinalFloor) break;
                    }
                }
            }
            if (allChallengeMode) {
                for (int j = maxStory; j < maxStory+30; j++) {
                    caveInfoName = Parser.all[j] + ".txt";
                    for (int k = 0; k < 10000; k++) {
                        sublevel = k+1;
                        CaveGen g = new CaveGen();
                        if (g.isFinalFloor) break;
                    }
                }
            }
            if (!allChallengeMode && !allStoryMode) {
                int sublevelArg = sublevel;
                String[] caveStrings = caveArg.split(",");
                for (String cs: caveStrings) {
                    if (cs.contains("-")) {
                        String[] csSplit = cs.split("-");
                        caveInfoName = Parser.fromSpecial(csSplit[0]);
                        sublevel = Integer.parseInt(csSplit[1]);
                        new CaveGen();
                    }
                    else {
                        caveInfoName = Parser.fromSpecial(cs);
                        if (sublevelArg == 0) {
                            for (int i = 0; i < 10000; i++) {
                                sublevel = i+1;
                                CaveGen g = new CaveGen();
                                if (g.isFinalFloor) break;
                            }
                        }
                        else {
                            sublevel = sublevelArg;
                            new CaveGen();
                        }
                    }
                }
            }
        } else { // user provided a list of sublevel/seed pairs.
            ArrayList<String> seedArgs = Parser.parseSeedFile(seedFile);
            for (int i = 0; i < seedArgs.size(); i += 3) {
                caveInfoName = Parser.fromSpecial(seedArgs.get(i));
                sublevel = Integer.parseInt(seedArgs.get(i+1));
                firstGenSeed = (int)(Long.decode("0x"+seedArgs.get(i+2)).longValue());
                new CaveGen();
            }
        }

        if (showStats) {
            stats.createReport();
        }
        if (expectTest) {
            memo.checkExpectation();
        }
        if (CaveViewer.active) {
            CaveViewer.caveViewer.update();
        }
    }

    public CaveGen() {

        specialCaveInfoName = Parser.toSpecial(caveInfoName);
        if (storyModeOverride)
            hardMode = true;
        else if (challengeModeOverride)
            hardMode = false;
        else {
            hardMode = !specialCaveInfoName.substring(0,2).equalsIgnoreCase("CH");
        }
        challengeMode = !hardMode;

        new Parser().parseAll(this); // Parse everything
        if (isFinalFloor) holeClogged = !isHardMode(); // final floor geysers aren't clogged in story mode

        if (aggregator) {
            Aggregator.reset();
        }
        if (judgeActive) {
            stats.judge.setupJudge(this);
        }

        for (int i = 0; i < numToGenerate; i++) {
            indexBeingGenerated = i;
            if (seedOrder) {
                initialSeed = (int)seedCalc.next_seed(firstGenSeed, i);
            }
            else {
                initialSeed = firstGenSeed + i;
            }
            seed = initialSeed;

            if (prints && (numToGenerate < 4096 && !CaveGen.judgeActive || initialSeed % 4096 == 0)) {
                System.out.println("Generating " + specialCaveInfoName + "-" + sublevel + " on seed " + Drawer.seedToString(initialSeed));
                if (CaveViewer.active) {
                    CaveViewer.caveViewer.reportBuffer.append("Generating " + specialCaveInfoName + "-" + sublevel + " on seed " + Drawer.seedToString(initialSeed) + "\n");
                }
            }

            reset();

            if (readMemo) {
                memo.readMemo(this);
            } else {
                createRandomMap();
            }

            if (showStats && !shortCircuitMap) {
                try {
                    stats.analyze(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            if (images && !shortCircuitMap && imageToggle) {
                try {
                    drawer.draw(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            if (expectTest) {
                memo.outputSublevelForExpect(this);
            }
            if (writeMemo) {
                memo.writeMemo(this);
            }
            if (aggregator) {
                Aggregator.process(this);
            }
            if (CaveViewer.active) {
                CaveViewer.caveViewer.update();
            }
        }
        if (showCaveInfo) {
            try {
                drawer.drawCaveInfo(this);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if (aggregator) {
            try {
                drawer.drawAggregator(this);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if (judgeActive && !judgeCombine) {
            stats.judge.printSortedList();
        }
    }

    void reset() {
        // reset parameters
        queueCap = new LinkedList<MapUnit>();
        queueRoom = new LinkedList<MapUnit>();
        queueCorridor = new LinkedList<MapUnit>();
        placedMapUnits = new ArrayList<MapUnit>();
        placedTekis = new ArrayList<Teki>();
        placedItems = new ArrayList<Item>();
        placedGates = new ArrayList<Gate>();
        placedOnions = new ArrayList<Onion>();
        placedStart = null;
        placedHole = null;
        placedGeyser = null;
        openDoors = new ArrayList<Door>();
        maxTeki0 = minTeki0 = maxTeki1 = maxTeki5 = maxTeki8 = 0;
        mapHasDiameter36 = false;
        markedOpenDoorsAsCaps = false;
        mapMaxX = mapMaxZ = mapMinX = mapMinZ = 0;
        shortCircuitMap = false;
    }

    // ---------------------------------------------------------------------------
    // SPAWNING ALGORITHM STARTS BELOW
    // ---------------------------------------------------------------------------

    final static int INF = Integer.MAX_VALUE;
    // Spawnpoint types:
    // 0: easy enemy, 1: hard enemy, 2: item, 4: hole/geyser,
    // 5: door, 6: plant, 7: start, 8: special enemy, 9: alcove
    // Unit types:
    // 0: alcove, 1: room, 2: corridor
    // Fall types:
    // 0: Not falling, 1: Captain/Pikmin near, 2: Pikmin near,
    // 3: Leader near, 4: Carrying near, 5: Purple pound near

    int seed;
    int initialSeed;

    // FloorInfo parameters
    int maxMainTeki, maxItem, maxGate, maxRoom;
    float corridorProb, capProb;
    boolean hasGeyser, holeClogged, hasFloorPlane, allowCapSpawns, hasSeesaw;
    String caveUnitFile, lightingFile, skyboxFile;
    int echoStrength, musicType;
    float waterwraithTimer;
    boolean isFinalFloor;
    int maxNumDoorsSingleUnit;

    // Lists to spawn from (comes from units file, TekiInfo, ItemInfo, GateInfo, CapInfo)
    ArrayList<MapUnit> spawnMapUnits;
    ArrayList<MapUnit> spawnMapUnitsSorted;
    ArrayList<MapUnit> spawnMapUnitsSortedAndRotated;
    ArrayList<Teki> spawnTeki0;
    ArrayList<Teki> spawnTeki1;
    ArrayList<Teki> spawnTeki5;
    ArrayList<Teki> spawnTeki8; 
    ArrayList<Teki> spawnTeki6; 
    ArrayList<Item> spawnItem;
    ArrayList<Gate> spawnGate;
    ArrayList<Teki> spawnCapTeki;
    ArrayList<Teki> spawnCapFallingTeki;
    ArrayList<Teki> spawnTekiConsolidated;

    // Queues of map units
    LinkedList<MapUnit> queueCap;
    LinkedList<MapUnit> queueRoom;
    LinkedList<MapUnit> queueCorridor;

    // Lists of placed objects
    ArrayList<MapUnit> placedMapUnits;
    ArrayList<Teki> placedTekis;
    ArrayList<Item> placedItems;
    ArrayList<Gate> placedGates;
    SpawnPoint placedStart;
    SpawnPoint placedHole;
    SpawnPoint placedGeyser;
    ArrayList<Onion> placedOnions;

    // Other helper variables
    ArrayList<Door> openDoors;
    int maxTeki0, minTeki0, maxTeki1, maxTeki5, maxTeki8;
    boolean mapHasDiameter36;
    int mapMaxX, mapMaxZ, mapMinX, mapMinZ;
    boolean markedOpenDoorsAsCaps;

    void createRandomMap() {
        mapUnitsInitialSorting();
        allocateEnemySlots();

        // Main logic for generating the map units
        setFirstMapUnit();
        if (openDoors.size() > 0) {
            int numLoops = 0;
            while (++numLoops <= 10000) {
                if (shortCircuitMap) return;
                boolean placed = false;
                if (countPlacedType(1) < maxRoom) {
                    placed = getNormalRandMapUnit();
                } else {
                    markOpenDoorsAsCaps();
                    placed = getHallwayRandMapUnit();
                    if (!placed) {
                        placed = getCapRandMapUnit();
                    }
                }

                if (openDoors.size() > 0) continue;
                changeCapToHallMapUnit();

                if (openDoors.size() > 0) continue;
                changeTwoToOneMapUnit();
                break;
            }
        }

        // add spawnpoints for doors (type 5), item alcoves (type 9)
        // and corridors (type 9)
        recomputeOffset();
        addSpawnPoints(); 

        // logic for spawning teki, gate, and item objects
        setStart();
        setScore();
        if (!isFinalFloor) placedHole = setHole();
        if (isFinalFloor || hasGeyser) placedGeyser = setHole();
        setEnemy5();
        setEnemy8();
        setEnemy1();
        setEnemy0();
        setScore();
        setPlant();
        setItem();
        setCapEnemy();
        //setScore(); // pretty sure this call doesn't actually change any scores
        setGate();

        if (colossal)
            placeOnionsColossal();

        if (!noWayPointGraph) {
            buildWayPointGraph();
        }
    }
    
    // Generate Map Units ---------------------------------------------------------------------

    void mapUnitsInitialSorting() {
        sortAndRotateMapUnits();

        for (MapUnit m: spawnMapUnitsSortedAndRotated) {
            switch(m.type) {
            case 0: queueCap.add(m); break;
            case 1: queueRoom.add(m); break;
            case 2: queueCorridor.add(m); break;
            }
        }

        randBacks(queueCap);
        randBacks(queueRoom);
        randBacks(queueCorridor);
    }

    void sortAndRotateMapUnits() {
        if (spawnMapUnitsSorted.size() < spawnMapUnits.size()) {
            for (MapUnit m: spawnMapUnits)
                spawnMapUnitsSorted.add(m);
            sortBySizeAndDoors(spawnMapUnitsSorted);
            for (int i = 0; i < spawnMapUnitsSorted.size(); i++)
                spawnMapUnitsSorted.get(i).spawnListIdx = i;
            for (MapUnit m: spawnMapUnitsSorted) {
                for (int i = 0; i < 4; i++) {
                    spawnMapUnitsSortedAndRotated.add(m.rotate(i));
                }
            }
        }
    }

    void sortBySizeAndDoors(List<MapUnit> queueMapUnits) {
        // sort by smallest unit size, ties by least num doors
        // Note, this is not a stable sort!
        for (int i = 0; i < queueMapUnits.size(); i++) {
            for (int j = i+1; j < queueMapUnits.size(); j++) {
                MapUnit m1 = queueMapUnits.get(i);
                MapUnit m2 = queueMapUnits.get(j);
                int sz1 = m1.dX * m1.dZ;
                int sz2 = m2.dX * m2.dZ;
                if (sz1 > sz2 || (sz1 == sz2 && m1.numDoors > m2.numDoors)) {
                    queueMapUnits.add(queueMapUnits.remove(i));
                    i--;
                    break;
                }
            }
        }
    }

    void allocateEnemySlots() {
        int[] allocatedSlots0158 = new int[10];
        int[] weightSum0158 = new int[10];
        int numSlotsUsedForMin = 0;
        for (Teki t: spawnTeki0) {
            allocatedSlots0158[t.type] += t.min;
            numSlotsUsedForMin += t.min;
            weightSum0158[t.type] += t.weight;
        }
        for (Teki t: spawnTeki1) {
            allocatedSlots0158[t.type] += t.min;
            numSlotsUsedForMin += t.min;
            weightSum0158[t.type] += t.weight;
        }
        for (Teki t: spawnTeki5) {
            allocatedSlots0158[t.type] += t.min;
            numSlotsUsedForMin += t.min;
            weightSum0158[t.type] += t.weight;
        }
        for (Teki t: spawnTeki8) {
            allocatedSlots0158[t.type] += t.min;
            numSlotsUsedForMin += t.min;
            weightSum0158[t.type] += t.weight;
        }
        minTeki0 = allocatedSlots0158[0];
        // extra main teki slots are allocated randomly between types 0 1 5 8
        ArrayList<Integer> weights = new ArrayList<Integer>();
        for (int w: weightSum0158) 
            weights.add(w);
        for (int i = 0; i < maxMainTeki - numSlotsUsedForMin; i++) {
            int idx = randIndexWeight(weights);
            if (idx == -1) continue; // No Teki with weight
            allocatedSlots0158[idx] += 1;
        }
        maxTeki0 = allocatedSlots0158[0];
        maxTeki1 = allocatedSlots0158[1];
        maxTeki5 = allocatedSlots0158[5];
        maxTeki8 = allocatedSlots0158[8];
    }
    
    void setFirstMapUnit() {
        // pick first room in queue that has a start spawnpoint
        for (MapUnit m: queueRoom) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type == 7) {
                    m.offsetX = 0;
                    m.offsetZ = 0;
                    addMapUnit(m, true);
                    return;
                }
            }
        }
        assert false;
    }

    void addMapUnit(MapUnit m, boolean doChecks) {
        MapUnit mPlaced = m.copy();
        mPlaced.offsetX = m.offsetX;
        mPlaced.offsetZ = m.offsetZ;
        placedMapUnits.add(mPlaced);
        if (doChecks) {
            closeDoorCheck(mPlaced);
            recomputeMapSize(mPlaced);
            shuffleMapPriority(mPlaced);
        }
        if (requireMapUnits) {
            shortCircuitMap = memo.checkForShortCircuit(this);
        }
    }

    void closeDoorCheck(MapUnit m1) {
        // "close" doors that are facing each other.
        for (Door d1: m1.doors) {
            for (Door d2: openDoors) {
                MapUnit m2 = d2.mapUnit;
                m1.doorOffset(d1);
                m2.doorOffset(d2);
                if (Door.doorDirsMatch(d1,d2) && d1.offsetX == d2.offsetX &&
                    d1.offsetZ == d2.offsetZ) {
                    d1.adjacentDoor = d2;
                    d2.adjacentDoor = d1;
                }
            }
        }
        recomputeOpenDoors();
    }

    void recomputeMapSize(MapUnit mostRecent) {
        mapMinX = Math.min(mapMinX, mostRecent.offsetX);
        mapMinZ = Math.min(mapMinZ, mostRecent.offsetZ);
        mapMaxX = Math.max(mapMaxX, mostRecent.offsetX + mostRecent.dX);
        mapMaxZ = Math.max(mapMaxZ, mostRecent.offsetZ + mostRecent.dZ);
        mapHasDiameter36 = mapMaxX-mapMinX >= 36 || mapMaxZ-mapMinZ >= 36;
    }

    void recomputeOffset() {
        // recenter the map such that all offsets are >= 0
        int minX = INF, minZ = INF, maxX = -INF, maxZ = -INF;
        for (MapUnit m: placedMapUnits) {
            minX = Math.min(minX, m.offsetX);
            minZ = Math.min(minZ, m.offsetZ);
            maxX = Math.max(maxX, m.offsetX + m.dX);
            maxZ = Math.max(maxZ, m.offsetZ + m.dZ);
        }
        for (MapUnit m: placedMapUnits) {
            m.offsetX = m.offsetX - minX;
            m.offsetZ = m.offsetZ - minZ;
        }
        mapMinX = mapMinZ = 0;
        mapMaxX = maxX-minX;
        mapMaxZ = maxZ-minZ;
        mapHasDiameter36 = maxX-minX >= 36 || maxZ-minZ >= 36;
        for (int i = 0; i < placedMapUnits.size(); i++)
            placedMapUnits.get(i).placedListIdx = i;
    }

    void shuffleMapPriority(MapUnit mostRecentlyPlaced) {
        if (mostRecentlyPlaced.type == 0) {
            randBacks(queueCap);
            return;
        }
        if (mostRecentlyPlaced.type == 2) {
            randBacks(queueCorridor);
            return;
        }
        // otherwise, for rooms...
        ArrayList<Integer> roomCounts = new ArrayList<Integer>();
        ArrayList<String> roomNames = new ArrayList<String>();

        for (MapUnit m: placedMapUnits) {
            if (m.type != 1) continue;
            int idx = roomNames.indexOf(m.name);
            if (idx == -1) {
                roomNames.add(m.name);
                roomCounts.add(1);
            }
            else {
                roomCounts.set(idx, roomCounts.get(idx) + 1);
            }
        }
        // sort the room names by room counts
        for (int i = 0; i < roomCounts.size() - 1; i++) {
            for (int j = i+1; j < roomCounts.size(); j++) {
                if (roomCounts.get(i) > roomCounts.get(j)) {
                    Integer temp = roomCounts.get(i);
                    String tempS = roomNames.get(i);
                    roomCounts.set(i, roomCounts.get(j));
                    roomNames.set(i, roomNames.get(j));
                    roomCounts.set(j, temp);
                    roomNames.set(j, tempS);
                }
            }
        }

        // update the queue so that rooms that have appeared
        // less often are at the front of the queue
        for (int i = 0; i < roomNames.size(); i++) {
            String s = roomNames.get(i);
            ArrayList<MapUnit> ms = new ArrayList<MapUnit>();

            for (int j = 0; j < queueRoom.size(); j++) {
                MapUnit m = queueRoom.get(j);
                if (m.name.equals(s)) {
                    queueRoom.remove(j);
                    j--;
                    ms.add(m);
                }
            }

            randBacks(ms);

            for (MapUnit m: ms) {
                queueRoom.add(m);
            }
        }

    }

    void setMapUnitOffset(Door expandDoor, MapUnit m, Door d) {
        // set the offset coordinates of a new map unit
        expandDoor.doorOffset();
        m.offsetX = expandDoor.offsetX;
        m.offsetZ = expandDoor.offsetZ;
        switch(d.dirSide) {
        case 0: m.offsetX -= d.offsetSide; break;
        case 1: m.offsetX -= m.dX; m.offsetZ -= d.offsetSide; break;
        case 2: m.offsetX -= d.offsetSide; m.offsetZ -= m.dZ; break;
        case 3: m.offsetZ -= d.offsetSide; break;
        }
    }

    boolean fitsOnCurrentMap(MapUnit m) {
        // map unit overlaps map unit
        for (MapUnit me: placedMapUnits) {
            if (isInnerBox(m.offsetX, m.offsetZ, m.dX, m.dZ,
                           me.offsetX, me.offsetZ, me.dX, me.dZ))
                return false;
        }
        // new door either matches with an existing open door,
        // or it has an open space in front of it.
        for (Door d: m.doors) {
            boolean matchingDoor = false;
            d.doorOffset();
            for (Door de: openDoors) {
                de.doorOffset();
                if (d.offsetX == de.offsetX && d.offsetZ == de.offsetZ && Door.doorDirsMatch(d,de))
                    matchingDoor = true;
            }
            if (!matchingDoor) {
                int openSpaceX = d.offsetX + (d.dirSide == 3 ? -1 : 0);
                int openSpaceZ = d.offsetZ + (d.dirSide == 0 ? -1 : 0);
                for (MapUnit me: placedMapUnits) {
                    if (isInnerBox(openSpaceX, openSpaceZ, 1, 1,
                                   me.offsetX, me.offsetZ, me.dX, me.dZ))
                        return false;
                }
            }
        }
        // existing open doors either match with a new door,
        // or have open space in front of them
        for (Door de: openDoors) {
            boolean matchingDoor = false;
            de.doorOffset();
            for (Door d: m.doors) {
                d.doorOffset();
                if (d.offsetX == de.offsetX && d.offsetZ == de.offsetZ && Door.doorDirsMatch(d,de))
                    matchingDoor = true;
            }
            if (!matchingDoor) {
                int openSpaceX = de.offsetX + (de.dirSide == 3 ? -1 : 0);
                int openSpaceZ = de.offsetZ + (de.dirSide == 0 ? -1 : 0);
                if (isInnerBox(openSpaceX, openSpaceZ, 1, 1,
                                   m.offsetX, m.offsetZ, m.dX, m.dZ))
                    return false;
            }
        }
        return true;
    }

    boolean tryAddMapUnit(Door expandDoor, MapUnit m, Door d) {
        // test if this mapunit can be added at expandDoor
        if (!Door.doorDirsMatch(expandDoor, d)) return false;
        setMapUnitOffset(expandDoor, m, d);
        return fitsOnCurrentMap(m);
    }

    boolean getNormalRandMapUnit() {
        // During this phase, the game tries to build out the map
        // using rooms and branching corridors

        // Choose a uniform random open door to expand from
        int openDoorCount = openDoors.size();
        int expandDoorIdx = randInt(openDoorCount);
        Door expandDoor = openDoors.get(expandDoorIdx);
        MapUnit expandMapUnit = expandDoor.mapUnit;

        // choose the priority order to try spawning units
        // using the variable "corridorProb"
        int[] kindPriority;
        float corridorProb = this.corridorProb;
        if (mapHasDiameter36) corridorProb = 0;
        if (expandMapUnit.type == 1) corridorProb *= 2;
        if (randFloat() < corridorProb) kindPriority = new int[] {2, 1, 0}; // corridor first
        else kindPriority = new int[] {1, 2, 0}; // room first

        for (int type: kindPriority) {
            // create the priority list for corridor units
            if (type == 2) {
                shuffleCorridorPriority();
            }

            // for each map unit in the appropriate queue
            // try to place that map unit at the door we chose
            // to extend from, trying all possible doors for the
            // new map unit in a random order
            LinkedList<MapUnit> queue = type == 0 ? queueCap
                : (type == 1 ? queueRoom : queueCorridor);
            for (MapUnit m: queue) {
                // choose the door priority
                ArrayList<Integer> doorPriority = new ArrayList<Integer>();
                for (int i = 0; i < m.numDoors; i++)
                    doorPriority.add(i);
                randSwaps(doorPriority);

                for (int doorIdx: doorPriority) {
                    Door d = m.doors.get(doorIdx);

                    if (tryAddMapUnit(expandDoor, m, d)) {
                        addMapUnit(m, true);
                        return true;
                    }
                }
            }
        }

        return false;
    }
  
    void shuffleCorridorPriority() {
        // This sorts the priority queue for corridors during
        // the "Normal" phase.

        int M = maxNumDoorsSingleUnit;
        int openDoorCount = openDoors.size();
        ArrayList<Integer> corridorPriority = new ArrayList<Integer>();
        
        // If there are only a few open doors, prioritize corridors
        // with a lot of doors (e.g. 4-ways)
        if (openDoorCount < 4) {
            for (int i = 0; i < M; i++)
                corridorPriority.add(M-i);
        }
        // If there are a lot of open doors, prioritize corridors
        // with few doors (e.g. hallways)
        else if (openDoorCount >= 10) {
            for (int i = 0; i < M; i++) 
                corridorPriority.add(i+1);
        }
        // Otherwise, prioirize corridors randomly
        else {
            for (int i = 0; i < M; i++)
                corridorPriority.add(i+1);
            randSwaps(corridorPriority);
        }
        // Update the queue to be sorted by the num-door prioirity
        for (int numDoors: corridorPriority) {
            ArrayList<MapUnit> c = new ArrayList<MapUnit>(); 
            for (int i = 0; i < queueCorridor.size(); i++) {
                if (queueCorridor.get(i).numDoors == numDoors) {
                    c.add(queueCorridor.remove(i));
                    i--;
                }
            }
            for (MapUnit m: c) {
                queueCorridor.add(m);
            }
        }
    }

    void markOpenDoorsAsCaps() {
        // After the "Normal" phase is done, we randomly choose some open doors
        // to be marked as cap. This means that we will not try to build
        // a hallway from this door. However, we still might build some
        // other hallway into this door.
        if (markedOpenDoorsAsCaps) return;
        markedOpenDoorsAsCaps = true;

        int numMarked = 0;
        int maxNumMarked = 16; // only mark 16 max
        for (Door d: openDoors) {
            if (randFloat() < capProb) { // use "capProb"
                d.markedAsCap = true;
                numMarked++;
                if (numMarked >= maxNumMarked) break;
            }
        }
    }

    boolean getHallwayRandMapUnit() {
        // During this phase of the algorithm, the game tries to spawn
        // hallways to connect nearby open doors.

        // create a random list of hallways (corridors with 2 doors)
        ArrayList<MapUnit> queueHallway = new ArrayList<MapUnit>();
        for (MapUnit m: queueCorridor) {
            if (m.dX == 1 && m.dZ == 1 && m.numDoors == 2)
                queueHallway.add(m);
        }
        randSwaps(queueHallway);

        for (Door d: openDoors) {
            if (d.markedAsCap) continue;

            // find the closest linkable door to this door
            Door cl = null;
            int clDist = INF;
            d.doorOffset();
            for (Door od: openDoors) {
                // Note, the game that it doesn't check
                // that the receiving door is marked as cap

                // check if od is linkable with d
                // meaning it's in a 19x10 rectangle in front of door d.
                if (od.mapUnit == d.mapUnit) continue;
                od.doorOffset();
                int dx = od.offsetX - d.offsetX;
                int dz = od.offsetZ - d.offsetZ;
                if (Math.abs(dx) >= 10 || Math.abs(dz) >= 10) continue;
                if (d.dirSide == 0 && dz > 0) continue;
                if (d.dirSide == 1 && dx < 0) continue;
                if (d.dirSide == 2 && dz < 0) continue;
                if (d.dirSide == 3 && dx > 0) continue;
                // if at this point, the doors are "linkable"

                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist < clDist) {
                    cl = od;
                    clDist = dist;
                }
            }
            if (cl == null) continue; // nothing linkable found

            // complicated formula to determine the directions
            // to try constructing the hallway in
            // This logic is responsible for the "snaking" corridors
            cl.doorOffset();
            int dx = cl.offsetX - d.offsetX;
            int dz = cl.offsetZ - d.offsetZ;
            int cld = cl.dirSide;
            int[] dirPriority = new int[2];
            int a = -1;
            if (d.dirSide == 0) {
                if (dz > -2) a = dx >= 0 ? 1 : 3;
                else if (dx < -1) a = 3;
                else if (dx == -1) a = cld == 2 || cld == 3 ? 3 : 0;
                else if (dx == 0) a = cld == 0 || cld == 3 ? 3 : 0;
                else if (dx == 1) a = cld == 1 || cld == 2 ? 1 : 0;
                else if (dx > 1) a = 1;
            }
            if (d.dirSide == 1) {
                if (dx == 0) a = dz > 0 ? 2 : 0;
                else if (dz < -1) a = 0;
                else if (dz == -1) a = cld == 0 || cld == 3 ? 0 : 1;
                else if (dz == 0) a = cld == 0 || cld == 1 ? 0 : 1;
                else if (dz == 1) a = cld == 2 || cld == 3 ? 2 : 1;
                else if (dz > 1) a = 2;
            }
            if (d.dirSide == 2) {
                if (dz == 0) a = dx > 0 ? 1 : 3;
                else if (dx < -1) a = 3;
                else if (dx == -1) a = cld == 0 || cld == 3 ? 3 : 2;
                else if (dx == 0) a = cld == 2 || cld == 3 ? 3 : 2;
                else if (dx == 1) a = cld == 0 || cld == 1 ? 1 : 2;
                else if (dx > 1) a = 1;
            }
            if (d.dirSide == 3) {
                if (dx > -2) a = dz > 0 ? 2 : 0;
                else if (dz < -1) a = 0;
                else if (dz == -1) a = cld == 0 || cld == 1 ? 0 : 3;
                else if (dz == 0) a = cld == 0 || cld == 3 ? 0 : 3;
                else if (dz == 1) a = cld == 1 || cld == 2 ? 2 : 3;
                else if (dz > 1) a = 2;
            } 
            dirPriority[0] = a;
            // if the complicated formula fails, we try a straight hallway
            dirPriority[1] = d.dirSide; 

            // try placing a hallway with the desired shape
            // into the map, taking the first thing that fits
            int dirHallway0 = (d.dirSide + 2) % 4;
            for (int dirHallway1: dirPriority) {
                for (MapUnit h: queueHallway) {
                    int dir0 = h.doors.get(0).dirSide;
                    int dir1 = h.doors.get(1).dirSide;
                    if (dir0 == dirHallway0 && dir1 == dirHallway1) {
                        if (tryAddMapUnit(d, h, h.doors.get(0))) {
                            addMapUnit(h, true);
                            return true;
                        }
                    } else if (dir0 == dirHallway1 && dir1 == dirHallway0) {
                        if (tryAddMapUnit(d, h, h.doors.get(1))) {
                            addMapUnit(h, true);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean getCapRandMapUnit() {
        // During this phase, we try to close off open doors.
        // for each open door, try to spawn first an alcove,
        // then a corridor, then a room until one fits.
        // For all intents and purposes, a cap or corridor should always spawn.
        for (Door expandDoor: openDoors) {
            for (int type: new int[] {0, 2, 1}) {
                LinkedList<MapUnit> queue = type == 0 ? queueCap
                    : (type == 1 ? queueRoom : queueCorridor);
                for (int numDoors = 1; numDoors <= maxNumDoorsSingleUnit; numDoors++) {
                    for (MapUnit m: queue) {
                        if (numDoors != m.numDoors) continue;

                        ArrayList<Integer> doorPriority = new ArrayList<Integer>();
                        for (int i = 0; i < numDoors; i++)
                            doorPriority.add(i);
                        randSwaps(doorPriority);

                        for (int doorIdx: doorPriority) {
                            Door d = m.doors.get(doorIdx);
                            if (tryAddMapUnit(expandDoor, m, d)) {
                                addMapUnit(m, true);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean changeCapToHallMapUnit() {
        // During this phase, we change all alcoves with a corridor directly
        // behind it to a straight hallway.

        // create a list of 1x1 straight hallway names
        ArrayList<String> hallwayNames = new ArrayList<String>();
        for (MapUnit m: queueCorridor) {
            if (m.dX == 1 && m.dZ == 1 && m.numDoors == 2
                && m.doors.get(0).dirSide == 0 && m.doors.get(1).dirSide == 2)
                hallwayNames.add(m.name);
        }
        if (hallwayNames.size() == 0) return false;

        for (int i = 0; i < placedMapUnits.size(); i++) {
            MapUnit m = placedMapUnits.get(i);
            if (m.type != 0) continue;

            // compute the coords of the space behind this alcove
            Door md = m.doors.get(0);
            int onePastX = m.offsetX;
            int onePastZ = m.offsetZ;
            switch(md.dirSide) {
            case 0: onePastZ += 1; break;
            case 1: onePastX -= 1; break;
            case 2: onePastZ -= 1; break;
            case 3: onePastX += 1; break;
            }

            // check for a corridor behind this alcove
            MapUnit corridorBehind = null;
            for (MapUnit c: placedMapUnits) {
                if (c.type == 2 && c != m && onePastX == c.offsetX && onePastZ == c.offsetZ) {
                    corridorBehind = c;
                    break;
                }
            }
            if (corridorBehind == null) continue;

            // delete the alcove and the corridor behind
            md.adjacentDoor.adjacentDoor = null;
            for (Door dd: corridorBehind.doors)
                dd.adjacentDoor.adjacentDoor = null;
            placedMapUnits.remove(m);
            placedMapUnits.remove(corridorBehind);
            recomputeOpenDoors();

            // add a hallway where the alcove was
            String nameChosen = hallwayNames.get(randInt(hallwayNames.size()));
            for (MapUnit c: queueCorridor) {
                Door cd = c.doors.get(0);
                if (c.name.equals(nameChosen) && cd.dirSide == md.dirSide) {
                    Door expandDoor = md.adjacentDoor;
                    if (tryAddMapUnit(expandDoor, c, cd)) {
                        addMapUnit(c, true);
                        return true;
                    }
                }
            }
            assert false;
        }
        return false;
    }

    boolean changeTwoToOneMapUnit() {
        // During this phase, we change two consecutive 1x1 hallways
        // to a single 2x1 hallway for all such instances

        // create a list of 1x1 straight hallway names
        ArrayList<String> hallwayNames1 = new ArrayList<String>();
        for (MapUnit m: queueCorridor) {
            if (m.dX == 1 && m.dZ == 1 && m.numDoors == 2
                && m.doors.get(0).dirSide == 0 && m.doors.get(1).dirSide == 2)
                hallwayNames1.add(m.name);
        }
        if (hallwayNames1.size() == 0) return false;

        // create a list of 1x2 straight hallway names
        ArrayList<String> hallwayNames2 = new ArrayList<String>();
        for (MapUnit m: queueCorridor) {
            if (m.dX == 1 && m.dZ == 2 && m.numDoors == 2
                && m.doors.get(0).dirSide == 0 && m.doors.get(1).dirSide == 2)
                hallwayNames2.add(m.name);
        }
        if (hallwayNames2.size() == 0) return false;

        boolean placed = false;
        for (int i = 0; i < placedMapUnits.size(); i++) {
            MapUnit m = placedMapUnits.get(i);
            if (!hallwayNames1.contains(m.name)) continue;

            // check for another 1x1 hallway next to this one
            MapUnit o = null;
            Door md = null;
            Door od = null;
            for (int j = 0; j < 2; j++) {
                md = m.doors.get(j);
                MapUnit adj = md.adjacentDoor.mapUnit;
                if (hallwayNames1.contains(adj.name)) {
                    o = adj;
                    od = md.adjacentDoor;
                    break;
                }
            }
            if (o == null) continue;

            // find the door to expand from
            Door expandDoor;
            if (m.offsetX > o.offsetX || m.offsetZ < o.offsetZ) {
                expandDoor = m.doors.get(md.doorLinks.get(0).otherIdx).adjacentDoor;
            } else {
                expandDoor = o.doors.get(od.doorLinks.get(0).otherIdx).adjacentDoor;
            }

            // delete the 1x1 hallways
            for (Door dd: m.doors)
                dd.adjacentDoor.adjacentDoor = null;
            for (Door dd: o.doors)
                if (dd.adjacentDoor != null)
                    dd.adjacentDoor.adjacentDoor = null;
            placedMapUnits.remove(m);
            placedMapUnits.remove(o);
            recomputeOpenDoors();

            // add a 2x1 hallway
            String nameChosen = hallwayNames2.get(randInt(hallwayNames2.size()));
            int desiredDir = m.offsetX == o.offsetX ? 0 : 1;
            for (MapUnit c: queueCorridor) {
                Door cd = c.doors.get(0);
                if (c.name.equals(nameChosen) && cd.dirSide == desiredDir) {
                    if (tryAddMapUnit(expandDoor, c, cd)) {
                        addMapUnit(c, true);
                        placed = true;
                        i = -1;
                        break;
                    }
                }
            }
            assert i == -1;
        }
        return placed;
    }

    void addSpawnPoints() {
        // add spawnpoints for doors, corridors, and item caps
        // (these aren't built into the game, but it makes it easier to think
        //  about it this way)
        // Corridors do have a special spawnpoint, but only the hole/geyser can spawn there
        for (MapUnit m: placedMapUnits) {
            for (Door d: m.doors) {
                if (d.spawnPoint == null) {
                    SpawnPoint sp = new SpawnPoint();
                    sp.type = 5;
                    sp.x = 0;
                    sp.y = 0;
                    sp.z = 0;
                    sp.angle = 0;
                    sp.radius = 0;
                    sp.minNum = 1;
                    sp.maxNum = 1;
                    sp.door = d;
                    d.spawnPoint = sp;
                    d.adjacentDoor.spawnPoint = sp;
                }
            }
            if ((m.type == 0 && itemInName(m.name)) || m.type == 2) {
                SpawnPoint sp = new SpawnPoint();
                sp.type = 9;
                sp.x = 0;
                sp.y = 0;
                sp.z = 0;
                sp.angle = 0;
                sp.radius = 0;
                sp.minNum = 1;
                sp.maxNum = 1;
                sp.mapUnit = m;
                sp.spawnListIdx = m.spawnPoints.size();
                m.spawnPoints.add(sp);
            }
        }

        // compute global positions for all spawnpoints, waypoints, and doors
        // which are fixed from this point on. 
        
        for (MapUnit m: placedMapUnits)
            m.recomputePos();
    }

    // Generate Objects ----------------------------------------------------------------------

    void setStart() {
        // pick start point uniform random from the first room placed.
        MapUnit first = null;
        for (MapUnit m: placedMapUnits) {
            if (m.type == 1) {
                first = m;
                break;
            }
        }

        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        for (SpawnPoint sp: first.spawnPoints) {
            if (sp.type == 7)
                sps.add(sp);
        }

        placedStart = sps.get(randInt(sps.size()));
        placedStart.filled = true;
    }

    void setScore() {
        // This function sets the "score" for each mapunit and door.
        // The score is used to spawn holes, items, and gates.
        // This is a very IMPORTANT function. It effectively determines the
        // set of locations where the holes, treasures, and gates can spawn on hard mode.

        // reset unit score to infinity, reset door score to inf
        for (MapUnit m: placedMapUnits) {
            m.enemyScore = 0;
            m.unitScore = INF;
            for (Door d: m.doors) {
                d.doorScore = INF;
                d.gateScore = 0;
            }
        }

        // set enemy scores
        setEnemyScores();

        // set score for the start map unit, and all adjacent map units.
        // For the start map unit, we see that the score is just the enemy score
        // For the adjacent ones, no distances are used, just enemy score + gate score + 1
        MapUnit first = placedStart.mapUnit;
        first.unitScore = first.enemyScore;
        for (Door d: first.doors) {
            d.doorScore = first.unitScore + 1 + d.gateScore;
            d.adjacentDoor.doorScore = d.doorScore;
            MapUnit adj = d.adjacentDoor.mapUnit;
            adj.unitScore = Math.min(d.doorScore + adj.enemyScore, adj.unitScore);
        }

        // run a bfs to set the scores for all other map units and doors
        while (true) {
            Door selectedDoor = null;
            int selectedScore = INF;

            // loop over each mapUnit. Find a door that has
            // its score already set and another door on the
            // same mapunit that has not had its score set.
            // compute the tentative score for the other door,
            // and find the smallest one.
            for (MapUnit m: placedMapUnits) {
                for (Door d1: m.doors) {
                    if (d1.doorScore == INF) continue;
                    for (DoorLink link: d1.doorLinks) {
                        Door d2 = m.doors.get(link.otherIdx);
                        if (d2.doorScore < INF) continue;
                        // Score is a function of distance, enemy score, and gate score
                        // Score is additive as you move away from the start
                        int distScore = (int)link.dist / 10;
                        int enemyScore = (int)m.enemyScore;
                        int gateScore = d2.gateScore;
                        int tentativeScore = distScore + enemyScore * link.tekiFlag
                            + gateScore + d1.doorScore;
                        if (tentativeScore < selectedScore) {
                            selectedScore = tentativeScore;
                            selectedDoor = d2;
                        }
                    }
                }
            }
            if (selectedScore == INF) break;

            // set the door and unit score for the selected door and
            // its adjacent mapunit
            selectedDoor.doorScore = selectedScore;
            selectedDoor.adjacentDoor.doorScore = selectedScore;
            MapUnit adj = selectedDoor.adjacentDoor.mapUnit;
            adj.unitScore = Math.min(selectedScore + adj.enemyScore, adj.unitScore);
        }

        for (MapUnit m: placedMapUnits) {
            m.unitScoreByPhase.add(m.unitScore);
            m.enemyScoreByPhase.add(m.enemyScore);
            for (Door d: m.doors) {
                d.doorScoreByPhase.add(d.doorScore);
            }
        }
    }

    void setEnemyScores() {
        // The enemy score for a map unit is 2 * #easy-enemies + 10 * #hard-enemies
        // Special enemies are not counted. What counts as an easy or hard enemy
        // depends on the spawnpoint type, which depends on the sublevel
        for (Teki t: placedTekis) {
            if (t.spawnPoint.type == 0) { 
                t.spawnPoint.mapUnit.enemyScore += 2;
            } else if (t.spawnPoint.type == 1) {
                t.spawnPoint.mapUnit.enemyScore += 10;
            }
        }
        // The gate score for a door is 5 if there is an enemy on it.
        for (Gate t: placedGates) { // there are never any gates placed when this is called.
            if (t.spawnPoint.door != null) {
                t.spawnPoint.door.gateScore += t.life;
                t.spawnPoint.door.adjacentDoor.gateScore += t.life;
            } 
        }
        for (Teki t: placedTekis) {
            if (t.spawnPoint.door != null) {
                t.spawnPoint.door.gateScore += 5;
                t.spawnPoint.door.adjacentDoor.gateScore += 5;
            } 
        }
    }

    SpawnPoint setHole() {
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int maxWeight = -1;

        // The score/weight for a hole is floor(sqrt(unitScore)) + 10 in normal mode,
        // and just unitScore in hard mode.
        // Only spawnpoints of type 4 (must be >= 150 distance from pod)
        // and type 9 (caps and alcoves) are considered.
        int[] typePriority = new int[] {1, 0, 2};
        for (int mapUnitType: typePriority) {
            // only use corridors if no other spots found in rooms/item alcoves
            if (mapUnitType == 2 && sps.size() > 0) continue;

            for (MapUnit m: placedMapUnits) {
                if (m.type != mapUnitType) continue;
                int score = isHardMode() ? (int)m.unitScore 
                    : (int)(sqrt(m.unitScore)) + 10;
                for (SpawnPoint sp: m.spawnPoints) {
                    if (sp.filled) continue;
                    float distToStart = spawnPointDist(placedStart, sp);
                    if ((sp.type == 4 && distToStart >= 150.0) || sp.type == 9) {
                        sp.scoreHole = score;
                        sps.add(sp);
                        weights.add(score);
                        maxWeight = Math.max(maxWeight, score);
                    }
                }
            }
        }

        if (sps.size() == 0) return null;
        SpawnPoint ret;
        if (!isHardMode()) {
            ret = sps.get(randIndexWeight(weights));
        } else {
            // In Hard mode, only the places with the highest score are considered
            ArrayList<SpawnPoint> spms = new ArrayList<SpawnPoint>();
            for (int i = 0; i < sps.size(); i++) {
                if (weights.get(i) == maxWeight)
                    spms.add(sps.get(i));
            }
            ret = spms.get(randInt(spms.size()));
        }
        ret.filled = true;
        return ret;
    }


    void setEnemy5() {
        // Type 5 are hazards on doors
        for (int numSpawned = 0; numSpawned < maxTeki5; numSpawned++) {
            // choose a empty door randomly. Room doors have weight 100, corridors 1
            ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
            ArrayList<Integer> weights = new ArrayList<Integer>();
            for (MapUnit m: placedMapUnits) {
                for (Door d: m.doors) {
                    if (m.type == 0 || d.spawnPoint.filled) continue;

                    sps.add(d.spawnPoint);
                    weights.add(m.type == 1 ? 100 : 1);
                }
            }
            SpawnPoint spot = null;
            if (sps.size() > 0)
                spot = sps.get(randIndexWeight(weights));

            // choose an enemy of type 5
            Teki toSpawn = getRandTeki(spawnTeki5, numSpawned);

            // quit if we reach the enemy limit for this type,
            // or there are no valid spots or enemies to place
            if (spot == null || toSpawn == null)
                break;

            // spawn the enemy
            Teki spawn = toSpawn.spawn(null, spot);
            setSpawnTekiPos(spawn, spot, false);
            spot.filled = true;
            placedTekis.add(spawn);
        }
    }

    void setEnemy8() {
        // find the possible spots
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type != 8) continue;
                if (m.type != 1 || sp.filled) continue;
                if (spawnPointDist(placedStart, sp) < 300) continue;
                if (placedHole != null && spawnPointDist(placedHole, sp) < 150) continue;
                if (placedGeyser != null && spawnPointDist(placedGeyser, sp) < 150) continue;

                sps.add(sp);
            }
        }

        // Type 8 are special enemies
        for (int numSpawned = 0; numSpawned < maxTeki8; numSpawned++) {
            // choose a uniform random spawnpoint of type 8 that is far enough from holes/start
            SpawnPoint spot = null;
            if (sps.size() > 0)
                spot = sps.get(randInt(sps.size()));

            // choose an enemy of type 8
            Teki toSpawn = getRandTeki(spawnTeki8, numSpawned);

            // quit if we reach the enemy cap for this type,
            // or there are no valid spots or enemies to place
            if (spot == null || toSpawn == null)
                break;
            sps.remove(spot);

            // spawn the enemy
            Teki spawn = toSpawn.spawn(spot.mapUnit, spot);
            setSpawnTekiPos(spawn, spot, false);
            spot.filled = true;
            placedTekis.add(spawn);
        }
    }

    void setEnemy1() {
        // find the possible spawnpoints
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type != 1) continue;
                if (m.type != 1 || sp.filled) continue;
                if (spawnPointDist(placedStart, sp) < 300) continue;
                if (placedHole != null && spawnPointDist(placedHole, sp) < 200) continue;
                if (placedGeyser != null && spawnPointDist(placedGeyser, sp) < 200) continue;

                sps.add(sp);
            }
        }

        // Type 1 are hard enemeies
        for (int numSpawned = 0; numSpawned < maxTeki1; numSpawned++) {
            // choose a uniform random spawnpoint of type 1 that is far enough from holes/start
            SpawnPoint spot = null;
            if (sps.size() > 0)
                spot = sps.get(randInt(sps.size()));

            // choose an enemy of type 1
            Teki toSpawn = getRandTeki(spawnTeki1, numSpawned);

            // quit if we reach the enemy cap for this type,
            // or there are no valid spots or enemies to place
            if (spot == null || toSpawn == null)
                break;
            sps.remove(spot);

            // spawn the enemy
            Teki spawn = toSpawn.spawn(spot.mapUnit, spot);
            setSpawnTekiPos(spawn, spot, false);
            spot.filled = true;
            placedTekis.add(spawn);
        }
    }

    void setEnemy0() {
        // find potential spawnpoints
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type != 0) continue;
                if (m.type != 1 || sp.filled) continue;
                if (spawnPointDist(placedStart, sp) < 300) continue;

                sps.add(sp);
            }
        }

        // Type 0 are easy enemies
        // These work differently as they can spawn in groups.
        for (int numSpawned = 0; numSpawned < maxTeki0; ) {
            // choose a uniform random spawnpoint of type 0 that is far enough from holes/start
            int minNum = 0, maxNum = 0, numToSpawn = 0, room = 0;
            SpawnPoint spot = null;
            if (sps.size() > 0) {
                spot = sps.get(randInt(sps.size()));
                minNum = spot.minNum;
                maxNum = spot.maxNum;
            }

            // choose an enemy of type 0
            Teki toSpawn = getRandTeki(spawnTeki0, numSpawned);
            // randomly chose the number to spawn
            // there is some logic to avoid spawning more enemies than
            // are allocated to spawn.
            if (numSpawned < minTeki0) { // we just spawned under min condition
                int cumMin = 0;
                for (Teki t: spawnTeki0) {
                    cumMin += t.min;
                    if (cumMin > numSpawned)
                        break;
                }
                room = cumMin - numSpawned;
            }
            else { // just spawned under weight condition
                room = maxMainTeki - placedTekis.size();
            }
            maxNum = Math.min(maxNum, room);
            if (maxNum <= minNum)
                numToSpawn = maxNum;
            else
                numToSpawn = minNum + randInt(maxNum - minNum + 1);

            // quit if we reach the enemy cap for this type,
            // or there are no valid spots or enemies to place
            if (spot == null || toSpawn == null || numToSpawn == 0)
                break;
            sps.remove(spot);

            // spawn the enemy
            ArrayList<Teki> justSpawned = new ArrayList<Teki>();
            for (int j = 0; j < numToSpawn; j++) {
                Teki spawn = toSpawn.spawn(spot.mapUnit, spot);
                setSpawnTekiPos(spawn, spot, true);
                spot.filled = true;
                placedTekis.add(spawn);
                justSpawned.add(spawn);
                numSpawned += 1;
            }

            // push the enemies away from each other
            for (int j = 0; j < 5; j++) {
                for (Teki t1: justSpawned) {
                    for (Teki t2: justSpawned) {
                        if (t1 == t2) continue;
                        float dx = t1.posX - t2.posX;
                        float dy = t1.posY - t2.posY;
                        float dz = t1.posZ - t2.posZ;
                        float dist = (float)sqrt(dx*dx+dy*dy+dz*dz);
                        if (dist > 0 && dist < 35) {
                            float mult = 0.5f * (35 - dist) / dist;
                            t1.posX += dx * mult;
                            t1.posY += dy * mult;
                            t1.posZ += dz * mult;
                            t2.posX -= dx * mult;
                            t2.posY -= dy * mult;
                            t2.posZ -= dz * mult;
                        }
                    }
                }
            }
        }
    }

    void setPlant() {
        // find the possible spawnpoints
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type != 6) continue;
                if (sp.filled) continue;
                sps.add(sp);
            }
        }

        // type 6 are usually plants. Note, sometimes real enemies are listed as type 6,
        // and sometimes actual plants are listed as a different type than 6.
        int minSum = 0; // plants only spawn under the min condition
        for (Teki t: spawnTeki6) {
            if (t.type == 6)
                minSum += t.min;
        }
        for (int numSpawned = 0; numSpawned < minSum; numSpawned++) {
            // choose a uniform random spawnpoint of type 6
            SpawnPoint spot = null;
            if (sps.size() > 0)
                spot = sps.get(randInt(sps.size()));

            // choose an enemy of type 6
            Teki toSpawn = getRandTeki(spawnTeki6, numSpawned);

            // quit if we reach the enemy cap for this type,
            // or there are no valid spots or enemies to place
            if (spot == null || toSpawn == null)
                break;
            sps.remove(spot);

            // spawn the enemy
            Teki spawn = toSpawn.spawn(spot.mapUnit, spot);
            setSpawnTekiPos(spawn, spot, false);
            spot.filled = true;
            placedTekis.add(spawn);
        }
    }

    void setItem() {
        // type 2 are items, aka treasures not inside enemies.
        // Treasures inside enemies spawn automatically with the enemy.
        for (int numSpawned = 0; numSpawned < maxItem; numSpawned++) {
            // Generate the weights for the spawnpoints for the treasures
            ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
            ArrayList<Integer> weights = new ArrayList<Integer>();
            int maxWeight = -1;
            int sumWeight = 0;
            for (MapUnit m: placedMapUnits) {
                if (m.type == 1) {
                    int numSpawnPointsType2 = 0;
                    for (SpawnPoint sp: m.spawnPoints)
                        if (sp.type == 2)
                            numSpawnPointsType2 += 1;
                    int numItemsThisMapUnit = 0;
                    for (Item i: placedItems)
                        if (i.spawnPoint.mapUnit == m)
                            numItemsThisMapUnit += 1;
                    for (SpawnPoint sp: m.spawnPoints) {
                        if (sp.type != 2 || sp.filled) continue;
                        // For rooms, the score is (unitScore / (1+numItemsThisMapUnit))
                        // on hard mode, and (1+unitScore/numSpawnPointsType2) on normal mode
                        int score = isHardMode() ? (int)(m.unitScore / (1 + numItemsThisMapUnit)) 
                            : (int)(1 + m.unitScore / numSpawnPointsType2);
                        sps.add(sp);
                        weights.add(score);
                        maxWeight = Math.max(score, maxWeight);
                        sumWeight += score;
                        sp.scoreItem = score;
                    }
                }
                else if (itemInName(m.name)) {
                        SpawnPoint sp = capSpawnPoint(m);
                        if (sp.filled) continue;
                        // For item alcoves, the score is (1+unitScore) on hard mode,
                        // and (1+10*unitScore) on normal mode.
                        int score = isHardMode() ? (int)(1 + m.unitScore) 
                            : (int)(1 + m.unitScore * 10);
                        sps.add(sp);
                        weights.add(score);
                        maxWeight = Math.max(score, maxWeight);
                        sumWeight += score;
                        sp.scoreItem = score;
                }
            }
            // update the probabilities that the alcoves are visually empty
            // (this isn't part of the algorithm, it's just used to help
            // challenge mode runners guess where the hole is)
            if (!hardMode)
                challengeModeHoleProbItem(sumWeight);

            SpawnPoint spot = null;
            if (sps.size() > 0) {
                if (!isHardMode()) // on normal mode, choose randomly weighted by score.
                    spot = sps.get(randIndexWeight(weights));
                else {
                    // on hard mode, only consider the spawnpoints that have the highest
                    // possible score. Choose uniformly random from these.
                    ArrayList<SpawnPoint> spms = new ArrayList<SpawnPoint>();
                    for (int i = 0; i < sps.size(); i++)
                        if (weights.get(i) == maxWeight)
                            spms.add(sps.get(i));
                    spot = spms.get(randInt(spms.size()));
                }
            }

            // choose the item to spawn
            Item toSpawn = getRandItem(numSpawned);

            // don't spawn it if there are no open spaces or no treasures left to spawn
            if (spot == null || toSpawn == null) break;

            // spawn the treasure
            Item spawn = toSpawn.spawn(spot.mapUnit, spot);
            setSpawnItemPos(spawn, spot);
            spot.filled = true;
            placedItems.add(spawn);
        }
    }

    void setCapEnemy() {
        // Spawn the enemies that appear in item alcoves.
        // Notably, the order that the alcoves are looped over
        // and the order that the min condition tekis are considered is
        // NOT random, making their spawns predicable.

        // update the probabilities that the alcoves are visually empty
        // (this isn't part of the algorithm, it's just used to help
        // challenge mode runners guess where the hole is)
        if (!hardMode)
            challengeModeHoleProbCap();

        int numSpawned = 0;
        for (MapUnit m: placedMapUnits) {
            if (m.type != 0) continue;
            if (!itemInName(m.name)) continue;
            // loop over all item alcoves, in order that they were placed.

            SpawnPoint spot = capSpawnPoint(m);
            if (spot.filled) continue;

            // choose a grounded cap teki to spawn
            Teki toSpawn = getRandCapTeki(spawnCapTeki, numSpawned);

            // spawn it
            if (toSpawn != null) {
                for (int i = 0; i < toSpawn.numToSpawn; i++) {
                    Teki spawn = toSpawn.spawn(m, spot);
                    setSpawnTekiPos(spawn, spot, false);
                    spot.filled = true;
                    if (isPomGroup(spawn)) {
                        spot.filledFalling = true; // nothing can fall on candypops
                        // gates don't prioritize spawning in front of falling candypops
                        if (spawn.fallType != 0)
                            spot.filledFallingPom = true;
                    }
                    placedTekis.add(spawn);
                    numSpawned++;
                }
            }
        }

        numSpawned = 0;
        for (MapUnit m: placedMapUnits) {
            if (m.type != 0) continue;
            if (!itemInName(m.name)) continue;
            // loop over all item alcoves, in order that they were placed.

            // Can't fall on hole, geyser, or candypop
            SpawnPoint spot = capSpawnPoint(m);
            if (spot == placedHole) continue;
            if (spot == placedGeyser) continue;
            if (spot.filledFalling) continue;

            // choose a falling cap teki to spawn
            Teki toSpawn = getRandCapTeki(spawnCapFallingTeki, numSpawned);

            // spawn it
            if (toSpawn != null) {
                for (int i = 0; i < toSpawn.numToSpawn; i++) {
                    Teki spawn = toSpawn.spawn(m, spot);
                    setSpawnTekiPos(spawn, spot, false);
                    spawn.posY += 100; // The game doesn't actually set the y-coord like this...
                    spot.filledFalling = true;
                    placedTekis.add(spawn);
                    numSpawned++;
                }
            }
        }
    }

    void setGate() {
        // Gates spawn on doors.
        for (int numSpawned = 0; numSpawned < maxGate; numSpawned++) {
            // Choose a random gate to spawn, weighted by weight.
            ArrayList<Gate> gates = new ArrayList<Gate>();
            ArrayList<Integer> weightsG = new ArrayList<Integer>();
            for (Gate t: spawnGate) {
                gates.add(t);
                weightsG.add(t.weight);
            }
            Gate toSpawn = null;
            if (gates.size() > 0) {
                toSpawn = gates.get(randIndexWeight(weightsG));
            }

            // Choose the spot for the gate, this gets fairly complicated.
            SpawnPoint spot = getGateSpot();

            // spawn the gate
            if (spot == null || toSpawn == null) break;
            Gate spawn = toSpawn.spawn(null, spot);
            setSpawnGatePos(spawn, spot);
            spot.filled = true;
            placedGates.add(spawn);
        }
    }

    SpawnPoint getGateSpot() {
        // This function chooses the location for the gate to spawn.
        // The game uses 4 different procedures to spawn gates, which are checked
        // in a specific order.
        ArrayList<SpawnPoint> sps = new ArrayList<SpawnPoint>();
        ArrayList<Integer> weights = new ArrayList<Integer>();

        // itemSetCapDoor
        // picks a uniform random door attached to an item alcove
        // that has an item, hole, geyser, or grounded cap teki.
        // The intent is to block off an item alcove with something in it.
        for (MapUnit m: placedMapUnits) {
            if (m.type != 0 || !itemInName(m.name)) continue;

            SpawnPoint spm = capSpawnPoint(m);
            if (!spm.filled) continue;
            if (spm.filledFallingPom) continue;

            for (Door d: m.doors) {
                if (d.spawnPoint.filled) continue;
                sps.add(d.spawnPoint);
            }
        }
        if (sps.size() > 0)
            return sps.get(randInt(sps.size()));

        // getRoomMinScoreDoor
        // for each placed mapunit that doesn't contain the start
        // and is type 1, loop over all of the doors and find the
        // first door with the minimum door score. If that door
        // doesn't already have a gate/teki, place a gate there.
        // otherwise, move on to the next mapunit.
        // The intent is to block off the easiest path.
        for (MapUnit m: placedMapUnits) {
            if (m.type != 1 || m == placedStart.mapUnit) continue;

            int minScore = INF;
            Door minDoor = null;
            for (Door d: m.doors) {
                int doorScore = (int)d.doorScore;
                if (doorScore < minScore) {
                    minScore = doorScore;
                    minDoor = d;
                }
            }

            if (minScore < INF && !minDoor.spawnPoint.filled)
                return minDoor.spawnPoint;
        }

        // getRoomLowScoreDoor
        // Only check this procedure with 80% probability.
        // consider all open doors on rooms. Let M be the maximum door score among
        // these. Do weighted random where the weight for each door is M + 1 - doorScore
        // The intent is to block off easy paths.
        if (randFloat() < 0.8) {
            int maxOpenDoorScore = -1;
            for (MapUnit m: placedMapUnits) {
                if (m.type != 1) continue;
                for (Door d: m.doors) {
                    if (d.spawnPoint.filled) continue;
                    maxOpenDoorScore = Math.max(maxOpenDoorScore, (int)d.doorScore);
                }
            }

            for (MapUnit m: placedMapUnits) {
                if (m.type != 1) continue;
                for (Door d: m.doors) {
                    if (d.spawnPoint.filled) continue;
                    sps.add(d.spawnPoint);
                    weights.add(maxOpenDoorScore + 1 - (int)d.doorScore);
                }
            }

            if (sps.size() > 0)
                return sps.get(randIndexWeight(weights));
        }

        // getRandomScoreDoor
        // consider all open doors on all units, regardless of type
        // Choose weighted random such that for room: weight = numdoors,
        // corridors: weight = 10/numdoors, alcoves: weight = numdoors
        // This places gates fairly randomly, though it prefers
        // rooms and hallways.
        for (MapUnit m: placedMapUnits) {
            for (Door d: m.doors) {
                if (d.spawnPoint.filled) continue;

                sps.add(d.spawnPoint);
                int numDoors = m.doors.size();
                int weight = m.type == 2 ? 10 / numDoors : numDoors;
                weights.add(weight);
            }
        }
        if (sps.size() > 0) {
            return sps.get(randIndexWeight(weights));
        }
        return null;
    }

    void setSpawnTekiPos(Teki t, SpawnPoint sp, boolean useRand) {
        t.posX = sp.posX;
        t.posZ = sp.posZ;
        t.posY = sp.posY;
        t.ang = sp.ang;
        if (useRand) {
            // for type 0 enemies, their location is randomized
            float radius = sp.radius * randFloat();
            float ang = (float)Math.PI * 2 * randFloat();
            t.posX += Math.sin(ang) * radius;
            t.posZ += Math.cos(ang) * radius; // note sin/cos are switched
            t.ang = ang;
        }
    }

    void setSpawnItemPos(Item t, SpawnPoint sp) {
        t.posX = sp.posX;
        t.posZ = sp.posZ;
        t.posY = sp.posY;
        t.ang = sp.ang;
    }

    void setSpawnGatePos(Gate t, SpawnPoint sp) {
        t.posX = sp.posX;
        t.posZ = sp.posZ;
        t.posY = sp.posY;
        t.ang = sp.ang;
    }

    Teki getRandTeki(ArrayList<Teki> spawnTeki, int numSpawned) {
        // choose an enemy. First, we use the min condition to
        // spawn that many teki, in order that they appear in the file.
        // After we have done all of the mins, we choose randomly by weight.
        ArrayList<Teki> tekis = new ArrayList<Teki>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int cumMins = 0;
        for (Teki t: spawnTeki) {
            cumMins += t.min;
            if (numSpawned < cumMins) {
                return t;
            }
            if (t.weight > 0) {
                tekis.add(t);
                weights.add(t.weight);
            }
        }
        if (tekis.size() > 0) {
            return tekis.get(randIndexWeight(weights));
        }
        return null;
    }

    Teki getRandCapTeki(List<Teki> spawnList, int numSpawned) {
        // choose a cap enemy. First, we use the min condition to
        // spawn that many teki, in order that they appear in the file.
        // After we have done all of the mins, we choose randomly by weight.
        ArrayList<Teki> tekis = new ArrayList<Teki>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int cumMins = 0;
        for (Teki t: spawnList) {
            cumMins += t.min;
            if (numSpawned < cumMins) {
                t.numToSpawn = 1;
                if (t.type == 0 && numSpawned + 1 < cumMins)
                    t.numToSpawn = 2;
                return t;
            }
            if (t.weight > 0) {
                tekis.add(t);
                weights.add(t.weight);
            }
        }
        if (tekis.size() > 0) {
            Teki t = tekis.get(randIndexWeight(weights));
            t.numToSpawn = 1;
            if (t.type == 0)
                t.numToSpawn = 2;
            return t;
        }
        else {
            // In the weight condition case, rand still gets
            // hit even if there is nothing to spawn.
            rand();
            return null;
        }
    }

    Item getRandItem(int numSpawned) {
        // choose an item. First, we use the min condition to
        // spawn that many items, in order that they appear in the file.
        // After we have done all of the mins, we choose randomly by weight.
        ArrayList<Item> items = new ArrayList<Item>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int cumMins = 0;
        for (Item t: spawnItem) {
                cumMins += t.min;
                if (numSpawned < cumMins) {
                    return t;
                }
                if (t.weight > 0) {
                    items.add(t);
                    weights.add(t.weight);
                }
        }
        if (items.size() > 0) {
            return items.get(randIndexWeight(weights));
        }
        return null;
    }

    void buildWayPointGraph() {
        // build adjacency graph
        for (MapUnit m: placedMapUnits) {
            for (WayPoint wp: m.wayPoints) {
                wp.adj = new ArrayList<WayPoint>();
                wp.inverts = new ArrayList<WayPoint>();
                for (Integer idx: wp.links) {
                    wp.adj.add(m.wayPoints.get(idx));
                }
            }
        }
        for (MapUnit m: placedMapUnits) {
            for (Door d: m.doors) {
                WayPoint a = m.wayPoints.get(d.wpIdx);
                WayPoint b = d.adjacentDoor.mapUnit.wayPoints.get(d.adjacentDoor.wpIdx);
                a.adj.add(b);
            }
        }
        // build inverted links
        for (MapUnit m: placedMapUnits) {
            for (WayPoint w: m.wayPoints) {
                for (WayPoint w2: w.adj) {
                    w2.inverts.add(w);
                }
            }
        }

        // find starting waypoint
        float minDistS = INF;
        WayPoint start = null;
        for (WayPoint wp: placedMapUnits.get(0).wayPoints) {
            float dist = spawnPointWayPointDist(placedStart, wp);
            if (dist < minDistS) {
                minDistS = dist;
                start = wp;
            }
        }
        start.isStart = true;
        start.distToStart = 0;
        start.backWp = null;

        // compute shortest path + distance to the start from each waypoint
        // using a bfs
        LinkedList<WayPoint> frontier = new LinkedList<WayPoint>();
        frontier.add(start);
        while (frontier.size() > 0) {
            WayPoint w = null;
            float minDist = INF;
            for (WayPoint f: frontier) {
                if (f.distToStart < minDist) {
                    minDist = f.distToStart;
                    w = f;
                }
            }
            w.visited = true;
            frontier.remove(w);

            for (WayPoint inv: w.inverts) {
                if (inv.visited) continue;
                if (inv.distToStart == INF) frontier.add(inv);
                float dist = wayPointDist(w, inv);
                if (dist + w.distToStart < inv.distToStart) {
                    inv.backWp = w;
                    inv.distToStart = dist + w.distToStart;
                }
            }
        }
    }

    void placeOnionsColossal() {
        int validRooms = 0;
        for (MapUnit m: placedMapUnits) {
            if (m.type == 1) {
                for (SpawnPoint p: m.spawnPoints) {
                    if (p.type == 7) {
                        validRooms += 1;
                        break;
                    }
                }
            }
        }
        int roomInterval = validRooms/3;
        if (roomInterval == 0) roomInterval += 1;
        int colorID = 0, roomNum = 0;
        for (MapUnit m: placedMapUnits) {
            if (m.type == 1) {
                SpawnPoint sp = null;
                for (SpawnPoint p: m.spawnPoints) {
                    if (p.type == 7) {
                        sp = p;
                        break;
                    }
                }
                if (sp != null && colorID < 3 && ++roomNum == (colorID + 1) * roomInterval) {
                    Onion o = new Onion();
                    o.posX = sp.posX;
                    o.posZ = sp.posZ;
                    o.posY = sp.posY;
                    o.spawnPoint = sp;
                    o.type = 2-colorID;
                    placedOnions.add(o);
                    colorID += 1;
                }
            }
        }
    }

    // Utility functions ----------------------------------------------------------------

    boolean isHardMode() {
        // hard mode is used for story mode
        // normal mode is used for challenge mode
        return hardMode;
    }

    HashSet<String> pomString = Parser.hashSet("bluepom,redpom,yellowpom,blackpom,whitepom,randpom,pom");
    boolean isPomGroup(Teki t) {
        // aka isCandypopBud
        return pomString.contains(t.tekiName.toLowerCase());
    }

    void recomputeOpenDoors() {
        openDoors.clear();
        for (MapUnit m: placedMapUnits) {
            for (Door d: m.doors) {
                if (Door.isDoorOpen(d))
                    openDoors.add(d);
            }
        }
    }

    int countPlacedType(int type) {
        int count = 0;
        for (MapUnit m: placedMapUnits) {
            if (m.type == type)
                count += 1;
        }
        return count;
    }

    boolean isInnerBox(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        // checks if two boxes overlap
        if (x1 + w1 <= x2 || x2 + w2 <= x1) return false;
        if (y1 + h1 <= y2 || y2 + h2 <= y1) return false;
        return true;
    }

    boolean itemInName(String s) {
        return s.substring(0, Math.min(4,s.length())).equals("item");
    }

    SpawnPoint capSpawnPoint(MapUnit m) {
        for (SpawnPoint sp: m.spawnPoints) {
            if (sp.type == 9)
                return sp;
        }
        assert false;
        return null;
    }

    // note, this function was just an incorrect guess of how the waypoints work
    WayPoint closestWayPoint(SpawnPoint a) {
        if (a.closestWayPoint != null) return a.closestWayPoint;
        float minDist = INF;
        WayPoint minWp = null;
        for (MapUnit m: placedMapUnits) {
            for (WayPoint wp: m.wayPoints) {
                float dist = spawnPointWayPointDist(a, wp);
                if (dist < minDist) {
                    minDist = dist;
                    minWp = wp;
                }
            }
        }
        a.closestWayPoint = minWp;
        return a.closestWayPoint;
    }

    // note, this function was just an incorrect guess of how the waypoints work
    float spawnPointDistToStart(SpawnPoint a) {
        if (a.distToStart != -1) return a.distToStart;
        WayPoint minWp = closestWayPoint(a);
        float dist = spawnPointWayPointDist(a, minWp);
        if (minWp.isStart) {
            a.distToStart = dist;
        } else {
            float distBack = spawnPointWayPointDist(a, minWp.backWp) + minWp.backWp.distToStart;
            if (distBack < minWp.distToStart)
                a.distToStart = distBack;
            else a.distToStart = dist + minWp.distToStart;
        }
        return a.distToStart;
    }

    void challengeModeHoleProbItem(int sumWeight) {
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if ((sp == placedHole || sp == placedGeyser || !sp.filled) && sp.type == 9 && sp.mapUnit.type == 0) {
                    float missingScore = sp.mapUnit.unitScoreByPhase.get(1)*10+1;
                    if (sumWeight == 0)
                        sp.probVisuallyEmpty = 0;
                    else
                        sp.probVisuallyEmpty *= 1 - missingScore/(missingScore+sumWeight);
                }
            }
        }          
    }

    void challengeModeHoleProbCap() {
        int totalEmptyAlcoves = 0;
        int totalCapEnemiesToSpawn = 0;
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if (sp.type == 9 && !sp.filled)
                    totalEmptyAlcoves += 1;
            }
        }
        for (Teki t: spawnCapTeki) {
            totalCapEnemiesToSpawn += t.min;
            if (t.weight > 0) {
                totalCapEnemiesToSpawn = INF;
                break;
            }
        }
        for (MapUnit m: placedMapUnits) {
            for (SpawnPoint sp: m.spawnPoints) {
                if ((sp == placedHole || sp == placedGeyser || !sp.filled) && sp.type == 9 && sp.mapUnit.type == 0) {
                    if (totalEmptyAlcoves < totalCapEnemiesToSpawn)
                        sp.probVisuallyEmpty = 0;
                    else
                        sp.probVisuallyEmpty *= 1 - totalCapEnemiesToSpawn/(totalEmptyAlcoves+1.0f);
                }
            }
        }
    }
    
    float spawnPointDist(SpawnPoint a, SpawnPoint b) {
        float dx = a.posX - b.posX;
        float dz = a.posZ - b.posZ;
        float dy = a.posY - b.posY;
        return (float)sqrt(dx * dx + dy * dy + dz * dz);
    }

    float wayPointDist(WayPoint a, WayPoint b) {
        float dx = a.posX - b.posX;
        float dz = a.posZ - b.posZ;
        float dy = a.posY - b.posY;
        return (float)sqrt(dx * dx + dy * dy + dz * dz);
    }

    float spawnPointWayPointDist(SpawnPoint a, WayPoint b) {
        float dx = a.posX - b.posX;
        float dz = a.posZ - b.posZ;
        float dy = a.posY - b.posY;
        return (float)sqrt(dx * dx + dy * dy + dz * dz);
    }

    int rand() { // the star of the show!
        seed = seed * 0x41c64e6d + 0x3039;
        int ret = (seed >>> 0x10) & 0x7fff;
        return ret; 
    }
    // some important to understand random helper functions
    int randInt(int max) {
        return (int)(rand() * max / 32768.0f);
    }
    float randFloat() {
        return rand() / 32768.0f;
    }
    int randIndexWeight(List<Integer> weights) {
        int sum = 0;
        for (int w: weights) sum += w;
        int cumSum = 0;
        int r = randInt(sum);
        for (int i = 0; i < weights.size(); i++) {
            cumSum += weights.get(i);
            if (cumSum > r)
                return i;
        }
        return -1;
     }
    <T> void randBacks(List<T> a) {
        for (int i = 0; i < a.size(); i++) {
            int r = randInt(a.size());
            a.add(a.remove(r));
        }
    }
    <T> void randSwaps(List<T> a) {
        for (int i = 0; i < a.size(); i++) {
            int r = randInt(a.size());
            T temp = a.get(i);
            a.set(i, a.get(r));
            a.set(r, temp);
        }
    }
    
    public static float sqrt(float x) {
        return (float)(x * ApproximateReciprocalSquareRoot((double)x));
    }

    // https://github.com/dolphin-emu/dolphin/commit/cffa848b9960bcf3dd7a5f3dfd8cdbe417b6ec55#diff-903a032099cd9031620bb1c10e0f7409
    // https://en.wikipedia.org/wiki/Fast_inverse_square_root
    // This function implements the frsqrte instruction of power pc.
    // The intent is to quickly approximate 1/sqrt(val) to within 1/32 precision.
    // How it works is black magic bit hacking
    static int expected_base[] = {
        0x3ffa000, 0x3c29000, 0x38aa000, 0x3572000,
        0x3279000, 0x2fb7000, 0x2d26000, 0x2ac0000,
        0x2881000, 0x2665000, 0x2468000, 0x2287000,
        0x20c1000, 0x1f12000, 0x1d79000, 0x1bf4000,
        0x1a7e800, 0x17cb800, 0x1552800, 0x130c000,
        0x10f2000, 0x0eff000, 0x0d2e000, 0x0b7c000,
        0x09e5000, 0x0867000, 0x06ff000, 0x05ab800,
        0x046a000, 0x0339800, 0x0218800, 0x0105800,
    };
    static int expected_dec[] = {
        0x7a4, 0x700, 0x670, 0x5f2,
        0x584, 0x524, 0x4cc, 0x47e,
        0x43a, 0x3fa, 0x3c2, 0x38e,
        0x35e, 0x332, 0x30a, 0x2e6,
        0x568, 0x4f3, 0x48d, 0x435,
        0x3e7, 0x3a2, 0x365, 0x32e,
        0x2fc, 0x2d0, 0x2a8, 0x283,
        0x261, 0x243, 0x226, 0x20b,
    };
    static double ApproximateReciprocalSquareRoot(double val)
    {
        double valf = val;
        long vali = Double.doubleToRawLongBits(valf);
		
        long mantissa = vali & ((1L << 52) - 1);
        long sign = vali & (1L << 63);
        long exponent = vali & (0x7FFL << 52);

        // Special case 0
        if (mantissa == 0 && exponent == 0)
            return 0;
        // Special case NaN-ish numbers
        if (exponent == (0x7FFL << 52))
            {
                if (mantissa == 0)
                    {
                        if (sign != 0)
                            return Double.NaN;
                        return 0.0;
                    }
                return 0.0 + valf;
            }
        // Negative numbers return NaN
        if (sign != 0)
            return Double.NaN;

        if (exponent == 0)
            {
                // "Normalize" denormal values
                do
                    {
                        exponent -= 1L << 52;
                        mantissa <<= 1;
                    } while ((mantissa & (1L << 52)) == 0);
                mantissa &= (1L << 52) - 1;
                exponent += 1L << 52;
            }

        boolean odd_exponent = 0 == (exponent & (1L << 52));
        exponent = ((0x3FFL << 52) - ((exponent - (0x3FEL << 52)) / 2)) & (0x7FFL << 52);

        int i = (int)(mantissa >> 37);
        vali = sign | exponent;
        int index = i / 2048 + (odd_exponent ? 16 : 0);
        vali |= (long)(expected_base[index] - expected_dec[index] * (i % 2048)) << 26;
        return Double.longBitsToDouble(vali);
    }

    // -------- Everything below is for the game's waypoint algorithm ---------

    boolean isInnerBox(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        // checks if two boxes overlap
        if (x1 + w1 <= x2 || x2 + w2 <= x1) return false;
        if (y1 + h1 <= y2 || y2 + h2 <= y1) return false;
        return true;
    }

    float pointToPointDist(Vec3 p1, Vec3 p2) {
        return p1.subtract(p2).length();
    }

    Vec3 normVector(Vec3 p1, Vec3 p2) {
        Vec3 diff = p2.subtract(p1);
        float len = diff.length();
        if (len <= 0) return null;
        return diff.scale(1.0f/len);
    }
    
    float pointToSegmentDist(Vec3 p, Vec3 l1, Vec3 l2, float r1, float r2) {
        float lenL = l2.subtract(l1).length();
        if (lenL <= 0) return 128001;
        Vec3 norm = normVector(l1, l2);
        float t = norm.dot(p.subtract(l1)) / lenL;
        if (t <= 0) {
            pointToSegmentDist_outCloserVec = 1;
            return p.subtract(l1).length() - r1;
        } else if (t >= 1) {
            pointToSegmentDist_outCloserVec = 2;
            return p.subtract(l2).length() - r2;
        } else {
            pointToSegmentDist_outCloserVec = p.subtract(l1).length() - r1 < p.subtract(l2).length() - r2 ? 1 : 2;
            return norm.scale(lenL * t).add(l1).subtract(p).length() - (1-t)*r1 - t*r2;
        }
    }
    int pointToSegmentDist_outCloserVec = 0;

    ArrayList<WayPoint> makePathToGoal(Vec3 p) {
        
        for (MapUnit m: placedMapUnits) {
            for (WayPoint wp: m.wayPoints) {
                if (wp.vec == null) {
                    wp.vec = new Vec3(wp.posX, wp.posY, wp.posZ);
                } else {
                    break;
                }
            }
        }

        // find the edge on the waypoint graph that's closest to this point, then take the closer
        // point on that edge as the start of the path.
        float bestDist = 128000;
        WayPoint bestWp = null;
        for (MapUnit m: placedMapUnits) {
            if (!isInnerBox(m.offsetX*170.0f, m.offsetZ*170.0f, m.dX*170.0f, m.dZ*170.0f, p.x-5, p.z-5, 10, 10))
                continue;
            for (WayPoint wp: m.wayPoints) {
                for (WayPoint wp2: wp.adj) {
                    if (wp.idx < wp2.idx || !wp2.adj.contains(wp)) {
                        float d = pointToSegmentDist(p, wp.vec, wp2.vec, wp.radius, wp2.radius);
                        if (d < bestDist) {
                            bestDist = d;
                            bestWp = pointToSegmentDist_outCloserVec == 1 ? wp : wp2;
                        }
                    }
                }
            }
        }
        ArrayList<WayPoint> ret = new ArrayList<WayPoint>();
        while (bestWp != null) {
            ret.add(bestWp);
            bestWp = bestWp.backWp;
        }
        return ret;
    }

    ArrayList<Vec3> drawPathToGoal(Vec3 p, float speed, int maxNumIter) {
        ArrayList<Vec3> ret = new ArrayList<Vec3>();

        ArrayList<WayPoint> path = makePathToGoal(p);

        ret.add(p);
        
        //System.out.println(path.size());
        for (int i = 0; i < path.size(); i++) {
            ret.add(path.get(i).vec);
        }

        return ret;
    }

}
