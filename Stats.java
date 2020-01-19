import java.util.*;
import java.io.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;  

// this class computes various statistics
// this class can be modified in order to collect the desired statistics

class Stats {

    PrintWriter out = null;
    long startTime;
    final int INF = Integer.MAX_VALUE;

    // this function gets called once at the start of the process
    public Stats(String args[]) {
        if (CaveGen.findGoodLayouts) {
            Parser.readEnemyFile();
        }
        try {
            startTime = System.currentTimeMillis();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();  
            String dateString = dtf.format(now);
            String output = CaveGen.p251 ? "output251" : "output";
            new File(output+"/").mkdir();
            new File(output + "/!reports/").mkdir();
            String outputFileName = output + "/!reports/report-" + dateString + ".txt";
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
            out.print("CaveGen ");
            for (String s: args) {
                out.print(s + " ");
            }
            out.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    int caveGenCount = 0;
    int numPurpleFlowers[] = new int[11];
    int minSumTreasureScore = INF;
    int minSumTreasureScoreSeed = -1;
    int missingTreasureCount = 0;
    ArrayList<Integer> allScores = new ArrayList<Integer>();

    // this function gets called once for every sublevel g that generates
    void analyze(CaveGen g) {
        caveGenCount += 1; 

        // count the number of purple flowers
        int num = 0;
        for (Teki t: g.placedTekis) {
            if (t.tekiName.equalsIgnoreCase("blackpom"))
                num += 1;
        }
        if (num > 10) num = 10;
        numPurpleFlowers[num] += 1;

        // find the sum of treasure scores
        // you probably want to use a different metric.
        int sumTreasureScore = 0;
        for (Item t: g.placedItems) {
            sumTreasureScore += t.spawnPoint.scoreItem;
        }
        for (Teki t: g.placedTekis) {
            if (t.itemInside != null) {
                if (t.type != 5)
                    sumTreasureScore += t.mapUnit.unitScore;
                else
                    sumTreasureScore += t.spawnPoint.door.doorScore;
            }
        }
        if (sumTreasureScore < minSumTreasureScore) {
            minSumTreasureScore = sumTreasureScore;
            minSumTreasureScoreSeed = g.initialSeed;
        }

        // report about missing treasures
        // print the seed everytime we see a missing treasure
        int minTreasure = 0, actualTreasure = 0;
        for (Item t: g.spawnItem) {
            minTreasure += t.min;
        }
        for (Teki t: g.spawnMainTeki) {
            if (t.itemInside != null)
                minTreasure += t.min;
        }
        for (Teki t: g.spawnCapTeki) {
            if (t.itemInside != null)
                minTreasure += t.min;
        }
        for (Teki t: g.spawnCapFallingTeki) {
            if (t.itemInside != null) 
                minTreasure += t.min;
        }
        actualTreasure += g.placedItems.size();
        for (Teki t: g.placedTekis) {
            if (t.itemInside != null)
                actualTreasure += 1;
        }
        int expectedMissingTreasures = 0;
        if ("CH29 1".equals(g.specialCaveInfoName + " " + g.sublevel))
            expectedMissingTreasures = 1; // This level is always missing a treasure
        boolean missingUnexpectedTreasure = actualTreasure + expectedMissingTreasures < minTreasure;
        if (missingUnexpectedTreasure) {
            out.println("Missing treasure: " + g.specialCaveInfoName + " " + g.sublevel + " " + Drawer.seedToString(g.initialSeed));
            missingTreasureCount += 1;
        }

        if (CaveGen.findGoodLayouts && !missingUnexpectedTreasure) {
            int numGoodLayoutsToGiveImagesFor = Math.abs((int)(CaveGen.findGoodLayoutsRatio*CaveGen.numToGenerate));
            boolean giveWorstLayoutsInstead = CaveGen.findGoodLayoutsRatio < 0;

            ArrayList<Teki> placedTekisWithItems = new ArrayList<Teki>();
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null)
                placedTekisWithItems.add(t);
            }

            // Compute the waypoints on the shortest paths
            ArrayList<WayPoint> wpOnShortPath = new ArrayList<WayPoint>();
            for (Item t: g.placedItems) {
                if ("g_futa_kyodo,flower_blue,tape_blue,kinoko_doku,flower_red,futa_a_silver,cookie_m_l".contains(t.itemName.toLowerCase())) continue;
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }
            for (Teki t: placedTekisWithItems) {
                if ("chocolate".contains(t.itemInside)) continue;
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }

            // add up distance penalty for score
            int score = 0;
            for (WayPoint wp: wpOnShortPath) {
                score += wp.distToStart - wp.backWp.distToStart;
            }    
            // add up enemy penalties for score
            for (Teki t: g.placedTekis) {
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                if (wpOnShortPath.contains(wp)) {
                    score += Parser.tekiDifficultyMap.get(t.tekiName.toLowerCase());
                }
            }
            // add up gate penalties for score
            for (Gate t: g.placedGates) {
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                if (g.placedHole.mapUnit.type == 0 && g.placedHole.mapUnit.doors.get(0).spawnPoint == t.spawnPoint)
                    score += t.life / 3; // covers hole
                if (wpOnShortPath.contains(wp))
                    score += t.life / 3; // covers path back to ship
            }

            if (giveWorstLayoutsInstead) score *= -1;

            // keep a sorted list of the scores
            for (int i = 0; i <= allScores.size(); i++) {
                if (i == allScores.size()) {
                    allScores.add(score);
                    break;
                }
                if (score < allScores.get(i)) {
                    allScores.add(i, score);
                    break;
                }
            }

            // only print good ones
            if (caveGenCount > CaveGen.numToGenerate/10 && 
                score <= allScores.get((int)(allScores.size()*numGoodLayoutsToGiveImagesFor/CaveGen.numToGenerate)) 
                || score == allScores.get(0) && caveGenCount > CaveGen.numToGenerate/40) {
                CaveGen.images = true;
                out.println("GoodLayoutScore: " + Drawer.seedToString(g.initialSeed) + " -> " + score);
            }
            else {
                CaveGen.images = false;
            }

        }
    }

    // this function gets called once at the end of the process
    void createReport() {
        out.println("\nGenerated " + caveGenCount + " sublevels.");
        out.println("Total run time: " + (System.currentTimeMillis()-startTime)/1000.0 + "s");
        
        // report about missing treasures
        out.println("Missing treasure count: " + missingTreasureCount);
        
        // report about purple flowers
        out.println("\nPurple flower distribution: ");
        for (int i = 0; i < 11; i++) {
            out.println(i + (i==10?"+":"") + ": " + numPurpleFlowers[i]);
        }

        // report about treasure scores
        out.println("\nBest sum of treasure score: " + minSumTreasureScore);
        out.println("Best seed: " + Drawer.seedToString(minSumTreasureScoreSeed));

        out.close();
    }
}
