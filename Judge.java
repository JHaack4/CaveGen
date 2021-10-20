import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Judge {

    Stats stats;

    Judge(Stats s) {
        stats = s;
    }

    HashSet<String> plantNames = hashSet("ooinu_s,ooinu_l,wakame_s,wakame_l,kareooinu_s,kareooinu_l,daiodored,daiodogreen,clover,hikarikinoko,tanpopo,zenmai,nekojarashi,tukushi,magaret,watage,chiyogami");     
    HashSet<String> hazardNames = hashSet("gashiba,hiba,elechiba,rock");
    HashSet<String> noCarcassNames = hashSet("wealthy,fart,kogane,mar,hanachirashi,damagumo,bigfoot,bigtreasure,qurione,baby,bomb,egg,kurage,onikurage,bombotakara,blackman,tyre,houdai,ooinu_s,ooinu_l,wakame_s,wakame_l,kareooinu_s,kareooinu_l,daiodored,daiodogreen,clover,hikarikinoko,tanpopo,zenmai,nekojarashi,tukushi,magaret,watage,chiyogami,gashiba,hiba,elechiba,rock,bluepom,redpom,yellowpom,blackpom,whitepom,randpom,pom");
    HashSet<String> pomNames = hashSet("bluepom,redpom,yellowpom,blackpom,whitepom,randpom,pom");
    HashSet<String> ignoreTreasuresPoD = hashSet("kinoko_doku,bird_hane,futa_a_silver,flower_red,flower_blue,g_futa_kyodo,dia_a_green,makigai,mojiban,nut,dashboots,cookie_m_l,bane_red,chocolate,tape_blue");
    HashSet<String> optionalTreasuresPoD = hashSet("chess_queen_white,gum_tape_s,chess_king_white,chess_queen_black,leaf_normal,locket"); // bey_goma (SL), others on sh6, scx1, fc6, cos1, cos4
    HashSet<String> purple20 = hashSet("EC-2,FC-1,HoB-2,CoS-2,GK-2,SR-2"), white20 = hashSet("WFG-3,BK-1,SH-2,SR-1");

/* 

judge [pod at (default for story) key cmat score (default for challenge)]
<1% or >99% : only output top/bottom percentile of results (for images and prints) as compared to the distribution of 100K random seeds.
combine: combine results across seeds for the final sorted list output

pod: output is seconds - [expected pokos - collected pokos] / 2
at: output is seconds (penalties included for missing purple flowers and treasures)
cmat/cmal: output is seconds
attk (score attack): output is ( pokos * 10 + [start pikis+8*queen candypops] * 10 + start time - seconds / 2 ) (meant to guess the final score)
	find good layouts is pretty good, just need to handle gates
defaults: pod/score
low score is better

alcove: output is number of alcoves

special cases:
pod sh 6 - (todo?)
pod breadbugs/high treasures

*/

    HashMap<String, Double> scoreMap = new HashMap<String, Double>();
    HashMap<String, Double> rankMap = new HashMap<String, Double>();
    
    HashMap<String, Double> seedAggregatedScoreMap = new HashMap<String, Double>();
    HashSet<String> seeds = new HashSet<String>();

    double judgeVsAvgCumScores[] = new double[100000], judgeVsAvgCumScore = 0;

    void judge(CaveGen g) {

        // calculate score... (in this function, "score" is based on jhawk's heuristics, not the game's version of score)
        double score = 0;

        ArrayList<Teki> placedTekisWithItemsInside;
        placedTekisWithItemsInside = new ArrayList<Teki>();
        for (Teki t: g.placedTekis) {
            if (t.itemInside != null) {
                placedTekisWithItemsInside.add(t);
            }
        }
        
        if (CaveGen.judgeType.equals("score")) { // sum of scores, using the game's version of score
            int treasureCount = 0;
            for (Item t: g.placedItems) {
                score += t.spawnPoint.mapUnit.unitScore;
                treasureCount += 1;
            }
            for (Teki t: placedTekisWithItemsInside) {
                if (t.spawnPoint.type == 5)
                    score += t.spawnPoint.door.doorScore;
                else
                    score += t.spawnPoint.mapUnit.unitScore;
                treasureCount += 1;
            }
            score += 100000 * (expectedNumTreasures - treasureCount); // add penalty for missing treasures
        } 

        else if (CaveGen.judgeType.equals("pod")) {

            HashSet<SpawnPoint> visited = new HashSet<SpawnPoint>();
            HashSet<SpawnPoint> unVisited = new HashSet<SpawnPoint>();
            visited.add(g.placedStart);

            int numPokosCollected = 0;
            int numTreasuresCollected = 0;
            for (Item t: g.placedItems) {
                String name = t.itemName.toLowerCase();
                if (!ignoreTreasuresPoD.contains(name) && !optionalTreasuresPoD.contains(name)) {
                    numTreasuresCollected += 1;
                    numPokosCollected += Parser.pokos.get(name);
                    unVisited.add(t.spawnPoint);
                }
            }
            for (Teki t: placedTekisWithItemsInside) {
                String name = t.itemInside;
                if (!ignoreTreasuresPoD.contains(name) && !optionalTreasuresPoD.contains(name)) {
                    numTreasuresCollected += 1;
                    numPokosCollected += Parser.pokos.get(name);
                    unVisited.add(t.spawnPoint);
                }
            }
        
            // compute pikmin*seconds required to collect all treasures
            float pikminSeconds = 0;
            float activePikmin = 0;

            for (Item t: g.placedItems) {
                String name = t.itemName.toLowerCase();
                if (ignoreTreasuresPoD.contains(name) || optionalTreasuresPoD.contains(name)) continue;
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                float dig = 0;
                if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                }
                g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
                activePikmin += maxCarry;
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
            }

            for (Teki t: placedTekisWithItemsInside) {
                String name = t.itemInside.toLowerCase();
                if (ignoreTreasuresPoD.contains(name) || optionalTreasuresPoD.contains(name)) continue;
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                float dig = 0;
                //if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                //    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                //}
                g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
                activePikmin += maxCarry;
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
            }

            float carrySeconds = pikminSeconds == 0 ? 0 : pikminSeconds / Math.min(pikiCount, activePikmin);

            // compute the minimum spanning tree, and add the time needed for the captain to walk it
            float walkingSeconds = 0;
            while (unVisited.size() > 0) {
                float minDist = CaveGen.INF;
                SpawnPoint minU = null;
                for (SpawnPoint v: visited) {
                    for (SpawnPoint u: unVisited) {
                        float dist = v == g.placedStart ? g.spawnPointDistToStart(u) : g.spawnPointDist(v, u);
                        if (dist < minDist) {
                            minU = u;
                            minDist = dist;
                        }
                    }
                }
                walkingSeconds += (minDist / olimarSpeedCm);
                unVisited.remove(minU);
                visited.add(minU);
            }

            // walking to hole
            if (g.placedHole != null) {
                walkingSeconds += g.spawnPointDistToStart(g.placedHole) / olimarSpeedCm / (1 + numTreasuresCollected);
            } else if (g.placedGeyser != null) {
                walkingSeconds += g.spawnPointDistToStart(g.placedGeyser) / olimarSpeedCm / (1 + numTreasuresCollected);
            } else {
                walkingSeconds += 300; // ho holes
            }
            
            // Teki/Hazards that are in the way of treasures, bug pokos
            float hazardSeconds = 0;
            int bugPokos = 0;
            computeWaypointCarryableGraph(g, ""); // pod treasures (already marked above)
            for (Teki t: g.placedTekis) {
                if (isInTheWay(g, t)) {
                    String name = t.tekiName.toLowerCase();
                    hazardSeconds += Parser.tekiDifficultyJudgeSec.get(name) / 2.0f;
                    if (!noCarcassNames.contains(name) && g.spawnPointDistToStart(t.spawnPoint) / olimarSpeedCm < walkingSeconds * 0.4f) {
                        bugPokos += Parser.pokos.get(name);
                    }
                }
            }

            // collecting optional treasures (note, small flaw, these don't check for enemies in the way.)
            float optionalTreasuresSeconds = 0;

            for (Item t: g.placedItems) {
                String name = t.itemName.toLowerCase();
                if (!optionalTreasuresPoD.contains(name)) continue;
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                float dig = 0;
                if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                }
                float minDistToAlreadyVisitedWp = 128000;
                for (MapUnit m: g.placedMapUnits) {
                    for (WayPoint wp: m.wayPoints) {
                        if (!wp.hasCarryableBehind && !wp.isStart) continue;
                        minDistToAlreadyVisitedWp = Math.min(minDistToAlreadyVisitedWp, g.spawnPointWayPointDist(t.spawnPoint, wp));
                    }
                }
                float gateCheck = 0; 
                if (g.placedGates.size() > 0) {
                    if (t.spawnPoint.mapUnit != null && t.spawnPoint.mapUnit.type == 0) { // only checking if in an alcove.
                        for (Gate gt: g.placedGates) {
                            if (gt.posX == t.spawnPoint.mapUnit.doors.get(0).posX && gt.posZ == t.spawnPoint.mapUnit.doors.get(0).posZ) {
                                gateCheck += Math.max(6, 3 + gt.life * gateLifeMultiplier / (pikiAttackValue * pikiCount));
                            }
                        }
                    }
                }
                float pikiCarryValueUse = g.specialCaveInfoName.equals("FC") && g.sublevel == 6 ? 2.4f : pikiCarryValue; // FC6 water override
                float timeNeeded = 10  // cutscene
                    + 2 * minDistToAlreadyVisitedWp / olimarSpeedCm // additional walking, use 2x since you also need to walk back
                    + loadTimeSecondsStory + dig / (pikiCount / (numTreasuresCollected+1)) + gateCheck // load+dig+gate
                    + g.spawnPointDistToStart(t.spawnPoint) // carry cost
                        * carryMultiplier / (220.0f + 180.0f * (pikiCarryValueUse * carry - minCarry + 1) / maxCarry);
                //System.out.println("fgt" + 0 + " wlk" + (2*minDistToAlreadyVisitedWp / olimarSpeedCm) + " load" + loadTimeSecondsStory 
                //        + " dig" + (dig / (pikiCount / (numTreasuresCollected+1))) + " carry" + (g.spawnPointDistToStart(t.spawnPoint)
                //        * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry)) + " gate"+gateCheck);
                int pokos = Parser.pokos.get(name);
                float timeSave = Math.min(pokos / 4, pokos / 1.5f - timeNeeded); // 1.5 extra pokos = 1s (more than bugs due to optionality)
                //System.out.println("opt " + name + " " + timeSave);
                if (timeSave > 0) {
                    //System.out.println("collect");
                    optionalTreasuresSeconds -= timeSave;
                }
            }

            for (Teki t: placedTekisWithItemsInside) {
                String name = t.itemInside.toLowerCase();
                if (!optionalTreasuresPoD.contains(name)) continue;
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                float dig = 0;
                //if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                //    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                //}
                float minDistToAlreadyVisitedWp = 128000;
                for (MapUnit m: g.placedMapUnits) {
                    for (WayPoint wp: m.wayPoints) {
                        if (!wp.hasCarryableBehind) continue;
                        minDistToAlreadyVisitedWp = Math.min(minDistToAlreadyVisitedWp, g.spawnPointWayPointDist(t.spawnPoint, wp));
                    }
                }
                float timeNeeded = 10  // cutscene
                    + Parser.tekiDifficultyJudgeSec.get(t.tekiName.toLowerCase()) // enemy fight
                    + 2 * minDistToAlreadyVisitedWp / olimarSpeedCm // additional walking, use 2x since you also need to walk back
                    + loadTimeSecondsStory + dig / (pikiCount / (numTreasuresCollected+1))  // load+dig
                    + g.spawnPointDistToStart(t.spawnPoint) // carry cost
                        * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
                //System.out.println("fgt" + Parser.tekiDifficultyJudgeSec.get(t.tekiName.toLowerCase()) + " wlk" + (2*minDistToAlreadyVisitedWp / olimarSpeedCm) + " load" + loadTimeSecondsStory 
                //    + " dig" + (dig / (pikiCount / (numTreasuresCollected+1))) + " carry" + (g.spawnPointDistToStart(t.spawnPoint)
                //    * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry)));
                int pokos = Parser.pokos.get(name);
                float timeSave = Math.min(pokos / 4, pokos / 1.5f - timeNeeded); // 1.5 extra pokos = 1s (more than bugs due to optionality)
                //System.out.println("opt " + name + " " + timeSave);
                if (timeSave > 0) {
                    //System.out.println("collect");
                    optionalTreasuresSeconds -= timeSave;
                }
                continue;
            }


            // gates that are in the way of holes/treasures
            float gateSeconds = 0;
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "h"); // holes (+ pod treasures)
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        gateSeconds += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * pikiCount));
                    }
                }
            }

            float pokoSeconds = expectedNumPokosPoD - numPokosCollected - bugPokos;
            pokoSeconds /= 2.0f; // 2 bug pokos = 1s

            // adjustments
            float adjSeconds = 0;
            if (CaveGen.specialCaveInfoName.equals("GK")) {
                // gk2 shortcuts
                if (CaveGen.sublevel == 2) {
                    for (Item i: g.placedItems) {
                        if (i.itemName.equalsIgnoreCase("g_futa_kyusyu")) {
                            if (i.spawnPoint.spawnListIdx == 6 || i.spawnPoint.spawnListIdx == 7)
                                walkingSeconds = 4.0f;
                        }
                    }
                    hazardSeconds = 0;
                }
                // high treasures
                for (Item t: g.placedItems) {
                    if (ignoreTreasuresPoD.contains(t.itemName.toLowerCase())) continue;
                    if (t.spawnPoint.y == 100 || t.spawnPoint.y == 125) {
                        adjSeconds += 12;
                        //System.out.println("high treasure");
                    }
                }
                // breadbugs
                for (Item i: g.placedItems) {
                    String name = i.itemName.toLowerCase();
                    if (ignoreTreasuresPoD.contains(name)) continue;
                    if (i.spawnPoint.y >= 30) continue; // high
                    if (Parser.minCarry.get(name) > 10) continue; // too heavy
                    if (g.spawnPointDistToStart(i.spawnPoint) < 170) continue; // too close
                    float worst = 0;
                    for (Teki t: g.placedTekis) {
                        if (t.tekiName.equalsIgnoreCase("panmodoki")) {
                            float closeness = g.spawnPointDist(t.spawnPoint, i.spawnPoint) / g.spawnPointDistToStart(i.spawnPoint);
                            if (closeness < 0.25) {
                                worst = Math.max(worst, Math.min(15, 1.5f / closeness));
                            }
                        }
                    }
                    adjSeconds += worst;
                    //if (worst > 0) System.out.println("breadbug " + worst + " " + name);
                }
            }

            //System.out.println("carry" + carrySeconds + " walk" + walkingSeconds + " haz" + hazardSeconds + " gate" + gateSeconds);
            //System.out.println("poko" + pokoSeconds + " adj" + adjSeconds + " opt" + optionalTreasuresSeconds);
            score = walkingSeconds + carrySeconds + hazardSeconds + gateSeconds + optionalTreasuresSeconds
                        + pokoSeconds + adjSeconds + 10*numTreasuresCollected;
        } 
        
        else if (CaveGen.judgeType.equals("at")) {
            int treasureCount = g.placedItems.size() + placedTekisWithItemsInside.size();
            int numPurpleCandypop = 0;
            for (Teki t: g.placedTekis)
                if (t.tekiName.equalsIgnoreCase("blackpom"))
                    numPurpleCandypop += 1;

            // compute the minimum spanning tree, and add the time needed for the captain to walk it
            float walkingSeconds = 0;
            HashSet<SpawnPoint> visited = new HashSet<SpawnPoint>();
            HashSet<SpawnPoint> unVisited = new HashSet<SpawnPoint>();
            visited.add(g.placedStart);
            for (Item t: g.placedItems) unVisited.add(t.spawnPoint);
            for (Teki t: placedTekisWithItemsInside) unVisited.add(t.spawnPoint);
        
            while (unVisited.size() > 0) {
                float minDist = CaveGen.INF;
                SpawnPoint minU = null;
                for (SpawnPoint v: visited) {
                    for (SpawnPoint u: unVisited) {
                        float dist = v == g.placedStart ? g.spawnPointDistToStart(u) : g.spawnPointDist(v, u);
                        if (dist < minDist) {
                            minU = u;
                            minDist = dist;
                        }
                    }
                }
                walkingSeconds += (minDist / olimarSpeedCm);
                unVisited.remove(minU);
                visited.add(minU);
            }

            // walking to hole
            if (g.placedHole != null) {
                walkingSeconds += g.spawnPointDistToStart(g.placedHole) / olimarSpeedCm / (1 + expectedNumTreasures);
            } else if (g.placedGeyser != null) {
                walkingSeconds += g.spawnPointDistToStart(g.placedGeyser) / olimarSpeedCm / (1 + expectedNumTreasures);
            } else {
                walkingSeconds += 300; // ho holes
            }

            // purple candypops
            if (!purple20.contains(sublevelId) && expectedNumPurpleCandypop > 0) {
                for (Teki t: g.placedTekis) {
                    if (t.tekiName.equalsIgnoreCase("blackpom")) {
                        walkingSeconds += g.spawnPointDistToStart(t.spawnPoint) / olimarSpeedCm / (2 + expectedNumTreasures);
                    }
                }
            }

            // compute pikmin*seconds required to collect all treasures
            float pikminSeconds = 0;
            float activePikmin = 0;

            for (Item t: g.placedItems) {
                String name = t.itemName.toLowerCase();
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                activePikmin += maxCarry;
                float dig = 0;
                if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                }
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
            }

            for (Teki t: placedTekisWithItemsInside) {
                String name = t.itemInside.toLowerCase();
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                activePikmin += maxCarry;
                float dig = 0;
                //if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                //    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                //}
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
            }

            float carrySeconds = pikminSeconds == 0 ? 0 : pikminSeconds / Math.min(pikiCount, activePikmin);
            
            // Teki/Hazards that are in the way of treasures
            float hazardSeconds = 0;
            computeWaypointCarryableGraph(g, "t"); // treasures
            for (Teki t: g.placedTekis) {
                if (isInTheWay(g, t)) {
                    String name = t.tekiName.toLowerCase();
                    hazardSeconds += Parser.tekiDifficultyJudgeSec.get(name) / 2.0f;
                }
            }

            // gates that are in the way of holes/treasures
            float gateSeconds = 0;
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "htp"); // holes + treasures + purple candypops
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        gateSeconds += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * pikiCount));
                    }
                }
            }

            // penalties
            float purpPenalty = purple20.contains(sublevelId) ? 0 : (60 * (expectedNumPurpleCandypop - numPurpleCandypop));
            float treasurePenalty = treasureCount < expectedNumTreasures ? 300 : 0;

            //System.out.println("carry" + carrySeconds + " walk" + walkingSeconds + " haz" + hazardSeconds + " gate" + gateSeconds);
            //System.out.println("tpen" + treasurePenalty + " ppen" + purpPenalty);
            score = walkingSeconds + carrySeconds + hazardSeconds + gateSeconds
                        + treasurePenalty + purpPenalty + 10*treasureCount;
            
        } 
        
        else if (CaveGen.judgeType.equals("attk")) {
            // compute the number of pokos availible on this layout
            int pokosAvailible = 0;
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (name.equalsIgnoreCase("egg"))
                    pokosAvailible += 10; // mitites
                else if (!noCarcassNames.contains(name))
                    pokosAvailible += Parser.pokos.get(name);
                if (t.itemInside != null)
                    pokosAvailible += Parser.pokos.get(t.itemInside.toLowerCase());
            }
            for (Item t: g.placedItems)
                pokosAvailible += Parser.pokos.get(t.itemName.toLowerCase());

            // compute the number of pikmin*seconds required to complete the level
            float pikminSeconds = 0;
            int numHazards = 0;
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (plantNames.contains(name)) continue;
                if (hazardNames.contains(name)) {
                    numHazards += 1;
                    continue;
                }
                pikminSeconds += Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name);
                pikminSeconds += judgeWorkFunction(g, t.tekiName, t.spawnPoint);
                //System.out.println(name + " " + (Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name)) + " " + judgeWorkFunction(g, t.tekiName, t.spawnPoint));
                if (t.itemInside != null)
                    pikminSeconds += judgeWorkFunction(g, t.itemInside, t.spawnPoint);
                //if (t.itemInside != null) System.out.println("-" + t.itemInside + " " + judgeWorkFunction(g, t.itemInside, t.spawnPoint));
            }
            for (Item t: g.placedItems) {
                pikminSeconds += judgeWorkFunction(g, t.itemName, t.spawnPoint);
                //System.out.println(t.itemName + " " + judgeWorkFunction(g, t.itemName, t.spawnPoint));
            }
            pikminSeconds += judgeWorkFunction(g, "hole", g.placedHole);
            //System.out.println("hole " + judgeWorkFunction(g, "hole", g.placedHole));
            pikminSeconds += judgeWorkFunction(g, "geyser", g.placedGeyser);
            //System.out.println("geyser " + judgeWorkFunction(g, "geyser", g.placedGeyser));
            
            // Hazards that are in the way of treasures/carcasses
            if (numHazards > 0) {
                computeWaypointCarryableGraph(g, "tc"); // treasures + carcasses
                for (Teki t: g.placedTekis) {
                    String name = t.tekiName.toLowerCase();
                    if (hazardNames.contains(name) && isInTheWay(g, t)) {
                        pikminSeconds += Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name);
                        //System.out.println(t.tekiName + " " + (Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name)));
                    }
                }
            }

            // gates that are in the way of holes/treasures/carcasses
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "htc"); // holes + treasures + carcasses
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        float wallCount = pikiCount / 2.0f;
                        pikminSeconds += wallCount * Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount));
                        //System.out.println("gate " + (wallCount * Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount))));
                    }
                }
            }

            // geyser breaking
            if (g.placedGeyser != null && g.holeClogged) {
                pikminSeconds += geyserHealth / pikiAttackValue;
                //System.out.println("clog " + geyserHealth / pikiAttackValue);
            }
            
            //System.out.println("cnt " + pikiCount + " attk " + pikiAttackValue + " crry " + pikiCarryValue + " strg " + pikiStrengthValue + " poko " + pokosAvailible + " time " + Parser.chTime.get(sublevelId) ); 
            float pikminEfficiencyRate = 1.0f;
            score = -pokosAvailible * 10 - pikiCount * 10 + Parser.chTime.get(sublevelId) 
                        + 0.5 * pikminSeconds / (pikiCount * pikminEfficiencyRate);
        } 
        
        else if (CaveGen.judgeType.equals("cmat")) {
            // compute the number of pikmin*seconds required to complete the level
            float pikminSeconds = 0;
            int numHazards = 0;
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (hazardNames.contains(name)) {
                    numHazards += 1;
                    continue;
                }
                if (t.itemInside == null) continue;
                pikminSeconds += Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name);
                //System.out.println(name + " " + (Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name)));
                pikminSeconds += judgeWorkFunction(g, t.itemInside, t.spawnPoint);
                //System.out.println("-" + t.itemInside + " " + judgeWorkFunction(g, t.itemInside, t.spawnPoint));
            }
            for (Item t: g.placedItems) {
                pikminSeconds += judgeWorkFunction(g, t.itemName, t.spawnPoint);
                //System.out.println(t.itemName + " " + judgeWorkFunction(g, t.itemName, t.spawnPoint));
            }
            pikminSeconds += judgeWorkFunction(g, "hole", g.placedHole);
            //System.out.println("hole " + judgeWorkFunction(g, "hole", g.placedHole));
            pikminSeconds += judgeWorkFunction(g, "geyser", g.placedGeyser);
            //System.out.println("geyser " + judgeWorkFunction(g, "geyser", g.placedGeyser));
            
            // Hazards that are in the way of treasures
            if (numHazards > 0) {
                computeWaypointCarryableGraph(g, "t"); // treasures
                for (Teki t: g.placedTekis) {
                    String name = t.tekiName.toLowerCase();
                    if (hazardNames.contains(name) && isInTheWay(g, t)) {
                        pikminSeconds += Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name);
                        //System.out.println(t.tekiName + " " + (Parser.tekiDifficultyJudgePiki.get(name) * Parser.tekiDifficultyJudgeSec.get(name)));
                    }
                }
            }

            // gates that are in the way of holes/treasures
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "ht"); // holes + treasures
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        float wallCount = pikiCount / 2.0f;
                        pikminSeconds += wallCount * Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount));
                        //System.out.println("gate " + (wallCount * Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount))));
                    }
                }
            }

            // geyser breaking
            if (g.placedGeyser != null && g.holeClogged) {
                pikminSeconds += geyserHealth / pikiAttackValue;
                //System.out.println("clog " + geyserHealth / pikiAttackValue);
            }
            
            //System.out.println("cnt " + pikiCount + " attk " + pikiAttackValue + " crry " + pikiCarryValue + " strg " + pikiStrengthValue + " time " + Parser.chTime.get(sublevelId) ); 
            float pikminEfficiencyRate = 1.0f;
            score = pikminSeconds / (pikiCount * pikminEfficiencyRate);

            boolean keyFound = false;
            for (Item t: g.placedItems) {
                if (t.itemName.equals("key")) {
                    keyFound = true;
                    break;
                }
            }
            for (Teki t: g.placedTekis) {
                if (keyFound) break;
                if (t.itemInside != null && t.itemInside.equals("key")) {
                    keyFound = true;
                    break;
                }
            }

            if (!keyFound) {
                score = 500;
            }
        } 
        
        else if (CaveGen.judgeType.equals("key")) {
            // travel time to key (including gates)
            // + max (time to travel from key to hole, time for key to return to ship fastest carry)
            // + time to break geyser
            float tripKeyGates = 0;
            float tripHoleGates = 0;
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "k"); // holes + treasures
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        float wallCount = pikiCount;
                        tripKeyGates += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount));
                    }
                }

                computeWaypointCarryableGraph(g, "h"); // holes + treasures
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        float wallCount = pikiCount;
                        tripHoleGates += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount));
                    }
                }
                tripHoleGates -= tripKeyGates;
            }

            float keyWalk = 0;
            float keyCarry = 0;
            float holeWalk = 0;
            boolean keyFound = false;
            for (Item t: g.placedItems) {
                if (t.itemName.equals("key")) {
                    int carry = Math.min(pikiCount, 3);
                    keyWalk = g.spawnPointDistToStart(t.spawnPoint) / olimarSpeedCm;
                    keyCarry = g.spawnPointDistToStart(t.spawnPoint)
                        * carryMultiplier / (220.0f + 180.0f * (fastestPikiCarryValue * carry - 1 + 1) / 3);
                    if (g.placedHole != null)
                        holeWalk = g.spawnPointDist(g.placedHole, t.spawnPoint) / olimarSpeedCm;
                    if (g.placedGeyser != null)
                        holeWalk = g.spawnPointDist(g.placedGeyser, t.spawnPoint) / olimarSpeedCm;
                    holeWalk = Math.min(holeWalk, g.spawnPointDist(g.placedStart, t.spawnPoint) / olimarSpeedCm);
                    keyFound = true;
                    break;
                }
            }
            for (Teki t: g.placedTekis) {
                if (keyFound) break;
                if (t.itemInside != null && t.itemInside.equals("key")) {
                    int carry = Math.min(pikiCount, 3);
                    keyWalk = g.spawnPointDistToStart(t.spawnPoint) / olimarSpeedCm
                            + Parser.tekiDifficultyJudgeSec.get(t.tekiName.toLowerCase());
                    keyCarry = g.spawnPointDistToStart(t.spawnPoint)
                        * carryMultiplier / (220.0f + 180.0f * (fastestPikiCarryValue * carry - 1 + 1) / 3);
                        if (g.placedHole != null)
                        holeWalk = g.spawnPointDist(g.placedHole, t.spawnPoint) / olimarSpeedCm;
                    if (g.placedGeyser != null)
                        holeWalk = g.spawnPointDist(g.placedGeyser, t.spawnPoint) / olimarSpeedCm;
                    holeWalk = Math.min(holeWalk, g.spawnPointDist(g.placedStart, t.spawnPoint) / olimarSpeedCm);
                    keyFound = true;
                    break;
                }
            }

            if (!keyFound) {
                keyWalk = 300;
            }

            /*if (CaveGen.specialCaveInfoName.equals("CH16")) {
                for (Teki t: g.placedTekis) {
                    if (t.itemInside != null && t.itemInside.equals("key")) {
                        for (Item i: g.placedItems) {
                            double dist = g.spawnPointDist(t.spawnPoint, i.spawnPoint);
                            if (dist < 70) {
                                keyWalk += dist < 40 ? 10 : 6;
                            }
                        }
                    }
                }
            }*/
            // special cases: ch6 candypop, ch26 candypop, ch16 bbgs

            float geyserBreak = 0;
            if (g.placedGeyser != null)
                geyserBreak = geyserHealth / pikiAttackValue / pikiCount;

            //System.out.println("keywalk" + keyWalk + " keygates" + tripKeyGates + " keyCarry" + keyCarry + "holewalk" + holeWalk + " holegates" + tripHoleGates + " geyserbreak" + geyserBreak + " fastestcarry" + fastestPikiCarryValue);
            score = (keyWalk + tripKeyGates)
                + Math.max(keyCarry, holeWalk + tripHoleGates)
                + geyserBreak;
        }

        else if (CaveGen.judgeType.equals("colossal")) { // this could definetly be improved someday.
            // travel time to globe (including gates)
            // + time for globe to return to ship fastest carry
            float tripKeyGates = 0;
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "m"); // globe
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        float wallCount = pikiCount;
                        tripKeyGates += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * wallCount));
                    }
                }
            }
            float keyWalk = 0;
            float keyCarry = 0;
            boolean keyFound = false;
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null && t.itemInside.equals("map01")) {
                    int carry = Math.min(pikiCount, 3);
                    keyWalk = g.spawnPointDistToStart(t.spawnPoint) / olimarSpeedCm
                            + Parser.tekiDifficultyJudgeSec.get(t.tekiName.toLowerCase());
                    keyCarry = g.spawnPointDistToStart(t.spawnPoint)
                        * carryMultiplier / (220.0f + 180.0f * (fastestPikiCarryValue * carry - 40 + 1) / 100);
                    keyFound = true;
                    break;
                }
            }
            if (!keyFound) {
                keyWalk = 100000;
            }
            
            int treasureCount = g.placedItems.size() + placedTekisWithItemsInside.size();

            // compute pikmin*seconds required to collect all treasures
            float pikminSeconds = 0;
            float activePikmin = 0;

            for (Onion o: g.placedOnions) {
                pikminSeconds += 
                        10 * pikiCount * g.spawnPointDistToStart(o.spawnPoint) / 170.0f;
            }

            for (Item t: g.placedItems) {
                String name = t.itemName.toLowerCase();
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                activePikmin += maxCarry;
                float dig = 0;
                if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                }
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry) / 1.3f;
            }

            for (Teki t: placedTekisWithItemsInside) {
                String name = t.itemInside.toLowerCase();
                int minCarry = Parser.minCarry.get(name);
                int maxCarry = Parser.maxCarry.get(name);
                int carry = maxCarry;
                activePikmin += maxCarry;
                float dig = 0;
                //if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
                //    dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
                //}
                pikminSeconds += 
                        carry * loadTimeSecondsStory + dig + // loading + digging
                        carry * g.spawnPointDistToStart(t.spawnPoint) // carry cost
                           * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry) / 1.3f;
            }

            float carrySeconds = pikminSeconds == 0 ? 0 : pikminSeconds / Math.min(pikiCount, activePikmin);
            
            // Teki/Hazards that are in the way of treasures
            float hazardSeconds = 0;
            computeWaypointCarryableGraph(g, "t"); // treasures
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (isInTheWay(g, t)) {
                    hazardSeconds += Parser.tekiDifficultyJudgeSec.get(name) / 2.0f;
                }
            }

            // gates that are in the way of holes/treasures
            float gateSeconds = 0;
            if (g.placedGates.size() > 0) {
                computeWaypointCarryableGraph(g, "htp"); // holes + treasures + purple candypops
                for (Gate t: g.placedGates) {
                    if (isInTheWay(g, t)) {
                        gateSeconds += Math.max(6, 3 + t.life * gateLifeMultiplier / (pikiAttackValue * pikiCount));
                    }
                }
            }

            float treasurePenalty = treasureCount < expectedNumTreasures ? 10000 * (expectedNumTreasures - treasureCount): 0;

            //System.out.println("carry" + carrySeconds + " walk" + walkingSeconds + " haz" + hazardSeconds + " gate" + gateSeconds);
            //System.out.println("tpen" + treasurePenalty + " ppen" + purpPenalty);
            //System.out.println("kWalk" + keyWalk + " kCarry" + keyCarry);
            //stats.println("kWalk" + keyWalk + " kCarry" + keyCarry);
            score = tripKeyGates + keyWalk + keyCarry
                        + carrySeconds + hazardSeconds + gateSeconds
                        + treasurePenalty;
            
        } 

        else if (CaveGen.judgeType.equals("mapunitcount")) {
            score = g.placedMapUnits.size();
        }

        else if (CaveGen.judgeType.equals("alcove")) {
            for (MapUnit U: g.placedMapUnits) {
                if (U.type == 0) {
                    score += 1.0;
                }
            }
        }
    
        // calculate rank
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

        if (CaveGen.judgeVsAvg) {
            judgeVsAvgCumScore += score;
        }

        // check if the image passes the filter
        if (filter(score, rank)) {
            if (CaveGen.numToGenerate < 4096 || CaveGen.judgeFilterRank != 0 || CaveGen.judgeFilterScore != 0) {
                stats.println(String.format("Judge: %s -> %.2f (%.1f%%, %s)", s, score, rank, rankBreakPoints==null?"?":""+(int)(score-rankBreakPoints[500])));
                if (CaveGen.prints)
                    System.out.println(String.format("Judge: %s -> %.2f (%.1f%%, %s)", s, score, rank,rankBreakPoints==null?"?":""+(int)(score-rankBreakPoints[500])));
            }
            CaveGen.imageToggle = true;
        }
        else {
            CaveGen.imageToggle = false;
        }
        
    }


    // units of return type is pikmin*seconds
    // used for score attack and cmat heuristics
    float judgeWorkFunction(CaveGen g, String name, SpawnPoint sp) {
        if (sp == null) return 0;
        name = name.toLowerCase();
        if (name.equals("hole"))
            return g.spawnPointDistToStart(sp) / olimarSpeedCm * holeWorkMultiplier; // walking cost
        if (name.equals("geyser"))
            return g.spawnPointDistToStart(sp) / olimarSpeedCm * geyserWorkMultiplier; // walking cost
        if (name.equals("egg")) // assume 10 mitites
            return 10 * g.spawnPointDistToStart(sp) / olimarSpeedCm + 10 * loadTimeSeconds + // walking/loading cost
                    10 * g.spawnPointDistToStart(sp) * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue)); // carry cost
        if (pomNames.contains(name))
            return g.spawnPointDistToStart(sp) / olimarSpeedCm * candypopWorkMultiplier; // walking cost
        if (noCarcassNames.contains(name)) return 0;
        int minCarry = Parser.minCarry.get(name);
        int maxCarry = Parser.maxCarry.get(name);
        int carry = (int)Math.ceil(minCarry / pikiStrengthValue);
        float dig = 0;
        if (Parser.depth.containsKey(name) && Parser.depth.get(name) > 0) {
            dig = Math.min(1, Parser.depth.get(name)/Parser.height.get(name)) * diggingHealth / pikiDigValue;
        }
        return  carry * loadTimeSeconds + dig + // loading + digging
                carry * g.spawnPointDistToStart(sp) / olimarSpeedCm + // walking cost
                carry * g.spawnPointDistToStart(sp) // carry cost
                   * carryMultiplier / (220.0f + 180.0f * (pikiCarryValue * carry - minCarry + 1) / maxCarry);
    }
    // CARRY NOTES
        // carry velocity (wiki) is 220 + 180 * (sum_i piki_i - minCarry + 1) / maxCarry
        // piki_i = (purp: 0.6, white: 3.0, rest: 1.0) + (leaf: 0.0, bud: 0.5, flower: 1.0) 
        // my unit adjustment: carry velocity is (220 + 180 * (sum_i piki_i - minCarry + 1) / maxCarry) / 8.5 cm/s
        // olimar moves 1.17 units/s = 199.8 cm/s without rush boots.
        // rush boots 4 units / 3.68s, without 4 units / 4.70s (ratio is 205/160)
    final float olimarSpeedUnitsNoRush = 1.17f;
    final float olimarSpeedUnitsRush = 1.17f * 205 / 160;
    // cm = in game units of x/z measurement (waypoint distances are in cm)
    // unit = length of 1 map unit
    // 170cm = 1 unit.
    // carrying formula on wiki gives measurements in some arbitrary velocity unit.
    // however, if you divide by ~8.5, the carrying formula is in cm/s.   
        // trials on HoB 1: 8.53,8.44,8.63,8.54,8.40,8.32
        // 1 flower on grub 5 units / 12.51s (sumpiki = 2, vel = 220+180*2=580)
        // 1 leaf on grub 5 units / 17.94s (sumpiki = 1, vel = 220+180*1=400)
        // 1 purp flower 5 units / 14.45s (sumpiki = 1.6, vel = 220+180*1.6=508)
    final float carryMultiplier = 8.5f;
    // gate notes:
        // formula: time in seconds = max(6, 3 + life * 2 / attkpower)
        // ~6s of fixed costs
        // 1 life (ch12)   6s
        //  4000 life     18*60, 9s, 16s, 27*18
        // 2500 life   100 attk 52s,, 50 attk 102s, 30 attk 170s, 20 attk 253
        // (piki attack animation is 30 / 20s, 30/11s spicy)
    final float gateLifeMultiplier = 2.0f;
    // geyser health: 17s / 145 attk, 15s / 180 attk, 9s / 340 attk -> 2700
    final float geyserHealth = 2700.0f;
    // digging: 
        // iid says: HP is 3900 and Pikmin digs once per a second
        // First 480HP requires whites
        // Theoretical digging time is 48/Pw + 342/P[s], where Pw is number of whites and P is weighted sum of Pikmin (Purple and Spicy:2, Red:1.5, others:1)
        // my tests: 10 whites on ring/SAT: ~40s each. 5 whites: 78s.
        // formula ~3900 / pikiDigSum
    final float diggingHealth = 3900.0f;
    // loading carryable objects (just made this up)
    final float loadTimeSeconds = 0.5f;
    final float loadTimeSecondsStory = 3.0f;

    // these are computed once per sublevel based on what the game is trying to spawn.
    int expectedNumTreasures;
    int expectedNumQueenCandypop;
    int previousNumQueenCandypop;
    int expectedNumPurpleCandypop;
    int expectedNumWhiteCandypop;
    int expectedNumPokosPoD;
    int expectedMaxCarry;
    int pikiCount;
    float holeWorkMultiplier, geyserWorkMultiplier, candypopWorkMultiplier;
    float pikiCarryValue, pikiAttackValue, pikiStrengthValue, pikiDigValue, fastestPikiCarryValue;
    String sublevelId;
    boolean rushBoots;
    float olimarSpeedCm;

    void setupJudge(CaveGen g) {
        readRankFile();

        if (CaveGen.judgeVsAvg && rankBreakPoints != null) {
            for (int i = 0; i < judgeVsAvgCumScores.length; i++) {
                int r = (int)(Math.random() * rankBreakPoints.length);
                judgeVsAvgCumScores[i] += rankBreakPoints[r];
            }
        }

        sublevelId = CaveGen.specialCaveInfoName + "-" + CaveGen.sublevel;

        // count expected number of treasures
        expectedNumTreasures = 0;
        expectedNumPokosPoD = 0;
        expectedMaxCarry = 0;
        for (Item t: g.spawnItem) {
            expectedNumTreasures += t.min;
            String name = t.itemName.toLowerCase();
            expectedMaxCarry += Parser.maxCarry.get(name);
            if (!ignoreTreasuresPoD.contains(name) && !optionalTreasuresPoD.contains(name)) {
                expectedNumPokosPoD += Parser.pokos.get(name);
            } else if (CaveGen.judgeType.equals("pod")) {
                expectedMaxCarry -= Parser.maxCarry.get(name);
            }
        }
        for (Teki t: g.spawnTekiConsolidated) 
            if (t.itemInside != null) {
                expectedNumTreasures += t.min;
                String name = t.itemInside.toLowerCase();
                expectedMaxCarry += Parser.maxCarry.get(name);
                if (!ignoreTreasuresPoD.contains(name) && !optionalTreasuresPoD.contains(name)) {
                    expectedNumPokosPoD += Parser.pokos.get(name);
                }  else if (CaveGen.judgeType.equals("pod")) {
                    expectedMaxCarry -= Parser.maxCarry.get(name);
                }
            }
        if (sublevelId.equals("CH8-1")) expectedNumTreasures += 3;
        if (sublevelId.equals("CH29-1")) expectedNumTreasures -= 1;
        if (sublevelId.equals("CH21-1")) expectedNumTreasures += 3;

        // count candypops
        expectedNumQueenCandypop = 0;
        expectedNumPurpleCandypop = 0;
        expectedNumWhiteCandypop = 0;
        for (Teki t: g.spawnTekiConsolidated) {
            if (t.tekiName.equalsIgnoreCase("randpom")) expectedNumQueenCandypop += t.min;
            if (t.tekiName.equalsIgnoreCase("blackpom")) expectedNumPurpleCandypop += t.min;
            if (t.tekiName.equalsIgnoreCase("whitepom")) expectedNumWhiteCandypop += t.min;
        }
        if (sublevelId.equals("CH6-2")) previousNumQueenCandypop = 1;
        else if (sublevelId.equals("CH26-2")) previousNumQueenCandypop = 3;
        else if (sublevelId.equals("CH26-3")) previousNumQueenCandypop = 8;
        else if (sublevelId.equals("CH30-4")) previousNumQueenCandypop = 3;
        else if (sublevelId.equals("CH30-5")) previousNumQueenCandypop = 3;
        else previousNumQueenCandypop = 0;

        holeWorkMultiplier = 1;
        geyserWorkMultiplier = 10;
        candypopWorkMultiplier = 1;
        rushBoots = false;

        // calculate challenge mode piki counts
        if (Parser.chPikiCount.containsKey(CaveGen.specialCaveInfoName + " 0")) {
            float pikiCarrySum = 0;
            float pikiAttackSum = 0;
            float pikiStrengthSum = 0;
            float pikiDigSum = 0;
            fastestPikiCarryValue = 0;
            pikiCount = 0;
            boolean spicy = Parser.chSpicy.get(CaveGen.specialCaveInfoName) > 0;
            for (int i = 0; i < 18; i++) {
                int num = Parser.chPikiCount.get(CaveGen.specialCaveInfoName + " " + i);
                pikiCount += num;
                float carryVal = (i%3 == 2 || spicy ? 1 : i%3 == 1 ? 0.5f : 0) + (i/3 == 3 ? 0.6f : i/3 == 4 ? 3 : 1);
                pikiCarrySum += num * carryVal;
                pikiAttackSum += num * (i/3 == 3 || spicy ? 20 : i/3 == 1 ? 15 : 10);
                pikiStrengthSum += num * (i/3 == 3 ? 10 : 1);
                pikiDigSum += num * (i/3 == 3 || spicy ? 20 : i/3 == 1 ? 15 : 10);
                if (num > 0)
                    fastestPikiCarryValue = Math.max(fastestPikiCarryValue, carryVal);
            }
            int extraPiki = (expectedNumQueenCandypop + previousNumQueenCandypop) * 8;
            pikiCount += extraPiki;
            pikiCarrySum += extraPiki * (spicy ? 2 : 1);
            pikiAttackSum += extraPiki * (spicy ? 18 : 15);
            pikiStrengthSum += extraPiki;
            pikiCarryValue = pikiCarrySum / pikiCount;
            pikiAttackValue = pikiAttackSum / pikiCount;
            pikiStrengthValue = pikiStrengthSum / pikiCount;
            pikiDigValue = pikiDigSum / pikiCount;
        } else {
            pikiCount = 60;
            pikiCarryValue = 2.5f;
            pikiAttackValue = 13;
            pikiStrengthValue = 1;
            pikiDigValue = 13;
            fastestPikiCarryValue = 4;
        }

        if (CaveGen.judgeType.equals("at")) {
            int whites = 0;
            switch (CaveGen.specialCaveInfoName) {
                case "EC": whites = 0; rushBoots = false; break;
                case "HoB": whites = 15; rushBoots = false; break;
                case "WFG": whites = CaveGen.sublevel >= 3 ? 15 : 0; rushBoots = false; break;
                default: whites = 35; rushBoots = true;
            }
            pikiCount = 80;
            expectedMaxCarry += 1;
            pikiCarryValue = (2.0f*Math.min(whites,expectedMaxCarry) + 2.0f*expectedMaxCarry) / expectedMaxCarry;
            pikiAttackValue = 18;
            pikiStrengthValue = 1;
            pikiDigValue = 16;
        }
        if (CaveGen.judgeType.equals("pod")) {
            int whites = 0;
            switch (CaveGen.specialCaveInfoName) {
                case "EC":
                case "HoB": whites = 0; break;
                case "WFG": whites = CaveGen.sublevel >= 3 ? 15 : 0; break;
                case "SH":
                case "BK": whites = 20; break;
                default: whites = 30;
            }
            rushBoots = false;
            pikiCount = 60;
            expectedMaxCarry += 1;
            pikiCarryValue = (2.0f*Math.min(whites,expectedMaxCarry) + 2.0f*expectedMaxCarry) / expectedMaxCarry;
            pikiAttackValue = 13;
            pikiStrengthValue = 1;
            pikiDigValue = 13;
        }

        olimarSpeedCm = rushBoots ? olimarSpeedUnitsRush * 170.0f : olimarSpeedUnitsNoRush * 170.0f;

    }

    double scoreToRank(double score) {
        score = Math.round(100*score)/100.0; // Double.parseDouble(String.format("%.2f", score));
        // calculate rank from rank file using binary search
        if (rankBreakPoints == null)
            return 50;
        if (score < rankBreakPoints[0]) 
            return 0;
        if (score > rankBreakPoints[999]) 
            return 100;
        int hi = 999, low = 0;
        while (low+1 < hi) {
            int mid = (hi+low)/2;
            double x = rankBreakPoints[mid];
            if (score <= x) 
                hi = mid;
            else if (score > x)
                low = mid;
        }
        if (rankBreakPoints[hi] == score) low = hi;
        int num_dupes = 0;
        for (int i = low+1; i<1000; i++) {
            if (rankBreakPoints[i] == score)
                num_dupes += 1;
            else break;
        }
        low += num_dupes / 2;
        return low / 10.0; // why was this so tricky to write ??
    }

    boolean filter(double score, double rank) {
        if (CaveGen.judgeFilterScoreSign > 0 && CaveGen.judgeFilterScore > score) return false;
        if (CaveGen.judgeFilterScoreSign < 0 && CaveGen.judgeFilterScore < score) return false;
        if (CaveGen.judgeFilterRank > 0 && CaveGen.judgeFilterRank > rank) return false;
        if (CaveGen.judgeFilterRank < 0 && -CaveGen.judgeFilterRank < rank) return false;
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
            if (scoreMap.containsKey(sublevelString + " " + s))
                ss.add(new ScoredSeed(s, scoreMap.get(sublevelString + " " + s)));
        }

        Collections.sort(ss, new Comparator<ScoredSeed>() {
            public int compare(ScoredSeed s1, ScoredSeed s2) {
                if (s1.score < s2.score) return -1;
                if (s1.score > s2.score) return 1;
                return s1.seed.compareTo(s2.seed);
            }
        });

        stats.println("\nJudge sorted list " + sublevelString + ":");

        for (ScoredSeed s: ss) {
            double rank = rankMap.get(sublevelString + " " + s.seed);
            if (filter(s.score, rank))
            stats.println(String.format("%s -> %.2f (%.1f%%)", s.seed, s.score, rank));
        }
        stats.println("\n");

        // write out the rank file
        if (CaveGen.judgeRankFile) {
            try {
                BufferedWriter wr = new BufferedWriter(new FileWriter("files/rank_file.txt", true));
                wr.write(sublevelString + "-" + CaveGen.judgeType);
                int n = ss.size();
                /* < 1
                for (int p = 1000000000; p >= 1000; p /= 10) {
                    for (int i = 1; i < 10; i++) {
                        double r = i * 1.0 / p;
                        if (n * i >= p) {
                            wr.write(String.format(";%."+(int)(Math.log10(p))+"f:" + "%.2f",r,ss.get((int)(r * n)).score));
                        }
                    }
                } 
                // > 99
                for (int p = 1000; p <= 1000000000; p *= 10) {
                    for (int i = 9; i > 0; i--) {
                        double r = 1 - i * 1.0 / p;
                        if (n * i >= p) {
                            wr.write(String.format(";%."+(int)(Math.log10(p))+"f:" + "%.2f",r,ss.get((int)(r * n)).score));
                        }
                    }
                } */

                // .1 spacing for ranks
                for (int i = 1; i <= 1000; i++) {
                    wr.write(String.format(";%.2f",ss.get((int)((i / 1000.0 - 0.0005) * n)).score));
                }

                wr.write("\n");
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
                        if (scoreMap.get(CaveViewer.caveViewer.nameBuffer.get(i)) > scoreMap.get(CaveViewer.caveViewer.nameBuffer.get(j))) {
                            String tempName = CaveViewer.caveViewer.nameBuffer.get(i);
                            CaveViewer.caveViewer.nameBuffer.set(i, CaveViewer.caveViewer.nameBuffer.get(j));
                            CaveViewer.caveViewer.nameBuffer.set(j, tempName);
                            BufferedImage tempImg = CaveViewer.caveViewer.imageBuffer.get(i);
                            CaveViewer.caveViewer.imageBuffer.set(i, CaveViewer.caveViewer.imageBuffer.get(j));
                            CaveViewer.caveViewer.imageBuffer.set(j, tempImg);
                        }            
        }
        
        // clear the maps to prevent them from getting too big
        if (CaveGen.dontStoreJudge) {
            scoreMap.clear();
            rankMap.clear();
        }
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
                if (s1.score < s2.score) return -1;
                if (s1.score > s2.score) return 1;
                return s1.seed.compareTo(s2.seed);
            }
        });

        for (ScoredSeed s: ss) {
            stats.println(String.format("%s -> %.2f", s.seed, s.score));
        }
    }

    double[] rankBreakPoints = null;
    HashMap<String, String> rankFileStrings = null;
    void readRankFile() {
        String id = CaveGen.specialCaveInfoName + "-" + CaveGen.sublevel + "-" + CaveGen.judgeType;
        if (rankFileStrings == null) {
            rankFileStrings = new HashMap<String, String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader("files/rank_file.txt"));
                String line;
                while ((line = br.readLine()) != null) {
                    int i = line.indexOf(';');
                    if (i != -1)
                        rankFileStrings.put(line.substring(0, i), line.substring(i+1));
                }
                br.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if (rankFileStrings.containsKey(id)) {
            String[] st = rankFileStrings.get(id).split(";");
            rankBreakPoints = new double[1000];
            for (int i = 0; i < 1000; i++) {
                rankBreakPoints[i] = Double.parseDouble(st[i]);
            }
        } else {
            rankBreakPoints = null;
        }
    }

    // this computes the set of waypoints that have a carryable item that will take this path
    void computeWaypointCarryableGraph(CaveGen g, String config) {
        if (config.contains("h")) { // holes
            if (g.placedHole != null)
                g.closestWayPoint(g.placedHole).hasCarryableBehind = true;
            if (g.placedGeyser != null)
                g.closestWayPoint(g.placedGeyser).hasCarryableBehind = true;
        }
        if (config.contains("t")) { // treasures
            for (Item t: g.placedItems)
                g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
            for (Teki t: g.placedTekis)
                if (t.itemInside != null)
                    g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
        }
        if (config.contains("c")) { // carcasses
            for (Teki t: g.placedTekis) 
                if (!noCarcassNames.contains(t.tekiName))
                    g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
        }
        if (config.contains("k")) { // key
            for (Item t: g.placedItems)
                if (t.itemName.equalsIgnoreCase("key"))
                    g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
            for (Teki t: g.placedTekis)
                if (t.itemInside != null)
                    if (t.itemInside.equalsIgnoreCase("key"))
                        g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
        }
        if (config.contains("m")) { // map01
            for (Item t: g.placedItems)
                if (t.itemName.equalsIgnoreCase("map01"))
                    g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
            for (Teki t: g.placedTekis)
                if (t.itemInside != null)
                    if (t.itemInside.equalsIgnoreCase("map01"))
                        g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
        }
        if (config.contains("p")) { // purple candypops
            for (Teki t: g.placedTekis)
                if (t.tekiName.equalsIgnoreCase("blackpom"))
                    g.closestWayPoint(t.spawnPoint).hasCarryableBehind = true;
        }

        for (MapUnit m: g.placedMapUnits)
            for (WayPoint w: m.wayPoints) {
                if (!w.hasCarryableBehind) continue;
                WayPoint wp = w;
                while (true) {
                    wp.hasCarryableBehind = true;
                    if (wp.isStart) break;
                    wp = wp.backWp;
                    if (wp.hasCarryableBehind) break;
                }
            }
    }

    // This checks if this teki is in the way of something
    boolean isInTheWay(CaveGen g, Teki t) {
        MapUnit m = t.spawnPoint.mapUnit;
        if (m == null) {
            // we are on a door
            boolean b = g.closestWayPoint(t.spawnPoint).hasCarryableBehind;
            if (b) t.isInTheWay = true;
            return b;
        }
        Vec3 p = new Vec3(t.posX, t.posY, t.posZ);
        for (WayPoint wp: m.wayPoints) {
            if (wp.isStart) continue;
            if (!wp.hasCarryableBehind) continue;
            if (wp.backWp.mapUnit != m) continue;
            float dist = g.pointToSegmentDist(p, wp.vec, wp.backWp.vec, 0, 0);
            if (dist < 120.0) {
                t.isInTheWay = true;
                return true;
            }
        }
        return false;
    }
    boolean isInTheWay(CaveGen g, Gate t) {
        boolean b = g.closestWayPoint(t.spawnPoint).hasCarryableBehind;
        if (b) t.isInTheWay = true;
        return b; // always a door spawnpoint
    }


    // -------------------- Find good layouts (Old version) ---------------------------
    // this should be depricated in favor of using -judge

    void findGoodLayouts(CaveGen g) {

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
            stats.println("Missing treasure: " + g.specialCaveInfoName + " " + g.sublevel + " " + Drawer.seedToString(g.initialSeed));
        }

        // Good layout finder (story mode)
        if (CaveGen.findGoodLayouts && !CaveGen.challengeMode && !missingUnexpectedTreasure) {
            boolean giveWorstLayoutsInstead = CaveGen.findGoodLayoutsRatio < 0;

            ArrayList<Teki> placedTekisWithItems = new ArrayList<Teki>();
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null)
                placedTekisWithItems.add(t);
            }

            // Compute the waypoints on the shortest paths
            ArrayList<WayPoint> wpOnShortPath = new ArrayList<WayPoint>();
            for (Item t: g.placedItems) { // Treasures
                if (ignoreTreasuresPoD.contains(t.itemName.toLowerCase())) continue;
                WayPoint wp = g.closestWayPoint(t.spawnPoint);
                while (!wp.isStart) {
                    if (!wpOnShortPath.contains(wp)) wpOnShortPath.add(wp);
                    wp = wp.backWp;
                }
            }
            for (Teki t: placedTekisWithItems) { // Treasures inside enemies
                if (ignoreTreasuresPoD.contains(t.itemInside.toLowerCase())) continue;
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
        else if (CaveGen.findGoodLayouts && CaveGen.challengeMode && !missingUnexpectedTreasure) {
            boolean giveWorstLayoutsInstead = CaveGen.findGoodLayoutsRatio < 0;

            // compute the number of pokos availible
            int pokosAvailible = 0;
            for (Teki t: g.placedTekis) {
                String name = t.tekiName.toLowerCase();
                if (plantNames.contains(name)) continue;
                if (hazardNames.contains(name)) continue;
                if (name.equalsIgnoreCase("egg"))
                    pokosAvailible += 10; // mitites
                else if (!noCarcassNames.contains(name) && !g.isPomGroup(t))
                    pokosAvailible += Parser.pokos.get(t.tekiName.toLowerCase());
                if (t.itemInside != null)
                    pokosAvailible += Parser.pokos.get(t.itemInside.toLowerCase());
            }
            for (Item t: g.placedItems)
                pokosAvailible += Parser.pokos.get(t.itemName.toLowerCase());

            // compute the number of pikmin*seconds required to complete the level
            float pikminSeconds = 0;
            for (Teki t: g.placedTekis) {
                if (plantNames.contains(t.tekiName.toLowerCase())) continue;
                if (hazardNames.contains(t.tekiName.toLowerCase())) continue;
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

        else
            CaveGen.imageToggle = false;
    }

    
    SortedList<Integer> allScores = new SortedList<Integer>(Comparator.naturalOrder());
    String findTekis = ""; // "blackpom,whitepom"

    HashSet<String> hashSet(String s) {
        return new HashSet<String>(Arrays.asList(s.split(",")));
    }

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
        if (noCarcassNames.contains(name)) return 0;
        int minCarry = Parser.minCarry.get(name);
        int maxCarry = Parser.maxCarry.get(name);
        return 7 * minCarry * g.closestWayPoint(sp).distToStart
                    / (220.0f + 180.0f * (2 * minCarry - minCarry + 1) / maxCarry);
    }

}
