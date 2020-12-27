import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Manip {

    final JFrame jfr = new JFrame("Manip");
    final String fontfamily = "Arial, sans-serif";
    final String fontfamilyMono = "Monospaced";
    final Font font = new Font(fontfamily, Font.PLAIN, 12);
    final Font font10 = new Font(fontfamily, Font.PLAIN, 10);
    final Font fontMono = new Font(fontfamilyMono, Font.PLAIN, 12);
	final Font fontMono16 = new Font(fontfamilyMono, Font.PLAIN, 16);
    final Font fontMono10 = new Font(fontfamilyMono, Font.PLAIN, 10);
    JTextPane jtext = new JTextPane();
    JTextPane jtext2 = new JTextPane();
    JTextPane jtextplay = new JTextPane();
    ArrayList<JTextPane> jTextGrid = new ArrayList<JTextPane>();

    String curCave = "";
    int curSublevel = 0;

    void manip(String mode) {

        boolean pod_mode = mode.equals("pod");
        boolean at_mode = mode.equals("at");
        boolean storyMode = pod_mode || at_mode;

        if (!(mode.equals("key") || mode.equals("cmat") || mode.equals("700k") || mode.equals("attk") || mode.equals("pod") || mode.equals("at"))) {
            System.out.println("Bad mode.");
            return;
        }


        // Set up the Manip UI
        jfr.getContentPane().setLayout(null);
		jfr.setSize(410, 660);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        jtext.setText("mode: " + mode);
        jtext.setFont(fontMono);
        jtext.setEditable(false);
        jtext.setContentType("text/plain");
        jtext.setBackground(null);
        jtext.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        jtext.setBounds(45,5,150,110);
        jfr.add(jtext);

        jtext2.setText("");
        jtext2.setFont(fontMono);
        jtext2.setEditable(false);
        jtext2.setContentType("text/plain");
        jtext2.setBackground(null);
        jtext2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        jtext2.setBounds(200,5,150,110);
        jfr.add(jtext2);

        jtextplay.setText("OOOOO\nOOOOO\nOOOOO\nOOOOO\nOOOOO\nOOOOO\n");
        jtextplay.setFont(fontMono10);
        jtextplay.setEditable(false);
        jtextplay.setContentType("text/plain");
        jtextplay.setBackground(null);
        jtextplay.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        jtextplay.setBounds(5,5,40,100);
        jfr.add(jtextplay);

        int cwidth = 9;
        int[] gA = {2,4,5,4,3,8,3,5,4};
        int gX = gA.length, gY = 30;
        for (int i = 0; i < gY; i++) {
            int x = 0;
            for (int j = 0; j < gX; j++) {
                JTextPane jg = new JTextPane();
                jg.setText("");
                jg.setFont(fontMono);
                jg.setEditable(false);
                jg.setContentType("text/plain");
                jg.setBackground(null);
                //jg.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
                jg.setBounds(x + 5,125 + 16*i,gA[j]*cwidth,16);
                jfr.add(jg);
                jTextGrid.add(jg);
                x += gA[j]*cwidth+5;
            }
        }

        jfr.revalidate();
		jfr.repaint();
        jfr.setVisible(true);


        // launch the continuous digit parser
        Thread thread = new Thread(new Runnable() {
            public void run() {

                try {
                    Process p = Runtime.getRuntime().exec("python continuous.py");
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while (true) {
                        String s = in.readLine();
                        if (s != null && !s.equals("exit")) {
                            message(false, s);
                        } else {
                            message(false, "exit");
                            break;
                        }
                    }

                    while (true) {
                        String s = err.readLine();
                        if (s != null) {
                            System.out.println("continuous err: " + s);
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();


        final CaveViewer caveViewer = new CaveViewer();
        CaveViewer.caveViewer = caveViewer;
        caveViewer.run("gui ch1 1".split(" "));
        CaveViewer.active = false;
        CaveViewer.manipActive = true;
        caveViewer.jfr.setVisible(false);

        boolean readyToGenerate = false;
        boolean showTimers = false;
        boolean waitForNextSeed = false;
        boolean waitForNextFadeout = false;
        boolean waitForNextLevelEnter = false;
        int numConsecutiveLevelEnters = 0;
        boolean levelsPlayed[] = new boolean[31];
        int numLevelsPlayed = 0;
        int lastStagePlayed = 0;
        long runStartTime = 0;
        double lateStageStdevs[] = new double[31];
        ArrayList<Option> options = null;
        ArrayList<String> storyLevels = new ArrayList<String>();
        ArrayList<Long> storySeeds = new ArrayList<Long>();
        ArrayList<Double> storyRanks = new ArrayList<Double>();
        ArrayList<Double> storyDiffs = new ArrayList<Double>();

        long timerTargetFrame = 0;
        long timerCurSeed = 0;
        long timerStartSeed = 0;

        long specialTargetSeed = 0;
        int specialTargetLevel = 0;
        long specialTargetWindow = 0;
        long specialTargetFrame = 0;

        boolean realTimeAttackMode = mode.equals("key") || mode.equals("cmat") || mode.equals("700k");
        CaveGen.resetParams();
        Parser.readConfigFiles();
        computeStats(mode.equals("700k") || storyMode ? "attk" : mode);
        resetDigitTrackers();
        computeStatsStory(mode);

        for (int i = 1; i <= 30; i++) {
            for (int j = 2; j <= Parser.chFloorCount.get("CH"+i); j++) {
                lateStageStdevs[i] += stddevs.get("CH"+i+"-"+j) * stddevs.get("CH"+i+"-"+j);
            }
            lateStageStdevs[i] = Math.sqrt(lateStageStdevs[i]);
        }
        System.out.println("stdevs: " + Arrays.toString(lateStageStdevs) +"\n\n");

        try {
            RandomAccessFile raf = realTimeAttackMode ? new RandomAccessFile("files/times_table_" + mode + ".txt", "r") : null;

            while (true) {

                // process the message from continuous.py
                String r = message(true, null);
                long time = System.currentTimeMillis();
                if (r != null) {
                    if (r.equals("exit"))
                        break;

                    String[] s = r.split(" ");
                    if (s[0].equals("digits")) {
                        if (numDigitsRead == 0) { // new set of digits to read. reset everything
                            resetDigitTrackers();
                            for (int i = 0; i < gY; i++) {
                                for (int j = 0; j < gX; j++) jTextGrid.get(gX*i+j).setText("");
                            }
                            repaintManip();
                            for (int i = 1; i <= 30; i++) {
                                if (levelsToPlay[i]) levelsPlayed[i] = false;
                                else if (levelsToIgnore[i]) levelsPlayed[i] = true;
                            }
                        }
                        if (numDigitsRead < 3000) {
                            for (int i = 0; i < 5; i++) {
                                char c = s[1].charAt(i);
                                digitsRead[numDigitsRead][i] = c == '_' ? -1 : c - '0';
                            }
                            numDigitsRead += 1;
                        }
                        if (!seedRead) { // process a set of digits, and try to read the seed
                            tryReadSeed();
                            if (seedRead && lastReadSeed == -1) {
                                System.out.println("Failed to detect seed, you are on your own");
                                readyToGenerate = true;
                            } else if (seedRead) {
                                System.out.println("Detected seed: " + Drawer.seedToString(lastReadSeed) + " " + seed.nth_inv(lastReadSeed));
                                readyToGenerate = true;
                            }
                        }
                        if (waitForNextSeed) { // params to change after a level has been played
                            System.out.println("\nStarting to look for next seed");
                            waitForNextSeed = false;
                            showTimers = false;
                            numLevelsPlayed += 1;
                            lastStagePlayed = options.get(caveViewer.lastSSeed).level;
                            if (options.size() > caveViewer.lastSSeed && realTimeAttackMode) {
                                levelsPlayed[options.get(caveViewer.lastSSeed).level] = true;
                            }
                        }
                    }
                    if (s[0].equals("fadeout")) { // count fadeouts
                        numDigitsRead = 0;
                        if (time - timeOfLastFadeout < 1000) {
                            timeOfLastFadeout = time;
                        } else {
                            numFadeouts += 1;
                            timeOfLastFadeout = time;
                            System.out.println("Detect fadeout " + numFadeouts + "\t\t\t\t\t\t\t\t");
                        }
                        if (waitForNextFadeout) {
                            waitForNextLevelEnter = true;
                            waitForNextFadeout = false;
                            showTimers = true;
                        }
                    }
                    if (s[0].equals("levelenter")) {
                        numConsecutiveLevelEnters += 1;
                        if (numConsecutiveLevelEnters >= 10) {
                            if (time - timeOfLastLevelEnter < 1000) {
                                timeOfLastLevelEnter = time;
                            } else {
                                numLevelEnters += 1;
                                timeOfLastLevelEnter = time;
                                System.out.println("Detect level enter " + numLevelEnters + "\t\t\t\t\t\t\t\t");
                            }
                            if (numLevelsPlayed == 0 && numLevelEnters == 1 && seedRead) {
                                runStartTime = time;
                            }
                            if (waitForNextLevelEnter) {
                                waitForNextSeed = true;
                                waitForNextLevelEnter = false;
                                showTimers = false;
                            }
                        }
                    } else numConsecutiveLevelEnters = 0;
                    if (s[0].equals("donedigit")) { // message that there are no more digits to be read
                        if (numDigitsRead > 0 && !seedRead) {
                            System.out.println("Missed seed, you are on your own");
                            seedRead = true;
                            readyToGenerate = true;
                            lastReadSeed = -1;
                        }
                    }
                    if (s[0].length() >= 11 && s[0].substring(0,11).equals("lettersinfo") && storyMode) {
                        System.out.println(s[0]);
                        System.out.println("reading story seed");

                        long sd = seed.letters.letters(s[0],lastReadSeed);

                        if (curCave.equals("Hole of Heroes")||curCave.equals("Dream Den")||curCave.equals("Cavern of Chaos")) {
                            if (seed.letters.out_cave.equals("Hole of Beasts"))
                                seed.letters.out_cave = "Hole of Heroes";
                        }
                        if (!seed.letters.out_cave.equals(curCave))
                            curSublevel = 1;
                        else curSublevel += 1;
                        curCave = seed.letters.out_cave;

                        if (sd == -1) {
                            jtext.setText("Failed to read story seed");
                            lastReadSeed = -1;
                        }
                        else {
                            
                            sd = seed.next_seed(sd, curCave.length());
                            lastReadSeed = sd;
                            String curCaveSp = Parser.fullNameToSpecial(curCave);
                            System.out.println(curCaveSp + "-" + curSublevel);
                            jtext.setText(curCaveSp + " " + curSublevel + "\n" + Drawer.seedToString(sd));

                            CaveViewer.guiOnly = true;
                            //caveViewer.imageBuffer.clear();
                            //caveViewer.nameBuffer.clear();
                            CaveViewer.manipKeepImages = true;
                            
                            storyLevels.add(0, curCaveSp + "-" + curSublevel);
                            storySeeds.add(0, sd);
                            for (int i = 0; i < 1; i++) {
                                String args2 = "cave " + curCaveSp + "-" + curSublevel + " -noprints -drawpodangle "
                                + "-seed 0x" + Drawer.seedToString(sd) + " -judge " + mode ;
                                CaveGen.main(args2.split(" "));
                                if (i == 0)  {
                                    caveViewer.lastSSeed = 0;
                                    caveViewer.jfrView.setVisible(true);
                                    caveViewer.lastImg();
                                }
                            }
                            storyRanks.add(0, CaveGen.stats.judge.rankMap.get(curCaveSp + "-" + curSublevel +" " + Drawer.seedToString(sd)));
                            storyDiffs.add(0, CaveGen.stats.judge.scoreMap.get(curCaveSp + "-" + curSublevel +" " + Drawer.seedToString(sd)) - story_topPercentile.get(curCaveSp + "-" + curSublevel));

                            CaveViewer.manipKeepImages = false;

                            int numOptionsShow = 30;
                            for (int i = 0; i < numOptionsShow; i++) {
                                if (i >= storyLevels.size()) {

                                    continue;
                                }
                                
                                if (i >= gY) continue;
                                double rank = storyRanks.get(i);
                                double avgDiff = storyDiffs.get(i);
                                jTextGrid.get(gX*i+0).setText(""+(i+1));
                                jTextGrid.get(gX*i+1).setText(storyLevels.get(i));
                                jTextGrid.get(gX*i+2).setText("");
                                jTextGrid.get(gX*i+3).setText("");
                                jTextGrid.get(gX*i+4).setText("");
                                jTextGrid.get(gX*i+5).setText(Drawer.seedToString(storySeeds.get(i)));
                                jTextGrid.get(gX*i+6).setText("");
                                jTextGrid.get(gX*i+7).setText(rank >= 100 ? "100.%" : String.format("%4.1f%%", rank));
                                jTextGrid.get(gX*i+8).setText(String.format("%4d", Math.round(avgDiff)));
                            }
                            
                            for (int i = numOptionsShow; i < gY; i++) {
                                for (int j = 0; j < gX; j++) jTextGrid.get(gX*i+j).setText("");
                            }
                            repaintManip();
                        }
                    }
                }

                // detect title loops
                if (numFadeouts >= 2 && time-timeOfLastFadeout > 1000 && numLevelEnters == 0 && numFadeouts % 2 == 0 && waitForNextLevelEnter && lastReadSeed != -1) {
                    numTitleLoops += 1;
                    System.out.println("Detected title loop " + numTitleLoops);
                    showTimers = false;
                    waitForNextFadeout = false;
                    waitForNextLevelEnter = false;
                    waitForNextSeed = false;
                    readyToGenerate = true;
                    lastReadSeed = seed.next_seed(timerCurSeed, 4505-4);
                }


                // compute and show the recommended manip
                if (readyToGenerate) {

                    StringBuilder text1 = new StringBuilder();

                    boolean unknownSeed = lastReadSeed == -1;
                    int numSeedsToConsider = realTimeAttackMode ? 
                                (numLevelsPlayed == 0 ? (int)Double.parseDouble(params.get("secondsWaitingForLevel"))/5 : 5)
                                : (int)Double.parseDouble(params.get("attkMaxWaitingForLevel"))/5;
                    if (unknownSeed) {
                        lastReadSeed = seed.next_seed(0, -1004);
                        numSeedsToConsider = 1;
                    }
                    if (numLevelsPlayed == 0) lastStagePlayed = 8;
                    long firstSeedToConsider = seed.next_seed(lastReadSeed, 1004);
                    long startSeed = seed.next_seed(lastReadSeed, 4);
                    //System.out.println(Drawer.seedToString(lastReadSeed) + " " + Drawer.seedToString(startSeed) + " " + Drawer.seedToString(firstSeedToConsider));

                    String args = "none CH1-1,CH2-1,CH3-1,CH4-1,CH5-1,CH6-1,CH7-1,CH8-1,CH9-1,CH10-1,"
                        + "CH11-1,CH12-1,CH13-1,CH14-1,CH15-1,CH16-1,CH17-1,CH18-1,CH19-1,CH20-1,CH21-1,"
                        + "CH22-1,CH23-1,CH24-1,CH25-1,CH26-1,CH27-1,CH28-1,CH29-1,CH30-1 "
                        + "-consecutiveseeds -seed 0x" + Drawer.seedToString(firstSeedToConsider) 
                        + " -num " + numSeedsToConsider + " -judge " + mode;
                    System.out.println("Generating levels...");
                    CaveGen.main(args.split(" "));
                    System.out.println("Done generating levels.");

                    int waitTimesBySeed[] = new int[numSeedsToConsider];
                    int scrollTimesByLevel[] = new int[31];
                    float optimalResetGiveupByLevel[] = new float[31];
                    float[] remainingTimesByLevel = new float[31];
                    String scrollSeq[] = new String[31];

                    for (int i = 1; i < numSeedsToConsider; i++) {
                        waitTimesBySeed[i] = i == 1 ? seed.seed_duration_first(startSeed)
                                        : waitTimesBySeed[i-1] + seed.seed_duration(seed.next_seed(lastReadSeed, 4+i-1));
                    }
                    if (numLevelsPlayed == 0) {
                        for (int i = 0; i < numSeedsToConsider; i++)
                            waitTimesBySeed[i] *= Double.parseDouble(params.get("secondsGiveUpPerSecondWait"));
                    }
                    int rl = (lastStagePlayed-1) / 5;
                    int cl = (lastStagePlayed-1) % 5;
                    for (int i = 1; i <= 30; i++) {
                        optimalResetGiveupByLevel[i] = (float)(lateStageStdevs[i] * Double.parseDouble(params.get("secondsGiveUpPerSecondVol")) * (1-numLevelsPlayed/30.0));
                        if (lastStagePlayed <= 0) continue; // all 0
                        int rc = (i-1) / 5;
                        int cc = (i-1) % 5;
                        int rd = (rc-rl+6) % 6;
                        int cd = (cc-cl+5) % 5;
                        int dist = Math.min(rd, 6-rd) + Math.min(cd, 5-cd);
                        scrollTimesByLevel[i] = Integer.parseInt(params.get("framesNeededToScrollLevel")) * dist;
                        //scrollSeq[i] = (rd==0?"":rd==1?"v":rd==2?"vv":rd==3?"vvv":rd==4?"^^":"^")
                        //            + (cd==0?"":cd==1?">":cd==2?">>":cd==3?"<<":"<");
                        scrollSeq[i] = (rd==0?"":rd==1?"D":rd==2?"DD":rd==3?"DDD":rd==4?"UU":"U")
                                    + (cd==0?"":cd==1?"R":cd==2?"RR":cd==3?"LL":"L");
                    }

                    if (realTimeAttackMode) {
                        int binKey = 0;
                        for (int i = 1; i <= 30; i++) {
                            if (!levelsPlayed[i]) {
                                binKey += 1 << (i-1);
                            }
                        }
                        for (int i = 1; i <= 30; i++) {
                            raf.seek(4 * (long)(binKey & (~(1 << (i-1)))));
                            remainingTimesByLevel[i] = raf.readFloat();
                        }

                        System.out.println("play " + Arrays.toString(levelsPlayed));
                        System.out.println("wait " + Arrays.toString(waitTimesBySeed));
                        System.out.println("scrl " + Arrays.toString(scrollTimesByLevel));
                        System.out.println("orpg " + Arrays.toString(optimalResetGiveupByLevel));
                        System.out.println("remt " + Arrays.toString(remainingTimesByLevel));

                        int splitsSum = 0;
                        for (int i = 1; i <= 30; i++) {
                            if (levelsPlayed[i]) {
                                splitsSum += 60*Math.floor(splits[i]) + 100*(splits[i]-Math.floor(splits[i]));
                            }
                        }

                        text1.append("Seed: " + (unknownSeed ? "???" : Drawer.seedToString(lastReadSeed)) + "\n");
                        text1.append("Seed: " + (unknownSeed ? "???" : seed.nth_inv(lastReadSeed)) + "\n");
                        System.out.println("num played level " + numLevelsPlayed +"/30");
                        System.out.println("last played level " + lastStagePlayed);
                        System.out.printf("run duration: %dm%ds \n", (int)((time-runStartTime)/60000), (int)((time-runStartTime)/1000)%60);
                        text1.append(numLevelsPlayed +"/30" + " last=" + lastStagePlayed + "\n");
                        System.out.println("delta: " + (int)((time-runStartTime)/1000 - splitsSum));
                        if (runStartTime > 0)
                            text1.append(String.format("%dm%ds\n%d\n", (int)((time-runStartTime)/60000), (int)((time-runStartTime)/1000)%60, (int)((time-runStartTime)/1000 - splitsSum)));
                    } else {
                        if (runStartTime > 0)
                            text1.append(String.format("%dm%ds\n", (int)((time-runStartTime)/60000), (int)((time-runStartTime)/1000)%60));
                        text1.append(numLevelsPlayed +"" + " last=" + lastStagePlayed + "\n");
                    }

                    StringBuilder textPlay = new StringBuilder();
                    for (int i = 1; i <= 30; i++) {
                        System.out.print(levelsToPlay[i] ? "P" : levelsToIgnore[i] ? "I" : levelsPlayed[i] ? "X" : "O");
                        textPlay.append(levelsToPlay[i] ? "P" : levelsToIgnore[i] ? "I" : levelsPlayed[i] ? "X" : "O");
                        if (i%5 == 0) {
                            System.out.println();
                            textPlay.append("\n");
                        }
                    }
                    jtext.setText(text1.toString());
                    jtextplay.setText(textPlay.toString());
                    jtext2.setText("");
                    repaintManip();

                    long[] seedsConsidered = new long[numSeedsToConsider];
                    String[] seedStr = new String[numSeedsToConsider];
                    options = new ArrayList<Option>(30*numSeedsToConsider);

                    for (int i = 0; i < numSeedsToConsider; i++) {
                        seedsConsidered[i] = seed.next_seed(firstSeedToConsider, i);
                        seedStr[i] = Drawer.seedToString(seedsConsidered[i]);
                        for (int j = 1; j <= 30; j++) {
                            if (realTimeAttackMode) {
                                //System.out.println(" CH"+j+"-1 " + seedStr);
                                double playt = unknownSeed ? 0 : CaveGen.stats.judge.scoreMap.get("CH"+j+"-1 " + seedStr[i])
                                                - means.get("CH"+j+"-1");
                                double t = Math.max(scrollTimesByLevel[j], waitTimesBySeed[i]) / 30.0
                                    + remainingTimesByLevel[j]
                                    + playt
                                    - optimalResetGiveupByLevel[j]
                                    + (levelsPlayed[j] ? 1000 : 0);
                                Option o = new Option(t, i, j);
                                o.rank =  unknownSeed ? 50 : CaveGen.stats.judge.rankMap.get("CH"+j+"-1 " + seedStr[i]);
                                o.avgDiff = playt;
                                options.add(o);
                            } else {
                                double playt = unknownSeed ? 0 : CaveGen.stats.judge.scoreMap.get("CH"+j+"-1 " + seedStr[i])
                                                - topPercentile.get("CH"+j+"-1");
                                double rankt = unknownSeed ? 0 : CaveGen.stats.judge.rankMap.get("CH"+j+"-1 " + seedStr[i]);
                                double t = (levelsToPlay[j] ? -15 : 0)
                                        + (levelsToIgnore[j] ? 100000 : 0)
                                        + playt
                                        + (rankt <= rankCutoffs[j] ? 0 : 100000)
                                        + (rankt - rankCutoffs[j]);
                                Option o = new Option(t, i, j);
                                o.rank = rankt;
                                o.avgDiff = playt;
                                options.add(o);
                            }
                        }
                    }

                    Collections.sort(options, new Comparator<Option>() {
                        public int compare(Option a, Option b) {
                            return Double.compare(a.time, b.time);
                        }
                    });

                    int numOptionsShow = Math.min(numLevelsPlayed == 0 || !realTimeAttackMode ? 30 : 5,options.size());

                    for (int i = 0; i < numOptionsShow; i++) {
                        Option o = options.get(i);
                        double scrollt = scrollTimesByLevel[o.level] /30.0;
                        double waitt = waitTimesBySeed[o.seed] / 30.0;
                        double remt = remainingTimesByLevel[o.level];
                        double playt = CaveGen.stats.judge.scoreMap.get("CH"+o.level+"-1 " + seedStr[o.seed]) - means.get("CH"+o.level+"-1");
                        double orpt = optimalResetGiveupByLevel[o.level];
                        System.out.printf("%2d: CH%-2s %s t=%.3f play=%.2f rem=%.2f orp=%.1f wait=%.1f scroll=%.1f\n", 
                            i+1, o.level+"", seedStr[o.seed], o.time, playt, remt, orpt, waitt, scrollt);
                        if (i >= gY) continue;
                        //jTextGrid.get(gX*i+0).setText(""+(i+1));
                        //jTextGrid.get(gX*i+1).setText("CH"+o.level);
                        //jTextGrid.get(gX*i+2).setText(scrollSeq[o.level]+"");
                        //jTextGrid.get(gX*i+3).setText("");
                        //jTextGrid.get(gX*i+4).setText("");
                        //jTextGrid.get(gX*i+5).setText("");
                        //jTextGrid.get(gX*i+6).setText("");
                    }
                    //repaintManip();

                    System.out.println("Done sorting options");

                    for (int i = 0; i < numOptionsShow; i++) {
                        Option o = options.get(i);
                        if (unknownSeed) {
                            o.targetFrame = 297/2;
                            o.targetWindow = 297;
                        }
                        else {
                            o.targetFrame = seed.best_timing(o.level, startSeed, seedsConsidered[o.seed]);
                            o.targetWindow = seed.frame_window;
                        }
                        if (o.targetWindow <= Integer.parseInt(params.get("framesNeededForWindow"))
                                || o.targetFrame+o.targetWindow/2 < Integer.parseInt(params.get("framesNeededToSelectLevel")) + scrollTimesByLevel[o.level] ) {
                            options.remove(i);
                            numOptionsShow = Math.min(numOptionsShow, options.size());
                            i -= 1;
                            continue;
                        }
                        if (i >= gY) continue;
                        jTextGrid.get(gX*i+0).setText(""+(i+1));
                        jTextGrid.get(gX*i+1).setText("CH"+o.level);
                        jTextGrid.get(gX*i+2).setText(scrollSeq[o.level]+"");
                        double tf = (o.targetFrame-o.targetWindow/2.0)/30.0;
                        if (tf<0) tf=0;
                        if (tf >= 100)
                            jTextGrid.get(gX*i+3).setText(String.format("%4d", (int)tf));
                        else jTextGrid.get(gX*i+3).setText(String.format("%4.1f", tf));
                        jTextGrid.get(gX*i+4).setText(o.targetWindow/30.0 >= 10 ? String.format("%2d.", (int)(o.targetWindow/30.0)) : String.format("%3.1f", o.targetWindow/30.0));
                        jTextGrid.get(gX*i+5).setText(seedStr[o.seed]+"");
                        jTextGrid.get(gX*i+6).setText(""+seed.dist(firstSeedToConsider,seedsConsidered[o.seed]));
                        jTextGrid.get(gX*i+7).setText(o.rank >= 100 ? "100.%" : String.format("%4.1f%%", o.rank));
                        jTextGrid.get(gX*i+8).setText(String.format("%4d", Math.round(o.avgDiff)));
                    }
                    
                    for (int i = numOptionsShow; i < gY; i++) {
                        for (int j = 0; j < gX; j++) jTextGrid.get(gX*i+j).setText("");
                    }
                    repaintManip();

                    System.out.println("Done timing options");

                    for (int i = numOptionsShow-1; i >= 0; i--) {
                        Option o = options.get(i);
                        System.out.printf("%2d: CH%-2s %s %3d -> %6.2f + %.2f   %s\n", 
                            i+1, o.level+"", seedStr[o.seed], seed.dist(firstSeedToConsider,seedsConsidered[o.seed]),
                                (o.targetFrame-o.targetWindow/2)/30.0,  o.targetWindow/30.0, 
                                scrollSeq[o.level]);
                    }

                    // find a special level
                    if (!unknownSeed && !realTimeAttackMode) {
                        long startInv = seed.nth_inv(firstSeedToConsider);
                        long bestDist = Long.MAX_VALUE;
                        long bestSeed = -1;
                        int bestLevel = 0;
                        for (int i = 0; i < specialTargetLevels.size(); i++) {
                            long dist = specialTargetSeeds.get(i) - startInv;
                            if (dist < 0) dist += (seed.M/2);
                            if (dist < bestDist) {
                                bestDist = dist;
                                bestSeed = specialTargetSeeds.get(i);
                                bestLevel = specialTargetLevels.get(i);
                                //System.out.println(dist + " " + Integer.parseInt(params.get("maxDistToConsider")) + " " + Drawer.seedToString(seed.nth(bestSeed)));
                                //System.out.println(startSeed + " " + startInv + " " + bestSeed + " " + seed.dist(firstSeedToConsider, seed.nth(bestSeed)));
                            }
                        }
                        if (bestDist < Integer.parseInt(params.get("maxDistToConsider"))) {
                            specialTargetLevel = bestLevel;
                            specialTargetSeed = seed.nth(bestSeed);
                            specialTargetFrame = seed.best_timing(specialTargetLevel, startSeed, specialTargetSeed);
                            specialTargetWindow = seed.frame_window;
                            System.out.println("Special target found: CH" + specialTargetLevel + " " + Drawer.seedToString(specialTargetSeed) +
                                    " " + specialTargetFrame + " " + specialTargetWindow);
                            
                            StringBuilder text = new StringBuilder();
                            text.append("special target:\n CH" + specialTargetLevel + " " + Drawer.seedToString(specialTargetSeed) + "\n ");
                            // show timer, num advances, and window
                            // also append this in the first phase.
                            double t = (specialTargetFrame-specialTargetWindow/2.0)/30.0;
                            if (t >= 60000)
                                text.append(String.format("%dh", (int)t/3600));
                            else if (t >= 10000)
                                text.append(String.format("%dm", (int)t/60));
                            else if (t >= 100)
                                text.append(String.format("%4d", (int)t));
                            else text.append(String.format("%4.1f", t));
                            text.append(" ");
                            
                            double w = specialTargetWindow/30.0;
                            text.append(w >= 10 ? String.format("%2d.", (int)w) : String.format("%3.1f", w));
                            text.append(" ");
    
                            text.append(seed.dist(firstSeedToConsider,specialTargetSeed));
                            text.append("\n");
                        
                            jtext2.setText(text.toString());
                            repaintManip();
                        }
                        else {
                            specialTargetLevel = 0;
                        }
                    } else {
                        specialTargetLevel = 0;
                    }

                    if (!unknownSeed) {
                        CaveViewer.guiOnly = true;
                        caveViewer.imageBuffer.clear();
                        caveViewer.nameBuffer.clear();
                        CaveViewer.manipKeepImages = true;
                        
                        for (int i = 0; i < numOptionsShow; i++) {
                            Option o = options.get(i);
                            String args2 = "cave CH" + o.level + "-1 -noprints -drawpodangle "
                            + "-seed 0x" + seedStr[o.seed];
                            CaveGen.main(args2.split(" "));
                            if (i == 0)  {
                                caveViewer.lastSSeed = 0;
                                caveViewer.jfrView.setVisible(true);
                                caveViewer.firstImg();
                            }
                        }

                        if (specialTargetLevel > 0) {
                            String args2 = "cave CH" + specialTargetLevel + "-1 -noprints -drawpodangle "
                            + "-seed 0x" + Drawer.seedToString(specialTargetSeed);
                            CaveGen.main(args2.split(" "));
                        }

                        CaveViewer.manipKeepImages = false;
                    } else {
                        caveViewer.jfrView.setVisible(false);
                    }

                    readyToGenerate = false;
                    lastStagePlayed = options.size() > 0 ? options.get(0).level : 0;
                    waitForNextFadeout = true;
                    timerStartSeed = startSeed;
                    timerCurSeed = startSeed;
                    timerTargetFrame = 0;
                }

                // show the countdown timers
                if (showTimers) {
                    if (timerTargetFrame == 0) {
                        timerTargetFrame = seed.seed_duration_first(timerStartSeed);
                    }
                    int diff = (int)(System.currentTimeMillis() - timeOfLastFadeout);
                    int timeShow = (int)((diff + Double.parseDouble(params.get("secondsOfDelay"))*1000) * 30.0 / 1000);

                    if (timeShow >= timerTargetFrame) {
                        timerCurSeed = seed.next_seed(timerCurSeed);
                        timerTargetFrame += seed.seed_duration(timerCurSeed);
                    }
                    
                    StringBuilder text = new StringBuilder();
                    text.append(""+String.format("%6.1f\n", timeShow/30.0));
                    double dt = timerTargetFrame - timeShow;
                    dt = Math.max(0,Math.min(30*9.9,dt));
                    text.append(Drawer.seedToString(timerCurSeed) + " " + seed.dist(timerStartSeed,timerCurSeed) + " " 
                            + String.format("%3.1f", dt/30.0) + "\n");
                    text.append(String.format("fd=%d le=%d,tl=%d\n",  numFadeouts, numLevelEnters, numTitleLoops));

                    if (specialTargetSeed > 0) {
                        text.append("special target:\n CH" + specialTargetLevel + " " + Drawer.seedToString(specialTargetSeed) + "\n ");
                        // show timer, num advances, and window
                        // also append this in the first phase.
                        double t = (specialTargetFrame-specialTargetWindow/2.0)/30.0-timeShow/30.0;
                        t = Math.max(0,t);
                        if (numLevelEnters >= 1) t = 0;
                        if (t >= 60000)
                            text.append(String.format("%dh", (int)t/3600));
                        else if (t >= 10000)
                            text.append(String.format("%dm", (int)t/60));
                        else if (t >= 100)
                            text.append(String.format("%4d", (int)t));
                        else text.append(String.format("%4.1f", t));
                        text.append(" ");
                        
                        double w = specialTargetWindow/30.0;
                        if (t <= 0) {
                            w += (specialTargetFrame-specialTargetWindow/2.0)/30.0-timeShow/30.0;
                            w = Math.max(w,0);
                        }
                        if (numLevelEnters >= 1) w = 0;
                        text.append(w >= 10 ? String.format("%2d.", (int)w) : String.format("%3.1f", w));
                        text.append(" ");

                        long distt = seed.dist(timerCurSeed,specialTargetSeed);
                        text.append(distt/4505 + ":" + distt%4505);
                        text.append("\n");
                    }

                    for (int i = 0; i < gY; i++) {
                        if (i >= options.size() || options.get(i).targetWindow <= 0) {
                            jTextGrid.get(gX*i+3).setText("");
                            jTextGrid.get(gX*i+4).setText("");
                            continue;
                        }
                        Option o = options.get(i);
                        double timer = (o.targetFrame-o.targetWindow/2.0)/30.0;
                        double t = timer-timeShow/30.0;
                        t = Math.max(0,t);
                        if (numLevelEnters > 1) t = 0;
                        if (t >= 100)
                            jTextGrid.get(gX*i+3).setText(String.format("%4d", (int)t));
                        else jTextGrid.get(gX*i+3).setText(String.format("%4.1f", t));

                        double w = o.targetWindow/30.0;
                        if (t <= 0) {
                            w += timer-timeShow/30.0;
                            w = Math.max(w,0);
                            if (numLevelEnters> 1) w = 0;
                            jTextGrid.get(gX*i+4).setText(w >= 10 ? String.format("%2d.", (int)w) : String.format("%3.1f", w));
                        }
                    }

                    jtext2.setText(text.toString());
                    repaintManip();

                    int cave = caveViewer.lastSSeed < options.size() ? 
                                    options.get(caveViewer.lastSSeed).level : 1;
                    System.out.printf(" TIMER:  %s %3d -> %6.2f    (cur %s %d)                   \r", 
                                        Drawer.seedToString(seed.seed_from_A(cave, timerCurSeed, timerTargetFrame-timeShow)), 
                                        seed.dist(timerStartSeed,timerCurSeed), timeShow/30.0, Drawer.seedToString(timerCurSeed),
                                        seed.nth_inv(timerCurSeed));


                }


                Thread.sleep(10);
            }
            if (raf != null)
                raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Seed seed;
    Manip(Seed s) {
        seed = s;
    }

    void repaintManip() {

        EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
                jfr.revalidate();
		        jfr.repaint();
            }
        });

    }

    HashMap<String, String> params;
    double splits[] = new double[31], rankCutoffs[] = new double[31];
    boolean levelsToPlay[] = new boolean[31],levelsToIgnore[] = new boolean[31];
    ArrayList<Long> specialTargetSeeds = new ArrayList<Long>();
    ArrayList<Integer> specialTargetLevels = new ArrayList<Integer>();
    void readParams() {
        try {
            params = new HashMap<String, String>();
            BufferedReader br = new BufferedReader(new FileReader("manip_config.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                int hash = line.indexOf("#");
                if (hash >= 0) line = line.substring(0,hash);
                line = line.trim();
                if (line.length() == 0) continue;
                if (line.charAt(0) == '@') {
                    String[] sp = line.split(",");
                    int level = Integer.parseInt(sp[0].toLowerCase().replace("@ch",""));
                    for (int i = 1; i < sp.length; i++) {
                        if (sp[i].length() < 8) continue;
                        long tseed = Long.decode("0x" + sp[i]).longValue() % (seed.M/2);
                        if (tseed < 0) continue;
                        specialTargetSeeds.add(seed.nth_inv(tseed));
                        specialTargetLevels.add(level);
                    }
                    continue;
                }
                if (line.indexOf("=") < 0) continue;
                Scanner sc = new Scanner(line);
                sc.useDelimiter("=");
                String a = sc.next().trim();
                String b = sc.next().trim();
                params.put(a,b);
                sc.close();

                if (a.equals("splits")) {
                    Scanner sc2 = new Scanner(b);
                    sc2.useDelimiter(",");
                    for (int i = 0; i < 30; i++) {
                        splits[i+1] = Double.parseDouble(sc2.next().trim());
                    }
                    int splitsSum = 0;
                    for (int i = 1; i <= 30; i++) {
                        //if (levelsPlayed[i]) {
                            splitsSum += 60*Math.floor(splits[i]) + 100*(splits[i]-Math.floor(splits[i]));
                        //}
                    }
                    System.out.println("Splits time: " + (splitsSum/60) + "m" + (splitsSum%60) + "s");
                    sc2.close();
                }
                if (a.equals("attkRankThreshold")) {
                    Scanner sc2 = new Scanner(b);
                    sc2.useDelimiter(",");
                    for (int i = 0; i < 30; i++) {
                        rankCutoffs[i+1] = Double.parseDouble(sc2.next().trim());
                    }
                    System.out.println("Rank cutoffs: " + Arrays.toString(rankCutoffs));
                    sc2.close();
                }
                if (a.equals("levelsToPlay")) {
                    Scanner sc2 = new Scanner(b);
                    sc2.useDelimiter(",");
                    levelsToPlay = new boolean[31];
                    while(sc2.hasNext()) {
                        int p = Integer.parseInt(sc2.next().trim());
                        levelsToPlay[p] = true;
                    }
                    System.out.println("Override levels to play: " + Arrays.toString(levelsToPlay));
                    sc2.close();
                }
                if (a.equals("levelsToIgnore")) {
                    Scanner sc2 = new Scanner(b);
                    sc2.useDelimiter(",");
                    levelsToIgnore = new boolean[31];
                    while(sc2.hasNext()) {
                        int p = Integer.parseInt(sc2.next().trim());
                        levelsToIgnore[p] = true;
                    }
                    System.out.println("Override levels to ignore: " + Arrays.toString(levelsToIgnore));
                    sc2.close();
                }
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<String> messages = new ArrayList<String>();
    synchronized String message(boolean read, String message) {
        if (read) {
            if (messages.size() == 0) return null;
            return messages.remove(0);
        } else {
            messages.add(message);
            return null;
        }
    }

    class Option {
        double time;
        int seed;
        int level;
        long targetFrame;
        long targetWindow;
        double rank,avgDiff;
        Option(double time, int seed, int level) {
            this.time=time; this.seed=seed; this.level=level;
        }
    }

    void timesTable(String kind) {
        CaveGen.resetParams();
        Parser.readConfigFiles();
        computeStats(kind);

        ArrayList<String> id2 = new ArrayList<String>();
        for (String s: ids) id2.add(s);

        Collections.sort(id2, new Comparator<String>() {
            public int compare(String a, String b) {
                double d = stddevs.get(a) - stddevs.get(b);
                return d < 0 ? -1 : d == 0 ? 0 : 1;
            }
        });
        for (String s: id2) System.out.printf("%6s  %8.3f\n", s, stddevs.get(s));

        int ns = 1000;
        float[][] samp = new float[30][ns];
        float avgs[] = new float[30];

        for (int f = 0; f < 30; f++) {
            double rank[] = readRankFile("CH" + (f+1) + "-1-" + kind);
            for (int s = 0; s < ns; s++) {
                samp[f][s] = (float)rank[(int)((0.5+s)*(1000.0/ns))];
            }
            for (int s = 0; s < ns; s++) {
                int j = s + (int)((ns-s) * Math.random());
                float temp = samp[f][j];
                samp[f][j] = samp[f][s];
                samp[f][s] = temp;
            }
            System.out.println(Arrays.toString(samp[f]));
            avgs[f] = means.get("CH" + (f+1) + "-1").floatValue();
        }

        
        int n = 1 << 30;
        float[] t = new float[n];

        t[0] = 0;
        float[] tt = new float[30];
        boolean[] in = new boolean[30];
        float x = 0, min = 0;

        for (int i = 1; i < n; i++) {

            for (int j = 0; j < 30; j++) {
                int y = 1 << j;
                tt[j] = t[i & (~y)];
                in[j] = (i & y) > 0;
            }

            x = 0;
            for (int s = 0; s < ns; s++) {
                min = Integer.MAX_VALUE;
                for (int j = 0; j < 30; j++) {
                    if (in[j]) {
                        min = Math.min(min, tt[j] + samp[j][s] - avgs[j]);
                    }
                }
                x += min;
            }
            x /= ns;
            t[i] = x;

            if (i % 1024 == 1023)
                System.out.printf("%2d %.3f %s\n", i, x, Arrays.toString(tt));
        }

        try {
            RandomAccessFile raf = new RandomAccessFile("files/times_table_" + kind + ".txt", "rw");
            
            for (int i = 0; i < t.length; i++) {
                //raf.seek(4*i);
                raf.writeFloat(t[i]);
            }

            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 

        /* try {
            RandomAccessFile raf = new RandomAccessFile("files/times_table_" + kind + ".txt", "r");
            
            System.out.println(raf.length());

            for (int i = 0; i < t.length; i++) {
                //raf.seek(4*i);
                
                float rd = raf.readFloat();
                //System.out.printf("%d %.3f\n", i, rd);
            }

            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        } */
    }


    ArrayList<String> ids = new ArrayList<String>();
    HashMap<String, Double> means = new HashMap<String, Double>();
    HashMap<String, Double> topPercentile = new HashMap<String, Double>();
    HashMap<String, Double> stddevs = new HashMap<String, Double>();
    HashMap<String, Double> ranges = new HashMap<String, Double>();
    void computeStats(String kind) {
        if (ids.size() > 0) return;
        for (int f = 1; f <= 30; f++) {
            for (int s = 1; s <= Parser.chFloorCount.get("CH" + f); s++) {
                String id = "CH" + f + "-" + s;
                ids.add(id);

                double[] rank = readRankFile(id + "-" + kind);

                double x = 0, x2 = 0;
                for (int i = 0; i < rank.length; i++) {
                    double k = rank[i];
                    if (k >= 100000) k = rank[500]+40;
                    x += k;
                    x2 += k * k;
                }
                x /= rank.length;
                means.put(id, x);
                stddevs.put(id, Math.sqrt(Math.max(0,x2/rank.length - x*x)));
                ranges.put(id, rank[rank.length-1]-rank[0]);
                topPercentile.put(id, rank[rank.length*5/100]);
            } 
        }
    }

    ArrayList<String> story_ids = new ArrayList<String>();
    HashMap<String, Double> story_means = new HashMap<String, Double>();
    HashMap<String, Double> story_topPercentile = new HashMap<String, Double>();
    HashMap<String, Double> story_stddevs = new HashMap<String, Double>();
    HashMap<String, Double> story_ranges = new HashMap<String, Double>();
    void computeStatsStory(String kind) {
        if (!(kind.equals("pod") || kind.equals("at"))) return;
        if (story_ids.size() > 0) return;
        for (String st: Parser.special) {
            if (st.length() > 2 && st.substring(0,2).equals("CH")) continue;
            for (int s = 1; s <= 20; s++) {
                String id = st + "-" + s;

                //if (s == 1) readRankFile(id + "-" + kind);

                if (!rankFile.containsKey(id + "-" + kind)) continue;

                story_ids.add(id);
                double[] rank = readRankFile(id + "-" + kind);

                double x = 0, x2 = 0;
                for (int i = 0; i < rank.length; i++) {
                    double k = rank[i];
                    if (k >= 100000) k = rank[500]+40;
                    x += k;
                    x2 += k * k;
                }
                x /= rank.length;
                story_means.put(id, x);
                story_stddevs.put(id, Math.sqrt(Math.max(0,x2/rank.length - x*x)));
                story_ranges.put(id, rank[rank.length-1]-rank[0]);
                story_topPercentile.put(id, rank[rank.length*50/100]);
            } 
        }
    }

    HashMap<String, double[]> rankFile = null; 
    double[] readRankFile(String id) {
        if (rankFile == null) {
            rankFile = new HashMap<String, double[]>();
            try {
                BufferedReader br = new BufferedReader(new FileReader("files/rank_file.txt"));
                String line;
                while ((line = br.readLine()) != null) {
                    int idx = line.indexOf(';');
                    if (idx != -1) {
                        String[] st = line.substring(idx+1).split(";");
                        double rankBreakPoints[] = new double[1000];
                        for (int ii = 0; ii < 1000; ii++) {
                            rankBreakPoints[ii] = Double.parseDouble(st[ii]);
                        }
                        rankFile.put(line.substring(0, idx), rankBreakPoints);
                    }
                }
                br.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return rankFile.get(id);
    }
    
    public char[] intToByteArray(int value) {
        return new char[] {
                (char)(value >>> 24),
                (char)(value >>> 16),
                (char)(value >>> 8),
                (char)value};
    }

    int numDigitsRead = 0;
    int[][] digitsRead;
    long timeOfLastFadeout;
    int numFadeouts;
    long timeOfLastLevelEnter;
    int numTitleLoops;
    int numLevelEnters;
    long lastReadSeed;
    boolean seedRead;

    String columnBlanks;
    int firstNoAdvanceFrame, numBlankColumns, numDigitsUse, numFramesUse;
    int[] mostRecentDigit, consecutiveDigits;

    void resetDigitTrackers() {
        readParams();
        seedRead = false;
        lastReadSeed = -1;
        numFadeouts = 0;
        numTitleLoops = 0;
        numLevelEnters = 0;
        timeOfLastLevelEnter = 0;
        timeOfLastFadeout = 0;
        digitsRead = new int[3000][5];
        numBlankColumns = 0;
        columnBlanks = "";
        firstNoAdvanceFrame = -1;
        numDigitsUse = -1;
        numFramesUse = -1;
        mostRecentDigit = new int[]{-1,-1,-1,-1,-1};
        consecutiveDigits = new int[]{0,0,0,0,0};
    }

    void tryReadSeed() {
        int n = numDigitsRead;

        for (int j = 0; j < 5; j++) {
            if (digitsRead[n-1][j] == mostRecentDigit[j]) {
                consecutiveDigits[j]++;
            } else {
                consecutiveDigits[j] = 1;
                mostRecentDigit[j] = digitsRead[n-1][j];
            }
        }

        if (n < 15) return;

        // find blank columns
        if (n == 15) {
            for (int j = 0; j < 5; j++) {
                int count = 0;
                for (int i = 0; i < n; i++) {
                    if (digitsRead[i][j] == -1)
                        count += 1;
                }
                if (count * 1.0 / n > 0.8) {
                    columnBlanks += "_";
                    numBlankColumns += 1;
                } else {
                    columnBlanks += "X";
                }
            }
            System.out.println(columnBlanks + " (" + numBlankColumns + " blank)");
        }

        // see if we are on the final frame where any advance happens
        if (columnBlanks.equals("____X")) {
            if (mostRecentDigit[4] == 0) {
                numDigitsUse = 1;
                numFramesUse = 2;
                firstNoAdvanceFrame = n;
            }
        } else if (columnBlanks.equals("___XX")) {
            if (consecutiveDigits[3] >= 1 || consecutiveDigits[4] >= 2) {
                numDigitsUse = 2;
                numFramesUse = 2;
                firstNoAdvanceFrame = n;
            }
        } else if (columnBlanks.equals("__XXX")) {
            if (consecutiveDigits[2] >= 1 && consecutiveDigits[3] >= 2 && consecutiveDigits[4] >= 4) {
                numDigitsUse = 3;
                numFramesUse = 2;
                firstNoAdvanceFrame = n;
            }
        } else if (columnBlanks.equals("_XXXX")) {
            if (consecutiveDigits[1] >= 1 && consecutiveDigits[2] >= 2 && consecutiveDigits[3] >= 4 && consecutiveDigits[4] >= 6) {
                numDigitsUse = 4;
                numFramesUse = 2;
                firstNoAdvanceFrame = n;
            }
        } else if (columnBlanks.equals("XXXXX")) {
            if (consecutiveDigits[0] >= 1 && consecutiveDigits[1] >= 2 && consecutiveDigits[2] >= 4 && consecutiveDigits[3] >= 6 && consecutiveDigits[4] >= 8) {
                numDigitsUse = 5;
                numFramesUse = 2;
                firstNoAdvanceFrame = n;
            }
        } else { // bad blank columns
            seedRead = true;
            return;
        }

        if (numDigitsUse == -1) return;
        seedRead = true;
        
        // recover the digit sequence to search for
        System.out.println("Using last " + numDigitsUse + " digits");
        String sequenceFull = "";
        for (int i = Math.max(0, firstNoAdvanceFrame - 16); i < Math.min(Math.min(n,3000),firstNoAdvanceFrame+5); i++) {
            for (int j = 0; j < 5; j++)
                System.out.print(digitsRead[i][j] == -1 ? '_' : (char)(digitsRead[i][j]+'0'));
            System.out.println();
            for (int j = 4; j >= 0; j--) {
                if (numDigitsUse + j < 5) continue; // only use non-blank columns
                if (firstNoAdvanceFrame - i <= Math.max(1, (j + numDigitsUse - 5) * numFramesUse)) continue;
                sequenceFull += digitsRead[i][j];
            }
            if (i == firstNoAdvanceFrame-2) // there is some off by one strangeness in this...
                System.out.println("-----");
        }
        System.out.println("Full Sequence: " + sequenceFull);

        // search for the sequence
        String sequence = "";
        ArrayList<Long> candidates = new ArrayList<Long>();
        int candidateAdvances = 0;
        for (int i = Math.min(sequenceFull.length(), 12); i <= Math.min(sequenceFull.length(), 50); i++) {
            sequence = sequenceFull.substring(sequenceFull.length()-i);
            candidates = seed.sequence_to_seed(sequence);
            candidateAdvances = i;
            if (candidates.size() < 2) break;
        }
        if (candidates.size() == 0) {
            System.out.println("Trying alt sequencing strat");
            for (int i = Math.min(sequenceFull.length(), 13); i <= Math.min(sequenceFull.length(), 50); i++) {
                sequence = sequenceFull.substring(sequenceFull.length()-i, sequenceFull.length()-2);
                candidates = seed.sequence_to_seed(sequence);
                candidateAdvances = i;
                if (candidates.size() < 2) break;
            }
            if (candidates.size() == 0) {
                System.out.println("Sequence: " + sequence);
                System.out.println("Digit processing error: no seed for this sequence found");
                return;
            }
        }
        if (candidates.size() > 1) {
            System.out.println("Warning, multiple candidates. Consider editing seed_last_known.txt");
            for (Long i: candidates) {
                long seedF = seed.next_seed(i, sequenceFull.length());
                System.out.println("  " + seed.seedToString(seedF) + " (" + seed.nth_inv(seedF) + ")\t-> " + seed.seed_to_sequence(i, 50));
            }
        }

        lastReadSeed = seed.next_seed(candidates.get(0), candidateAdvances);
        System.out.println("Seed found: " + Drawer.seedToString(lastReadSeed));
    }

}