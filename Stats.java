import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

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
            judge.readRankFile();
        }
        try {
            startTime = System.currentTimeMillis();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String dateString = dtf.format(now);
            String output = CaveGen.outputFolder.replace("/","");
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

        if (CaveGen.judgeActive)
            judge.judge(g);

        if (CaveGen.findGoodLayouts)
            judge.findGoodLayouts(g);

    }

    // this function gets called once at the end of the process
    void createReport() {

        if (CaveGen.judgeActive && CaveGen.judgeCombine) {
            judge.printSortedCombinedList();
        }

        if (CaveGen.judgeActive && CaveGen.judgeVsAvg) {
            Arrays.sort(judge.judgeVsAvgCumScores);
            println("Judge overall score: " + String.format("%.2f",judge.judgeVsAvgCumScore) + " (avg " + String.format("%.2f",judge.judgeVsAvgCumScore-judge.judgeVsAvgCumScores[judge.judgeVsAvgCumScores.length/2])+")");
            int rank = 0;
            for (; rank < judge.judgeVsAvgCumScores.length; rank++) {
                if (judge.judgeVsAvgCumScore < judge.judgeVsAvgCumScores[rank])
                    break;
            }
            println("Judge overall rank: " + String.format("%.2f%%", rank*100.0/judge.judgeVsAvgCumScores.length));
            print("Distribution:");
            for (int i = 0; i < 100; i++) {
                print(" " + String.format("%.2f", judge.judgeVsAvgCumScores[(int)((i+0.5)*judge.judgeVsAvgCumScores.length / 100)]));
            }
            println("");
            System.out.println("Judge overall score: " + String.format("%.2f",judge.judgeVsAvgCumScore) + " (avg " + String.format("%.2f",judge.judgeVsAvgCumScore-judge.judgeVsAvgCumScores[judge.judgeVsAvgCumScores.length/2])+")");
            System.out.println("Judge overall rank: " + String.format("%.2f%%", rank*100.0/judge.judgeVsAvgCumScores.length));
        }

        // report about sublevel objects
        if (CaveGen.countObject.length() > 0) {
            println("\n" + CaveGen.countObject + " distribution: ");
            for (int i = 0; i < maxCountObj; i++) {
                println(i + (i==maxCountObj-1?"+":"") + ": " + countObj[i]);
            }
        }

        println("\nGenerated " + caveGenCount + " sublevels.");
        println("Total run time: " + (System.currentTimeMillis()-startTime)/1000.0 + "s");

        out.close();
    }



}
