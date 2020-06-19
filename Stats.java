import java.io.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;  

// this class computes various statistics
// this class can be modified in order to collect the desired statistics

class Stats {

    // ---------------------------- REPORT -----------------------------
    PrintWriter out = null;
    long startTime;
    final int INF = Integer.MAX_VALUE;
    Judge judge;

    void print(String s) {
        out.print(s);
        if (CaveViewer.active) {
            CaveViewer.caveViewer.reportBuffer.append(s);
        }
    }
    void println(String s) {
        print(s + "\n");
    }

    // this function gets called once at the start of the process
    public Stats(String args[]) {
        if (!CaveGen.showStats) return;
        if (CaveGen.findGoodLayouts || CaveGen.judgeActive) {
            judge = new Judge(this);
            Parser.readConfigFiles();
            judge.readRankFile();
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
            print("CaveGen ");
            for (String s: args) {
                print(s + " ");
            }
            println("\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    int caveGenCount = 0;
    int maxCountObj = 16;
    int observedMax = 0;
    int countObj[] = new int[maxCountObj];
    int missingTreasureCount = 0;

    // this function gets called once for every sublevel g that generates
    void analyze(CaveGen g) {
        caveGenCount += 1; 

        // count the number of purple flowers
        if (CaveGen.countObject.length() > 0) {
            int num = 0;
            for (Teki t: g.placedTekis) {
                if (t.tekiName.equalsIgnoreCase(CaveGen.countObject))
                    num += 1;
            }
            if (num >= observedMax && CaveGen.countObject.equalsIgnoreCase("egg")) {
                observedMax = num;
                println("Max objects: " + num + " in " + g.specialCaveInfoName + " " + g.sublevel + " " + Drawer.seedToString(g.initialSeed));
            }
            if (num >= maxCountObj) num = maxCountObj-1;
            countObj[num] += 1;
        }

        // report about missing treasures
        // print the seed everytime we see a missing treasure
        int minTreasure = 0, actualTreasure = 0;
        for (Item t: g.spawnItem) { minTreasure += t.min; }
        for (Teki t: g.spawnTekiConsolidated) { if (t.itemInside != null) minTreasure += t.min; }
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
            println("Missing treasure: " + g.specialCaveInfoName + " " + g.sublevel + " " + Drawer.seedToString(g.initialSeed));
            missingTreasureCount += 1;
        }

        if (CaveGen.findGoodLayouts) {
            if (missingUnexpectedTreasure)
                CaveGen.imageToggle = false;
            else 
                judge.findGoodLayouts(g);
        }

        if (CaveGen.judgeActive)
            judge.judge(g);
        
    }

    // this function gets called once at the end of the process
    void createReport() {

        if (CaveGen.judgeActive && CaveGen.judgeCombine) {
            judge.printSortedCombinedList();
        }

        // report about purple flowers
        if (CaveGen.countObject.length() > 0) {
            println("\n" + CaveGen.countObject + " distribution: ");
            for (int i = 0; i < maxCountObj; i++) {
                println(i + (i==maxCountObj-1?"+":"") + ": " + countObj[i]);
            }
        }
        
        // report about missing treasures
        println("Missing treasure count: " + missingTreasureCount);

        println("\nGenerated " + caveGenCount + " sublevels.");
        println("Total run time: " + (System.currentTimeMillis()-startTime)/1000.0 + "s");

        out.close();
    }



}
