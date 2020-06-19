import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Judge {

    Stats stats;

    Judge(Stats s) {
        stats = s;
    }

/* 

judge [pod at (default for story) key cmat score (default for challenge)]
<1 or >99 : only output top/bottom percentile of results (for images and prints) as compared to the distribution of 10M random seeds.
combine: combine results across seeds for the final sorted list output

pod: output is [expected pokos + collected pokos] / 2 - seconds
at: output is -seconds
	need a better heuristic for these two, considering treasure paths...
	at should consider purple flowers
cmat/cmal: output is -seconds
	every enemy has a treasure so fight times are not an issue for cmat. probably use something similar to score
	cmal: enemies aren't really an issue either, only key location + gates + hole location
score: output is pokos * 10 + [start pikis+8*queen candypops] * 10 + start time - seconds / 2
	find good layouts is pretty good, just need to handle gates
defaults: pod/score
High score is always best...



special cases:
pod sh 6
pod gk 4
ch 16 breadbugs?

if prints are active, each score (that passes the filter is output)
and then a sorted list is printed at the end (if stats are active) for all that pass the filter.

if images are active, then there is an image toggle to pass the filter.

*/

    HashMap<String, Double> scoreMap = new HashMap<String, Double>();
    HashMap<String, Double> rankMap = new HashMap<String, Double>();
    
    HashMap<String, Double> seedAggregatedScoreMap = new HashMap<String, Double>();
    HashSet<String> seeds = new HashSet<String>();

    void judge(CaveGen g) {

        // calculate score... (in this function, "score" is based on jhawk's heuristics, not the game's version of score)
        double score = 0;
        
        if (CaveGen.judgeType.equals("default") && CaveGen.hardMode || CaveGen.judgeType.equals("pod")) {

        } 
        
        else if (CaveGen.judgeType.equals("at")) {

        } 
        
        else if (CaveGen.judgeType.equals("default") && CaveGen.challengeMode || CaveGen.judgeType.equals("score")) {

        } 
        
        else if (CaveGen.judgeType.equals("cmat")) {

        } 
        
        else if (CaveGen.judgeType.equals("key")) {
            for (Item t: g.placedItems) {
                if (t.itemName.equals("key"))
                    score += t.spawnPoint.mapUnit.unitScore;
            }
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null && t.itemInside.equals("key"))
                    score += t.spawnPoint.mapUnit.unitScore;
            }
        }

        // calculate rank...
        double rank = scoreToRank(score);
    
        // add to dictionaries
        String seedStr = Drawer.seedToString(g.initialSeed);
        String s = CaveGen.specialCaveInfoName + "-" + CaveGen.sublevel + " " + seedStr;
        scoreMap.put(s, score);
        rankMap.put(s, rank);

        if (seeds.contains(seedStr)) {
            seedAggregatedScoreMap.put(seedStr, seedAggregatedScoreMap.get(seedStr) + score);
        } else {
            seeds.add(seedStr);
            seedAggregatedScoreMap.put(seedStr, score);
        }

        // TODO check if passes filter
        if (filter(score, rank)) {
            if (CaveGen.numToGenerate < 4096) {
                stats.println("Judge: " + s  + " -> " + score + " (" + rank + "%)");
                if (CaveGen.prints)
                    System.out.println("Judge: " + s  + " -> " + score + " (" + rank + "%)");
            }
            CaveGen.imageToggle = true;
        }
        else {
            CaveGen.imageToggle = false;
        }
        
    }

    double scoreToRank(double score) {
        // TODO calculate rank from rank file
        return 0;
    }

    boolean filter(double score, double rank) {
        if (CaveGen.judgeFilterScore > 0 && CaveGen.judgeFilterScore > score) return false;
        if (CaveGen.judgeFilterScore < 0 && -CaveGen.judgeFilterScore < score) return false;
        // TODO rank filter
        return true;
    }
    
    class ScoredSeed {
        String seed;
        double score;
        ScoredSeed(String s,double sc) { seed=s; score=sc; }
    }

    void printSortedList() {

        String sublevelString = CaveGen.specialCaveInfoName + "-" + CaveGen.sublevel;

        ArrayList<ScoredSeed> ss = new ArrayList<ScoredSeed>(seeds.size());
        for (String s: seeds) {
            ss.add(new ScoredSeed(s, scoreMap.get(sublevelString + " " + s)));
        }

        Collections.sort(ss, new Comparator<ScoredSeed>() {
            public int compare(ScoredSeed s1, ScoredSeed s2) {
                if (s1.score < s2.score) return 1;
                if (s1.score > s2.score) return -1;
                return s1.seed.compareTo(s2.seed);
            }
        });

        stats.println("\nJudge sorted list " + sublevelString + ":");

        for (ScoredSeed s: ss) {
            stats.println(s.seed  + " -> " + s.score + " (" + rankMap.get(sublevelString + " " + s.seed) + "%)");
        }

        if (CaveGen.judgeRankFile) {
            try {
                BufferedWriter wr = new BufferedWriter(new FileWriter("files/rank_file.txt", true));
                // TODO write rank file
                wr.close();
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }

        // Sort the images in CaveViewer by score for ease of viewing
        if (CaveViewer.active) {
            int l = CaveViewer.caveViewer.nameBuffer.size();
            for (int i = 0; i < l; i++)
                for (int j = i+1; j < l; j++) 
                    if (CaveViewer.caveViewer.nameBuffer.get(i).contains(sublevelString) && CaveViewer.caveViewer.nameBuffer.get(j).contains(sublevelString))
                        if (scoreMap.get(CaveViewer.caveViewer.nameBuffer.get(i)) < scoreMap.get(CaveViewer.caveViewer.nameBuffer.get(j))) {
                            String tempName = CaveViewer.caveViewer.nameBuffer.get(i);
                            CaveViewer.caveViewer.nameBuffer.set(i, CaveViewer.caveViewer.nameBuffer.get(j));
                            CaveViewer.caveViewer.nameBuffer.set(j, tempName);
                            BufferedImage tempImg = CaveViewer.caveViewer.imageBuffer.get(i);
                            CaveViewer.caveViewer.imageBuffer.set(i, CaveViewer.caveViewer.imageBuffer.get(j));
                            CaveViewer.caveViewer.imageBuffer.set(j, tempImg);
                        }            
        }

    }

    void readRankFile() {
        // TODO
    }

    void printSortedCombinedList() {

        // combined ranks/filters are not supported.

        stats.println("\nJudge sorted combined list:");

        ArrayList<ScoredSeed> ss = new ArrayList<ScoredSeed>(seeds.size());
        for (String s: seeds) {
            ss.add(new ScoredSeed(s, seedAggregatedScoreMap.get(s)));
        }

        Collections.sort(ss, new Comparator<ScoredSeed>() {
            public int compare(ScoredSeed s1, ScoredSeed s2) {
                if (s1.score < s2.score) return 1;
                if (s1.score > s2.score) return -1;
                return s1.seed.compareTo(s2.seed);
            }
        });

        for (ScoredSeed s: ss) {
            stats.println(s.seed  + " -> " + s.score);
        }

    }


    // -------------------- Find good layouts (Old version) ---------------------------
    // this should be depricated in favor of using -judge

    void findGoodLayouts(CaveGen g) {
        // Good layout finder (story mode)
        if (CaveGen.findGoodLayouts && !CaveGen.challengeMode) {
            boolean giveWorstLayoutsInstead = CaveGen.findGoodLayoutsRatio < 0;

            ArrayList<Teki> placedTekisWithItems = new ArrayList<Teki>();
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null)
                placedTekisWithItems.add(t);
            }

            // Compute the waypoints on the shortest paths
            ArrayList<WayPoint> wpOnShortPath = new ArrayList<WayPoint>();
            for (Item t: g.placedItems) { // Treasures
                if (ignoreItems.contains(t.itemName.toLowerCase())) continue;
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }
            for (Teki t: placedTekisWithItems) { // Treasures inside enemies
                if (ignoreItems.contains(t.itemInside.toLowerCase())) continue;
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }
            for (Teki t: g.placedTekis) { // Other tekis
                if (findTekis.contains(t.tekiName.toLowerCase())) {
                    WayPoint wp = g.closestWayPoint(t.spawnPoint);
                    while (!wp.isStart) {
                        if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                        wp = wp.backWp;
                    }
                }
            }
            /*if (g.placedHole != null) {
                WayPoint wp = g.closestWayPoint(g.placedHole);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }
            if (g.placedGeyser != null) {
                WayPoint wp = g.closestWayPoint(g.placedGeyser);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }*/

            // add up distance penalty for score
            int score = 0;
            for (WayPoint wp: wpOnShortPath) {
                score += wp.distToStart - wp.backWp.distToStart;
            }    
            // add up enemy penalties for score
            for (Teki t: g.placedTekis) {
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                if (wpOnShortPath.contains(wp)) {
                    score += Parser.tekiDifficulty.get(t.tekiName.toLowerCase());
                }
            }
            // add up gate penalties for score
            for (Gate t: g.placedGates) {
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                if (g.placedHole != null && g.placedHole.mapUnit.type == 0 && g.placedHole.mapUnit.doors.get(0).spawnPoint == t.spawnPoint)
                    score += t.life / 3; // covers hole
                // if (g.placedGeyser != null && g.placedGeyser.mapUnit.type == 0 && g.placedGeyser.mapUnit.doors.get(0).spawnPoint == t.spawnPoint)
                //    score += t.life / 3; // covers geyser
                if (wpOnShortPath.contains(wp))
                    score += t.life / 3; // covers path back to ship
            }

            if (giveWorstLayoutsInstead) score *= -1;

            // keep a sorted list of the scores
            allScores.add(score);

            // only print good ones
            if (CaveGen.indexBeingGenerated > CaveGen.numToGenerate/10 && 
                score <= allScores.get((int)(allScores.size()*Math.abs(CaveGen.findGoodLayoutsRatio))) 
                || score == allScores.get(0) && CaveGen.indexBeingGenerated > CaveGen.numToGenerate/40) {
                CaveGen.imageToggle = true;
                stats.println("GoodLayoutScore: " + Drawer.seedToString(g.initialSeed) + " -> " + score);
            }
            else {
                CaveGen.imageToggle = false;
            }

        }

        // good layout finder (challenge mode)
        if (CaveGen.findGoodLayouts && CaveGen.challengeMode) {
            boolean giveWorstLayoutsInstead = CaveGen.findGoodLayoutsRatio < 0;

            // compute the number of pokos availible
            int pokosAvailible = 0;
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (plantNames.contains("," + name + ",")) continue;
                if (hazardNames.contains("," + name + ",")) continue;
                if (name.equalsIgnoreCase("egg"))
                    pokosAvailible += 10; // mitites
                else if (!noCarcassNames.contains("," + name + ",") && !name.contains("pom"))
                    pokosAvailible += Parser.pokos.get(t.tekiName.toLowerCase());
                if (t.itemInside != null)
                    pokosAvailible += Parser.pokos.get(t.itemInside.toLowerCase());
            }
            for (Item t: g.placedItems)
                pokosAvailible += Parser.pokos.get(t.itemName.toLowerCase());

            // compute the number of pikmin*seconds required to complete the level
            float pikminSeconds = 0;
            for (Teki t: g.placedTekis) {
                if (plantNames.contains("," + t.tekiName.toLowerCase() + ",")) continue;
                if (hazardNames.contains("," + t.tekiName.toLowerCase() + ",")) continue;
                pikminSeconds += fglWorkFunction(g, t.tekiName, t.spawnPoint);
                if (t.itemInside != null)
                    pikminSeconds += fglWorkFunction(g, t.itemInside, t.spawnPoint);
            }
            for (Item t: g.placedItems) {
                pikminSeconds += fglWorkFunction(g, t.itemName, t.spawnPoint);
            }
            pikminSeconds += fglWorkFunction(g, "hole", g.placedHole);
            pikminSeconds += fglWorkFunction(g, "geyser", g.placedGeyser);
            // gates??
            // hazards??
            
            int score = -pokosAvailible * 1000 + (int)(pikminSeconds/2);
            if (giveWorstLayoutsInstead) score *= -1;

            // keep a sorted list of the scores
            allScores.add(score);

            // only print good ones
            if (CaveGen.indexBeingGenerated > CaveGen.numToGenerate/10 && 
                score <= allScores.get((int)(allScores.size()*Math.abs(CaveGen.findGoodLayoutsRatio))) 
                || score == allScores.get(0) && CaveGen.indexBeingGenerated > CaveGen.numToGenerate/40) {
                CaveGen.imageToggle = true;
                stats.println("GoodLayoutScore: " + Drawer.seedToString(g.initialSeed) + " -> " + Math.abs(score));
            }
            else {
                CaveGen.imageToggle = false;
            }
        }
    }

    
    SortedList<Integer> allScores = new SortedList<Integer>(Comparator.naturalOrder());
    String plantNames = ",ooinu_s,ooinu_l,wakame_s,wakame_l,kareooinu_s,kareooinu_l,daiodored,"
        + "daiodogreen,clover,hikarikinoko,tanpopo,zenmai,nekojarashi,tukushi,magaret,watage,chiyogami,";     
    String hazardNames = ",gashiba,hiba,elechiba,rock,";
    String noCarcassNames = ",wealthy,fart,kogane,mar,hanachirashi,damagumo,bigfoot,bigtreasure,qurione,baby,bomb,egg,kurage,onikurage,bombotakara,blackman,tyre,";
    String ignoreItems = "g_futa_kyodo,flower_blue,tape_blue,kinoko_doku,flower_red,futa_a_silver,cookie_m_l,chocolate";
    String findTekis = ""; //"whitepom,blackpom";

    private float fglWorkFunction(CaveGen g, String name, SpawnPoint sp) {
        if (sp == null) return 0;
        name = name.toLowerCase();
        if (name.equals("hole"))
            return g.closestWayPoint(sp).distToStart / 170.0f * 4;
        if (name.equals("geyser"))
            return g.closestWayPoint(sp).distToStart / 170.0f * 20;
        if (name.equals("egg"))
            return 7 * 10 * g.closestWayPoint(sp).distToStart / 580.0f;
        if (name.contains("pom"))
            return g.closestWayPoint(sp).distToStart / 170.0f * 10;
        if (noCarcassNames.contains(","+name+",")) return 0;
        int minCarry = Parser.minCarry.get(name);
        int maxCarry = Parser.maxCarry.get(name);
        return 7 * minCarry * g.closestWayPoint(sp).distToStart
                    / (220.0f + 180.0f * (2 * minCarry - minCarry + 1) / maxCarry);
    }

}