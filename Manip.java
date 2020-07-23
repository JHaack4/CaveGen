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
    //JComboBox<String> jComboBox = new JComboBox<String>();
    //JTextField jTextField = new JTextField();
    ArrayList<JTextPane> jTextGrid = new ArrayList<JTextPane>();

    void manip() {

        readParams();
        String kind = params.get("mode");

        jfr.getContentPane().setLayout(null);
		jfr.setSize(434, 555);
        //jfr.setResizable(false);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        jtext.setText("mode: " + kind);
        jtext.setFont(font);
        jtext.setEditable(false);
        jtext.setContentType("text/plain");
        jtext.setBackground(null);
        jtext.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        jtext.setBounds(45,5,150,110);
        jfr.add(jtext);

        jtext2.setText("");
        jtext2.setFont(font);
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

        int[] gA = {20,40,50,60,60,80,30};
        int gX = gA.length, gY = 25;
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
                jg.setBounds(x + 5,125 + 16*i,gA[j],16);
                jfr.add(jg);
                jTextGrid.add(jg);
                x += gA[j]+5;
            }
        }

        jfr.revalidate();
		jfr.repaint();
        jfr.setVisible(true);

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
        boolean levelsPlayed[] = new boolean[31];
        int numLevelsPlayed = 0;
        int lastStagePlayed = 0;
        long runStartTime = 0;
        double stageStddevs[] = new double[31];
        double timers[] = new double[gY];
        ArrayList<Option> options = null;

        CaveGen.resetParams();
        Parser.readConfigFiles();
        computeStats(kind);

        long timerTargetFrame = 0;
        long timerCurSeed = 0;
        long timerStartSeed = 0;

        for (int i = 1; i <= 30; i++) {
            for (int j = 2; j <= Parser.chFloorCount.get("CH"+i); j++) {
                stageStddevs[i] += stddevs.get("CH"+i+"-"+j) * stddevs.get("CH"+i+"-"+j);
            }
            stageStddevs[i] = Math.sqrt(stageStddevs[i]);
            //System.out.println(i + ": " + stageStddevs[i]);
        }
        System.out.println("stdevs: " + Arrays.toString(stageStddevs));

        try {
            resetDigitTrackers();
            RandomAccessFile raf = new RandomAccessFile("files/times_table_" + kind + ".txt", "r");

            while (true) {
                String r = message(true, null);
                long time = System.currentTimeMillis();
                if (r != null) {
                    //System.out.println("continuous: " + r);
                    if (r.equals("exit"))
                        break;

                    String[] s = r.split(" ");
                    if (s[0].equals("digits")) {
                        if (numDigitsRead == 0) {
                            resetDigitTrackers();
                            for (int i = 0; i < gY; i++) {
                                for (int j = 0; j < gX; j++) jTextGrid.get(gX*i+j).setText("");
                                timers[i] = -1;
                            }
                            repaintManip();
                        }
                        if (numDigitsRead < 3000) {
                            for (int i = 0; i < 5; i++) {
                                char c = s[1].charAt(i);
                                digitsRead[numDigitsRead][i] = c == '_' ? -1 : c - '0';
                            }
                            numDigitsRead += 1;
                        }
                        if (!seedRead) {
                            tryReadSeed();
                            if (seedRead && lastReadSeed == -1) {
                                System.out.println("Failed to detect seed, you are on your own");
                                readyToGenerate = true;
                            } else if (seedRead) {
                                System.out.println("Detected seed: " + Drawer.seedToString(lastReadSeed) + " " + seed.nth_inv(lastReadSeed));
                                readyToGenerate = true;
                            }
                        }
                        if (waitForNextSeed) {
                            System.out.println("\nStarting to look for next seed");
                            waitForNextSeed = false;
                            showTimers = false;
                            if (options.size() > caveViewer.lastSSeed) {
                                levelsPlayed[options.get(caveViewer.lastSSeed).level] = true;
                                numLevelsPlayed += 1;
                                lastStagePlayed = options.get(caveViewer.lastSSeed).level;
                            }
                        }
                    }
                    if (s[0].equals("fadeout")) {
                        numDigitsRead = 0;
                        if (time - timeOfLastFadeout < 1000) {
                            timeOfLastFadeout = time;
                        } else {
                            numFadeouts += 1;
                            timeOfLastFadeout = time;
                            System.out.println("Detect fadeout " + numFadeouts + "\t\t\t\t\t\t\t\t");
                        }
                        if (waitForNextFadeout) {
                            waitForNextFadeout = false;
                            waitForNextSeed = true;
                            showTimers = true;
                        }
                        if (numLevelsPlayed == 0 && numFadeouts == 2 && seedRead) {
                            runStartTime = time;
                        }
                    }
                    if (s[0].equals("donedigit")) {
                        if (numDigitsRead > 0 && !seedRead) {
                            System.out.println("Missed seed, you are on your own");
                            seedRead = true;
                            readyToGenerate = true;
                            lastReadSeed = -1;
                        }
                    }
                }

                // cmal manip
                if (kind.equals("key")) {
                    if (readyToGenerate) {

                        StringBuilder text1 = new StringBuilder();

                        boolean unknownSeed = lastReadSeed == -1;
                        int numSeedsToConsider = numLevelsPlayed == 0 ? (int)Double.parseDouble(params.get("secondsWaitingForLevel"))/6 : 5;
                        if (unknownSeed) {
                            lastReadSeed = 0;
                            numSeedsToConsider = 1;
                        }
                        long firstSeedToConsider = seed.next_seed(lastReadSeed, 1004);
                        long startSeed = seed.next_seed(lastReadSeed, 4);

                        String args = "none CH1-1,CH2-1,CH3-1,CH4-1,CH5-1,CH6-1,CH7-1,CH8-1,CH9-1,CH10-1,"
                         + "CH11-1,CH12-1,CH13-1,CH14-1,CH15-1,CH16-1,CH17-1,CH18-1,CH19-1,CH20-1,CH21-1,"
                         + "CH22-1,CH23-1,CH24-1,CH25-1,CH26-1,CH27-1,CH28-1,CH29-1,CH30-1 "
                         + "-consecutiveseeds -seed 0x" + Drawer.seedToString(firstSeedToConsider) 
                         + " -num " + numSeedsToConsider + " -judge " + kind;
                        System.out.println("Generating levels...");
                        CaveGen.main(args.split(" "));
                        System.out.println("Done generating levels.");

                        int waitTimesBySeed[] = new int[numSeedsToConsider];
                        int scrollTimesByLevel[] = new int[31];
                        float optimalResetGiveupByLevel[] = new float[31];
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
                            optimalResetGiveupByLevel[i] = (float)(stageStddevs[i] * Double.parseDouble(params.get("secondsGiveUpPerSecondVol")) * (1-numLevelsPlayed/30.0));
                            if (numLevelsPlayed == 0) continue; // all 0
                            int rc = (i-1) / 5;
                            int cc = (i-1) % 5;
                            int rd = (rc-rl+6) % 6;
                            int cd = (cc-cl+5) % 5;
                            int dist = Math.min(rd, 6-rd) + Math.min(cd, 5-cd);
                            scrollTimesByLevel[i] = Integer.parseInt(params.get("framesNeededToScrollLevel")) * dist;
                            scrollSeq[i] = (rd==0?"":rd==1?"v":rd==2?"vv":rd==3?"vvv":rd==4?"^^":"^")
                                        + (cd==0?"":cd==1?">":cd==2?">>":cd==3?"<<":"<");
                        }

                        float[] remainingTimesByLevel = new float[31];
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

                        StringBuilder textPlay = new StringBuilder();
                        for (int i = 1; i <= 30; i++) {
                            System.out.print(levelsPlayed[i] ? "X" : "O");
                            textPlay.append(levelsPlayed[i] ? "X" : "O");
                            if (i%5 == 0) {
                                System.out.println();
                                textPlay.append("\n");
                            }
                        }
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
                        jtext.setText(text1.toString());
                        jtextplay.setText(textPlay.toString());
                        repaintManip();

                        long[] seedsConsidered = new long[numSeedsToConsider];
                        String[] seedStr = new String[numSeedsToConsider];
                        options = new ArrayList<Option>(30*numSeedsToConsider);

                        for (int i = 0; i < numSeedsToConsider; i++) {
                            seedsConsidered[i] = seed.next_seed(lastReadSeed, 1004+i);
                            seedStr[i] = Drawer.seedToString(seedsConsidered[i]);
                            for (int j = 1; j <= 30; j++) {
                                //System.out.println(" CH"+j+"-1 " + seedStr);
                                double playt = CaveGen.stats.judge.scoreMap.get("CH"+j+"-1 " + seedStr[i])
                                                - means.get("CH"+j+"-1");
                                double t = Math.max(scrollTimesByLevel[j], waitTimesBySeed[i]) / 30.0
                                    + remainingTimesByLevel[j]
                                    + (unknownSeed ? 0 : playt)
                                    - optimalResetGiveupByLevel[j]
                                    + (levelsPlayed[j] ? 1000 : 0);
                                options.add(new Option(t, i, j));
                            }
                        }

                        Collections.sort(options, new Comparator<Option>() {
                            public int compare(Option a, Option b) {
                                return Double.compare(a.time, b.time);
                            }
                        });

                        int numOptionsShow = Math.min(numLevelsPlayed == 0 ? 30 : 5,options.size());

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
                            o.targetFrame = seed.best_timing("CH"+o.level, startSeed, seedsConsidered[o.seed]);
                            o.targetWindow = seed.frame_window;
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
                            jTextGrid.get(gX*i+3).setText(String.format("%6.1f", (o.targetFrame-o.targetWindow/2)/30.0));
                            jTextGrid.get(gX*i+4).setText(String.format("%6.1f", o.targetWindow/30.0));
                            jTextGrid.get(gX*i+5).setText(seedStr[o.seed]+"");
                            jTextGrid.get(gX*i+6).setText(""+seed.dist(firstSeedToConsider,seedsConsidered[o.seed]));
                            timers[i] = (o.targetFrame-o.targetWindow/2)/30.0;
                            repaintManip();
                        }
                        

                        for (int i = numOptionsShow; i < gY; i++) {
                            for (int j = 0; j < gX; j++) jTextGrid.get(gX*i+j).setText("");
                            timers[i] = -1;
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

                        CaveViewer.manipKeepImages = false;

                        readyToGenerate = false;
                        lastStagePlayed = options.get(0).level;
                        waitForNextFadeout = true;
                        timerStartSeed = startSeed;
                        timerCurSeed = startSeed;
                        timerTargetFrame = 0;
                    }

                    if (showTimers) {
                        if (timerTargetFrame == 0) {
                            timerTargetFrame = seed.seed_duration_first(timerStartSeed);
                        }
                        int diff = (int)(System.currentTimeMillis() - timeOfLastFadeout);
                        int timeShow = (int)((diff + Double.parseDouble(params.get("secondsOfDelay"))*1000) * 30.0 / 1000);

                        //System.out.println(timeShow + " " + timerTargetFrame + " " + timeOfLastFadeout + " " + diff);

                        if (timeShow >= timerTargetFrame) {
                            timerCurSeed = seed.next_seed(timerCurSeed);
                            timerTargetFrame += seed.seed_duration(timerCurSeed);
                        }
                        
                        StringBuilder text = new StringBuilder();
                        text.append(""+String.format("%6.1f\n", timeShow/30.0));
                        text.append(Drawer.seedToString(timerCurSeed) + " " + seed.dist(timerStartSeed,timerCurSeed) + "\n");

                        for (int i = 0; i < gY; i++) {
                            if (timers[i] == -1) {
                                jTextGrid.get(gX*i+3).setText("");
                                continue;
                            }
                            double t = timers[i]-timeShow/30.0;
                            t = Math.max(0,t);
                            if (numFadeouts > 1) t = 0;
                            jTextGrid.get(gX*i+3).setText(String.format("%6.1f", t));

                            double w = options.get(i).targetWindow/30.0;
                            if (t <= 0) {
                                w += timers[i]-timeShow/30.0;
                                w = Math.max(w,0);
                                jTextGrid.get(gX*i+4).setText(String.format("%6.1f", w));
                            }
                        }

                        jtext2.setText(text.toString());
                        repaintManip();

                        String cave = caveViewer.lastSSeed < options.size() ? 
                                        "CH" + options.get(caveViewer.lastSSeed).level : "CH1";
                        System.out.printf(" TIMER:  %s %3d -> %6.2f    (cur %s %d)                   \r", 
                                            Drawer.seedToString(seed.seed_from_A(cave, timerCurSeed, timerTargetFrame-timeShow)), 
                                            seed.dist(timerStartSeed,timerCurSeed), timeShow/30.0, Drawer.seedToString(timerCurSeed),
                                            seed.nth_inv(timerCurSeed));


                    }


                }




                Thread.sleep(10);
            }
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
    double splits[] = new double[31];
    void readParams() {
        try {
            params = new HashMap<String, String>();
            BufferedReader br = new BufferedReader(new FileReader("files/manip_config.txt"));
            String line;
            while ((line = br.readLine()) != null) {
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
            System.out.println("Sequence: " + sequence);
            System.out.println("Digit processing error: no seed for this sequence found");
            return;
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