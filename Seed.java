import java.util.*;
import java.io.*;
import java.math.BigInteger;

public class Seed {

    public static void main(String[] args) {
        new Seed().run(args);
    }

    Manip manip;
    Letters letters;

    String helpString = "Usage:\n  Seed nth n\n  Seed nthinv seed\n  Seed dist seed1 seed2\n  Seed next seed [n]\n  Seed seed2seq seed\n  Seed seq2seed seq\n  Seed ieee hex\n  Seed digit seed\n  Seed frames seed\n  Seed window cave seed\n" +
        "  python setup_seed_detect.py - used to setup the real time seed finder\n" +
        "  Seed manip key|cmat|700k|attk - run real time manip";

    void run(String args[]) {
        manip = new Manip(this);
        letters = new Letters(this);
        try {
            if (args.length == 0) {
                System.out.println(helpString);
            } else if (args[0].equalsIgnoreCase("nth") && args.length >= 2) {
                System.out.println(seedToString(nth(Long.parseLong(args[1]))));
            } else if (args[0].equalsIgnoreCase("digit") && args.length >= 2) {
                System.out.println(digit_observation(Long.decode(args[1])));
            } else if (args[0].equalsIgnoreCase("frames") && args.length >= 2) {
                System.out.println(seed_duration(Long.decode(args[1])));
            } else if (args[0].equalsIgnoreCase("window") && args.length >= 3) {
                long sd = Long.decode(args[2]);
                best_timing(Integer.parseInt(args[1].toLowerCase().replace("ch", "")), next_seed(sd,-1005), sd);
                System.out.println(Math.max(0,frame_window));
            } else if (args[0].equalsIgnoreCase("fframes") && args.length >= 2) {
                System.out.println(seed_duration_first(Long.decode(args[1])));
            } else if (args[0].equalsIgnoreCase("nthinv") && args.length >= 2) {
                System.out.println(nth_inv(Long.decode(args[1])));
            } else if (args[0].equalsIgnoreCase("dist") && args.length >= 3) {
                System.out.println(dist(Long.decode(args[1]), Long.decode(args[2])));
            } else if (args[0].equalsIgnoreCase("next") && args.length >= 3) {
                System.out.println(seedToString(next_seed(Long.decode(args[1]), Long.decode(args[2]))));
            } else if (args[0].equalsIgnoreCase("next") && args.length >= 2) {
                System.out.println(seedToString(next_seed(Long.decode(args[1]))));
            } else if (args[0].equalsIgnoreCase("seq2seed") && args.length >= 2) {
                ArrayList<Long> r = sequence_to_seed(args[1]);
                for (Long i: r) System.out.println(seedToString(i));
            }  else if (args[0].equalsIgnoreCase("seed2seq") && args.length >= 3) {
                System.out.println(seed_to_sequence(Long.decode(args[1]), Integer.parseInt(args[2])));
            } else if (args[0].equalsIgnoreCase("seed2seq") && args.length >= 2) {
                System.out.println(seed_to_sequence(Long.decode(args[1]), 30));
            } else if (args[0].equalsIgnoreCase("ieee") && args.length >= 2) {
                if (args[1].length() == 8) {
                    System.out.println(Float.intBitsToFloat(Long.decode("0x" + args[1]).intValue()));
                }
                if (args[1].length() == 16) {
                    System.out.println(Double.longBitsToDouble(Long.parseUnsignedLong(args[1],16)));
                }
            } else if (args[0].equalsIgnoreCase("int") && args.length >= 2) {
                System.out.println(Long.decode("0x" + args[1]));
            }
            else if (args[0].equalsIgnoreCase("test")) {
                runTests();
            } 
            else if (args[0].equalsIgnoreCase("inferseed")) {
                processDigits();
            } 
            else if (args[0].equalsIgnoreCase("caveviewer") && args.length >= 2) {
                openCaveViewer(args[1], args);
            } 
            else if (args[0].equalsIgnoreCase("timer") && args.length >= 2) {
                timer(args);
            } 
            else if (args[0].equalsIgnoreCase("titleloop") && args.length >= 2) {
                titleLoop(Integer.parseInt(args[1]));
            }
            else if (args[0].equalsIgnoreCase("titleloop")) {
                titleLoop(1);
            }
            else if (args[0].equalsIgnoreCase("timestable") && args.length >= 2) {
                manip.timesTable(args[1]);
            }
            else if ((args[0].equalsIgnoreCase("manip") || args[0].equalsIgnoreCase("detect")) && args.length >= 2) {
                manip.manip(args[1]);
            }
            else if (args[0].equalsIgnoreCase("lettersim") && args.length >= 1) {
                letters.letterSim();
            } 
            else if (args[0].equalsIgnoreCase("letters") && args.length >= 2) {
                letters.letters(args[1],-1);
            }
            else if (args[0].equalsIgnoreCase("search") && args.length >= 1) {
                //letters.searchForLowDisutilNear();
            }
            else if (args[0].equalsIgnoreCase("pretty") && args.length >= 1) {
                new PrettyAlign().run();
            }
            else if (args[0].equalsIgnoreCase("diffs") && args.length >= 2) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(args[1]));
                    String line;
                    long last_n = -1;
                    while ((line = br.readLine()) != null) {
                        String s = line.split(" ")[1];
                        long n = nth_inv(Long.decode("0x"+s));
                        if (last_n>0) System.out.println(n-last_n + "\t" + line.split(" ")[0]);
                        last_n = n;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(helpString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n" + helpString);
        }
    }

    String seedToString(long seed) {
        if (seed < 0) seed += ((1-seed/M) * M);
        seed %= M;
        String seedN = Long.toHexString(seed).toUpperCase();
        seedN = String.format("%8s",seedN).replace(" ", "0");
        return seedN;
    }
	
	void runTests() {

        long a = best_timing(24, 1, next_seed(1,1025));
        System.out.println("new " + a + " " + frame_window);
        a = best_timing_slow(24, 1, next_seed(1,1025));
        System.out.println("old " + a + " " + frame_window);

        System.out.println(seed_duration(next_seed(0x9a77e115,4)));

		System.out.println(seed_to_sequence(1227587417, 15));
		
		System.out.println("method LLL");
		ArrayList<Long> seed1 = sequence_to_seed("9731927384");
		for (long i: seed1) System.out.println(i + " " + seed_to_sequence(i,20));
		System.out.println("slow method");
		ArrayList<Integer> seedL = sequence_to_seed_slow("9731927384");
		for (int i: seedL) System.out.println(i + " " + seed_to_sequence(i,20));
	
		long[] tests = new long[] {0, 1, 10, 25, 60, 65535, 65536, C-1, C, C+1, A-1, A, A+1, M/2-1, M/2, M/2+1, M-2, M-1};
	
		for (long i: tests) {
			long x = nth(i);
			System.out.println(i + ": " + x + " -> " + nth_inv(x) + " " + nth_inv2(x) + " " + nth_inv3(x));
		}
    }

    void processDigits() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/seed_digits_parsed.txt"));
            int[][] digits = new int[10000][5];
            String line;
            int n = 0;
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < 5; i++)
                    digits[n][i] = line.charAt(i) == '_' ? -1 : line.charAt(i) - '0';
                n++;
            }
            br.close();

            // check for blank columns
            int numBlankColumns = 0;
            boolean[] columnIsBlank = new boolean[5];
            System.out.print("Detected: ");
            for (int j = 0; j < 5; j++) {
                int count = 0;
                for (int i = 0; i < n; i++) {
                    if (digits[i][j] == -1)
                        count += 1;
                }
                if (count * 1.0 / n > 0.8) {
                    columnIsBlank[j] = true;
                    numBlankColumns += 1;
                    System.out.print("_");
                } else {
                    columnIsBlank[j] = false;
                    System.out.print("X");
                }
            }
            System.out.println(" (" + numBlankColumns + " blank)");

            // determine the final frame where any advance happens
            int firstNoAdvanceFrame = -1;
            int numDigitsUse = -1;
            int[] mostRecentDigit = {-1,-1,-1,-1,-1};
            int[] consecutiveDigits = {0,0,0,0,0};
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < 5; j++) {
                    if (digits[i][j] == mostRecentDigit[j]) {
                        consecutiveDigits[j]++;
                    } else {
                        consecutiveDigits[j] = 1;
                        mostRecentDigit[j] = digits[i][j];
                    }
                }

                if (    (columnIsBlank[0]) &&
                        (columnIsBlank[1]) &&
                        (columnIsBlank[2]) &&
                        (columnIsBlank[3]) &&
                        (!columnIsBlank[4]) &&
                        (mostRecentDigit[4] == 0) ) {
                    numDigitsUse = 1;
                    firstNoAdvanceFrame = i;
                    break;
                }
                if (    (columnIsBlank[0]) &&
                        (columnIsBlank[1]) &&
                        (columnIsBlank[2]) &&
                        (!columnIsBlank[3]) &&
                        (!columnIsBlank[4]) &&
                        (mostRecentDigit[4] == 0 || consecutiveDigits[4] > 2) ) {
                    numDigitsUse = 2;
                    firstNoAdvanceFrame = i;
                    break;
                }
                if (    (columnIsBlank[0]) &&
                        (columnIsBlank[1]) &&
                        (!columnIsBlank[2]) &&
                        (!columnIsBlank[3]) &&
                        (!columnIsBlank[4]) &&
                        (mostRecentDigit[3] == 0 || consecutiveDigits[3] > 2) &&
                        (mostRecentDigit[4] == 0 || consecutiveDigits[4] > 4) ) {
                    numDigitsUse = 3;
                    firstNoAdvanceFrame = i;
                    break;
                }
                if (    (columnIsBlank[0]) &&
                        (!columnIsBlank[1]) &&
                        (!columnIsBlank[2]) &&
                        (!columnIsBlank[3]) &&
                        (!columnIsBlank[4]) &&
                        (mostRecentDigit[2] == 0 || consecutiveDigits[2] > 2) &&
                        (mostRecentDigit[3] == 0 || consecutiveDigits[3] > 4) &&
                        (mostRecentDigit[4] == 0 || consecutiveDigits[4] > 6) ) {
                    numDigitsUse = 4;
                    firstNoAdvanceFrame = i;
                    break;
                }
                if (    (!columnIsBlank[0]) &&
                        (!columnIsBlank[1]) &&
                        (!columnIsBlank[2]) &&
                        (!columnIsBlank[3]) &&
                        (!columnIsBlank[4]) &&
                        (mostRecentDigit[1] == 0 || consecutiveDigits[1] > 2) &&
                        (mostRecentDigit[2] == 0 || consecutiveDigits[2] > 4) &&
                        (mostRecentDigit[3] == 0 || consecutiveDigits[3] > 6) &&
                        (mostRecentDigit[4] == 0 || consecutiveDigits[4] > 8) ) {
                    numDigitsUse = 5;
                    firstNoAdvanceFrame = i;
                    break;
                }
            }
            if (numDigitsUse == -1) {
                System.out.println("Digit processing error - no end found");
                return;
            }
            
            // recover the digit sequence to search for
            System.out.println("Using last " + numDigitsUse + " digits");
            String sequenceFull = "";
            for (int i = Math.max(0, firstNoAdvanceFrame - 16); i < Math.min(n,firstNoAdvanceFrame+5); i++) {
                if (i == firstNoAdvanceFrame)
                    System.out.println("-----");
                for (int j = 0; j < 5; j++)
                    System.out.print(digits[i][j] == -1 ? '_' : (char)(digits[i][j]+'0'));
                System.out.println();
                for (int j = 4; j >= 0; j--) {
                    if (numDigitsUse + j < 5) continue; // only use non-blank columns
                    if (firstNoAdvanceFrame - i <= Math.max(1, (j + numDigitsUse - 5) * 2)) continue;
                    sequenceFull += digits[i][j];
                }
            }
            System.out.println("Full Sequence: " + sequenceFull);

            // search for the sequence
            String sequence = "";
            ArrayList<Long> candidates = new ArrayList<Long>();
            for (int i = Math.min(sequenceFull.length(), 10); i <= Math.min(sequenceFull.length(), 50); i++) {
                sequence = sequenceFull.substring(sequenceFull.length()-i);
                candidates = sequence_to_seed(sequence);
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
                    long seedF = next_seed(i, sequenceFull.length());
                    System.out.println("  " + seedToString(seedF) + " (" + nth_inv(seedF) + ")\t-> " + seed_to_sequence(i, 50));
                }
            }

            // set the last known seed
            long seed = next_seed(candidates.get(0), sequence.length());
            String verify = seed_to_sequence(next_seed(seed, -sequenceFull.length()), sequenceFull.length());
            System.out.println("Full Verify:   " + verify + " " + verify.equals(sequenceFull));
            System.out.println("Sequence: " + sequence + " (length " + sequence.length() + ")");
            System.out.println("Last known seed: " + seedToString(seed) + " (" + nth_inv(seed) + ")");

            PrintWriter oWriter = new PrintWriter(new BufferedWriter(new FileWriter("files/seed_last_known.txt")));
            oWriter.write(seedToString(seed) + "\n");
            oWriter.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void titleLoop(int n) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/seed_last_known.txt"));
            String line = "";
            long seed = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0)
                    seed = Long.decode("0x" + line.trim());
            }
            br.close();

            seed = next_seed(seed, 4505 * n);
            System.out.println("Last known seed: " + seedToString(seed));

            PrintWriter oWriter = new PrintWriter(new BufferedWriter(new FileWriter("files/seed_last_known.txt")));
            oWriter.write(seedToString(seed) + "\n");
            oWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openCaveViewer(String map, String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/seed_last_known.txt"));
            String line = "";
            long seed = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0)
                    seed = Long.decode("0x" + line.trim());
            }
            br.close();

            seed = next_seed(seed, 1000);
            String s = "gui " + map + " 1 -num 200 -findgoodlayouts 0.025 -run -consecutiveSeeds -noprints -seed 0x" + seedToString(seed);
            for (int i = 2; i < args.length; i++)
                s += args[i];
            CaveViewer.main(s.split(" "));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    volatile boolean interrupt = false;
    void timer(String args[]) {
        try {
            int cave = Integer.parseInt(args[1].toLowerCase().replace("ch", ""));

            // read in the start seed
            long startSeed = 0;
            BufferedReader br = new BufferedReader(new FileReader("files/seed_last_known.txt"));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0)
                    startSeed = Long.decode("0x" + line.trim());
            }
            br.close();

            // read in the target seed (using the closest if there are more than one)
            ArrayList<String> considerations = new ArrayList<String>();
            if (args.length <= 2) {
                BufferedReader br2 = new BufferedReader(new FileReader("files/seed_desired.txt"));
                line = "";
                while ((line = br2.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        considerations.add("0x" + line.trim());
                    }
                }
                br2.close();
            } else {
                for (int i = 2; i < args.length; i++)
                    considerations.add(args[i]);
            }
            long targetSeed = 0;
            long targetDist = Long.MAX_VALUE;
            for (String s: considerations) {
                long seed = Long.decode(s);
                long dist = dist(startSeed, next_seed(seed, -1004));
                if (dist < targetDist) {
                    targetSeed = seed;
                    targetDist = dist;
                }
                seed = seed ^ 0x80000000;
                dist = dist(startSeed, next_seed(seed, -1004));
                if (dist < targetDist) {
                    targetSeed = seed;
                    targetDist = dist;
                }
            }
            
            System.out.println(seedToString(startSeed) + " -> " + seedToString(targetSeed) + " (advances " + targetDist + ")");
            long targetFrames = best_timing(cave, next_seed(startSeed,4), targetSeed);
            long targetWindow = frame_window;
            long frames = targetFrames;

            if (targetDist > 1000000000) {
                System.out.println("Target too far away");
                return;
            }
            System.out.println("This target seed has a window of " + targetWindow + " frames");
            if (targetWindow <= 0) {
                System.out.println("Warning - this seed is theoretically impossible");
            } else if (targetWindow <= 30) {
                System.out.println("Warning - this seed has a tight window");
            }
            if (targetDist > 2500) {
                System.out.println("Warning - target is very far from current seed (consider doing loops)");
            }
            System.out.println("Time the \"Enter Level\" A press when the timer reaches zero.");
            System.out.println("Press Enter (and A for \"Don't save\" simultaneously) to begin timer for " +
                     String.format("%d:%02d.%d%d", frames/1800, (frames%1800)/30, frames%30/3, (int)(frames%3 * 3.3)) );
            System.in.read();
            Scanner sc = new Scanner(System.in);


            long startTime = System.currentTimeMillis();
            long trgTime = startTime + frames * 1000 / 30;
            long curTime = startTime;
            long curSeed = next_seed(startSeed, 4);
            long curSeedTime = startTime + (delay_between_dont_save_and_first_advance + seed_duration_first(curSeed)) * 1000 / 30; 
            while (curTime < trgTime) {
                Thread.sleep(16);
                curTime = System.currentTimeMillis();
                frames = (trgTime - curTime) * 30 / 1000;
                if (curTime >= curSeedTime) {
                    curSeed = next_seed(curSeed);
                    curSeedTime += seed_duration(curSeed) * 1000 / 30;
                }
                System.out.print(timerString(cave, frames, curSeed, (curSeedTime - curTime) * 30 / 1000) + "            \r");
            }
            System.out.println("Timer elapsed... press Enter to exit                                                            ");

            interrupt = false;

            final long fcurTime = curTime;
            final long fcurSeed = curSeed;
            final long fcurSeedTime = curSeedTime;
            final long ftrgTime = trgTime;
            Thread thread = new Thread() {
                public void run() {
                    try {
                        long curTime = fcurTime;
                        long curSeed = fcurSeed;
                        long curSeedTime = fcurSeedTime;
                        long trgTime = ftrgTime;
                        while (!interrupt) {
                            Thread.sleep(16);
                            curTime = System.currentTimeMillis();
                            if (curTime >= curSeedTime) {
                                curSeed = next_seed(curSeed);
                                curSeedTime += seed_duration(curSeed) * 1000 / 30;
                            }
                            System.out.print(timerString(cave, (curTime - trgTime) * 30 / 1000, curSeed, (curSeedTime - curTime) * 30 / 1000) + "            \r");
                        }
                        System.out.println("\nDone");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            sc.nextLine();
            sc.nextLine();
            interrupt = true;
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String timerString(int cave, long frames, long curSeed, long curSeedFramesLeft) {
        long res = seed_from_A(cave, curSeed, curSeedFramesLeft);
        return String.format("%d:%02d.%d%d", frames/1800, (frames%1800)/30, frames%30/3, (int)(frames%3 * 3.3))
            +  String.format(" (current seed: %s %d %3d)", seedToString(curSeed), nth_inv(curSeed), (int)curSeedFramesLeft) 
            +  String.format(" (level seed: %s %s)", seedToString(res), nth_inv(res));
    }

    // Computes the best timing to press A, measured in frames from pressing "Don't Save" to pressing "1-P Challenge"
    // to enter the specified cave.
    // The frame window is output in the frame_window global variable.
    long frame_window;
    long best_timing(int cave, long startSeed, long targetSeed) {
 
        long curFrameCount = 0;
        ArrayList<Long> goodFrames = new ArrayList<Long>();
        long latestEarlyFrame = -1;
        long earliestLateFrame = Long.MAX_VALUE;

        long targetM1 = next_seed(targetSeed, -1);
        long targetM2 = next_seed(targetSeed, -2);
        long targetM1000 = next_seed(targetSeed, -1000);
        long targetP1 = next_seed(targetSeed, 1);
        long targetP2 = next_seed(targetSeed, 2);

        long curSeed = startSeed;
        long framesBeforeAdvance = delay_between_dont_save_and_first_advance + seed_duration_first(startSeed);
        long dist = dist(curSeed, targetM1000);

        int frames_A_to_enter = frames_A_to_enter(cave);

        if (dist < 100001005) {
            while (dist >= 0) {

                if (dist <= 3) { // close (check frame by frame)

                    //System.out.println(nth_inv(curSeed) + " " + framesBeforeAdvance + " " + frames_A_to_enter);
                    long currentMinThresh = framesBeforeAdvance - frames_A_to_enter;
                    long resSeed = next_seed(curSeed, 1000);
                    
                    for (int i = 0; i < framesBeforeAdvance; i++) {
                        while (i > currentMinThresh) {
                            resSeed = next_seed(resSeed);
                            currentMinThresh += seed_duration(resSeed);
                            //System.out.println("   " + i + " " + nth_inv(resSeed) + " " + currentMinThresh + " " + seed_duration(resSeed));
                        }
                        if (resSeed == targetSeed) {
                            goodFrames.add(curFrameCount + i);
                            //System.out.println(curFrameCount + " " + i + " " + nth_inv(curSeed) + " " + nth_inv(resSeed));
                        } else if (resSeed == targetM1 || resSeed == targetM2) {
                            latestEarlyFrame = Math.max(latestEarlyFrame, curFrameCount + i);
                        } else if (resSeed == targetP1 || resSeed == targetP2) {
                            earliestLateFrame = Math.min(earliestLateFrame, curFrameCount + i);
                        }
                    }
                }

                curFrameCount += framesBeforeAdvance;
                curSeed = next_seed(curSeed);
                framesBeforeAdvance = seed_duration(curSeed);
                dist -= 1;
            }
        }
        else { // too far away
            frame_window = 0;
            return 0;
        }

        // no good frames
        if (goodFrames.size() == 0) {
            frame_window = Math.min(0, earliestLateFrame-latestEarlyFrame);
            return (earliestLateFrame + latestEarlyFrame) / 2;
        }

        // check for the longest contiguous block of good frames
        int bestStartOfBlock = 0;
        int bestLengthOfBlock = 0;
        int curStartOfBlock = 0;
        for (int i = 0; i < goodFrames.size(); i++) {
            if (i > 0 && goodFrames.get(i) == goodFrames.get(i-1) + 1) {
                // block is still good
            } else {
                curStartOfBlock = i;
            }

            if (i - curStartOfBlock + 1 > bestLengthOfBlock) {
                bestLengthOfBlock = i - curStartOfBlock + 1;
                bestStartOfBlock = curStartOfBlock;
            }
        }

        frame_window = bestLengthOfBlock;
        return goodFrames.get(bestStartOfBlock + bestLengthOfBlock/2);
    }

    // returns the seed from pressing A for this cave
    // with this current seed, where the current seed will advance in framesBeforeAdvance frames
    long seed_from_A(int cave, long curSeed, long framesBeforeAdvance) {
        long framesLeft = frames_A_to_enter(cave);

        curSeed = next_seed(curSeed, 1000); // A press advances seed by 1000

        while (framesBeforeAdvance < framesLeft) {
            framesLeft -= framesBeforeAdvance;
            curSeed = next_seed(curSeed);
            framesBeforeAdvance = seed_duration(curSeed);
        }

        return curSeed;
    }

    // frames between pressing don't save and the first rng advance.
    int delay_between_dont_save_and_first_advance = 61;

    // frames between pressing A and entering the cave (when the seed can no longer advance)
    // warning, these numbers are only a rough estimation, they could be off by quite a bit...
    int[] frames_by_cave = {-1,76,76,76,84,58,56,88,68,78,82,65,61,90,79,79,77,82,80,81,96,86,79,9,80,80,64,92,97,89,85};
    int frames_A_to_enter(int cave) {
        return 8 + frames_by_cave[cave];
    }
    
    // -------------------- Functional Seed Code ------------------------------

	final long A = 0x41c64e6d;
	final long C = 0x3039;
	final long M = 0x100000000L;
    
    long next_seed(long seed) {
		return (A*seed+C) % M;
    }
    
    // digit you see in challenge result screen mode for this seed
    int digit_observation(long seed) {
		return (int)((seed >> 16) & 0x7fff) * 9 / 32768 + 1;
    }
    
    // number of frames until next advance on the challenge mode menu/result screen
    int seed_duration(long seed) {
        return 16 + (int)(( (int)((seed >> 16) & 0x7fff)/32768.0 * 0.9 + 0.1) * 300);
    }

    // number of frames of the first advance while entering the challenge mode menu screen
    int seed_duration_first(long seed) {
        return (int)(( (int)((seed >> 16) & 0x7fff)/32768.0) * 300);
    }
	
	long a_inv = inverse(A,M);
	long prev_seed(long seed) {
		return ((seed-C) * a_inv) % M;
    }
    
    long next_seed(long seed, long n) {
        long idx = nth_inv(seed);
        idx += n;
        if (idx < 0) idx += M * (1-idx/M);
        return nth(idx);
    }

    float[] seed_to_vel_vector(int seed, int len) {
        float vs[] = new float[len];
        for (int j = 0; j < len; j++) {
            seed = seed * 0x41c64e6d + 0x3039;
            int ret = (seed >>> 0x10) & 0x7fff;
            vs[j] = ret * 5.0f / 32768.0f;
        }
        return vs;
    }

    int clamp(long seed) { // clamp to [0, 2^31)
        return (int)((seed + M * (1-seed/M)) % (M/2));
    }

    // Compute the nth seed in O(log(M)) time
    private long r1_ = (C * inverse((A - 1)/4, M)) % M;
	long nth(long n) {
		n = n % M;
		long r2 = power(A,n,4*M)/4;
		return (r1_ * r2) % (M/2);
    }
    
    // The 2-adic valuation of x
	// aka the largest integer v such that 2^v divides x
	int v2(long x) {
		return x == 0 ? Integer.MAX_VALUE : Long.numberOfTrailingZeros(x);
	}
	
	// find the value of n such that the nth seed is x
	// Technique is based on Mihai's lemma / lifting the exponent
	long nth_inv(long x) {
        x = x % (M/2);
		long xpow = (x * (A-1) * inverse(C,M) + 1) % (4*M);
		long n=0, p=1;
		for (int i = 0; i < 32; i++) {
			if ( v2(power(A, n+p, 4*M) - xpow) > v2(power(A, n, 4*M) - xpow) )
				n += p;
			p *= 2;
		}
		return n % (M/2);
	}
	
	BigInteger theta(long num) {
		if (num % 4 == 3) {
            num = 4*M - num;
        }
        BigInteger xhat = BigInteger.valueOf(num);
        xhat = xhat.modPow(BigInteger.ONE.shiftLeft(32+1), BigInteger.ONE.shiftLeft(2*32+3));
        xhat = xhat.subtract(BigInteger.ONE);
        xhat = xhat.divide(BigInteger.ONE.shiftLeft(32+3));
        xhat = xhat.mod(BigInteger.ONE.shiftLeft(32));
        return xhat;
	}
	
	// find the value of n such that the nth seed is x
	long nth_inv2(long x) {
		BigInteger thetaAInverse = BigInteger.valueOf(2755579993L); // inverse(theta(A), M)
		long xpow = (x * (A-1) * inverse(C,M) + 1) % (4*M);
		return thetaAInverse.multiply(theta(xpow)).mod(bigInt(M)).longValue();
    }
    
    // find the value of n such that the nth seed is x
	// the runtime and memory is O(sqrt(M))
	HashMap<Long,Long> table = null;
	long nth_inv3(long x) {
		x = x % M;
		long m = (long)(Math.sqrt(M));
		if (table == null) {
			table = new HashMap<Long, Long>();
			for (long i = 0; i < M; i += m) {
				table.put(nth(i), i);
			}
		}
		long r = x;
		for (long i = 0; i < m; i++) {
			if (table.containsKey(r)) {
				return (table.get(r) + M - i) % M;
			}
			r = (A * r + C) % M;
		}
		return -1;
	}
	
	// distance from a1 to a2 (i.e. how many advances from a1 to a2)
	long dist(long a1, long a2) {
        long x = nth_inv(a2) - nth_inv(a1);
        if (x < 0) x += (1-x/M) * M;
		return x % M;
	}
	
	long power(long b, long e, long m) { 
		return bigInt(b).modPow(bigInt(e), bigInt(m)).longValue();
    } 
	
	long inverse(long a, long m) {
		return bigInt(a).modInverse(bigInt(m)).longValue();
	}
	
	long gcd(long a, long b) {
		return bigInt(a).gcd(bigInt(b)).longValue();
	}
	
	BigInteger bigInt(long l) {
		return BigInteger.valueOf(l);
	}
	
	String seed_to_sequence(long seed, int length) {
		String s = "";
		for (int i = 0; i < length; i++) {
			s += digit_observation(seed);
			seed = next_seed(seed);
		}
		return s;
    }
    
    // Pre-computed LLL matrices in the 10 length case.
	long[] lattice_P = {0L, 12345L, 1406932606L, 654583775L, 1449466924L, 229283573L, 1109335178L, 1051550459L, 1293799192L, 794471793L};
	long[][] lattice_LLL = {
			{7285528L, -83449544L, 45423832L, -50915336L, 5424280L, 11797688L, 73798232L, 18653048L, 9860632L, 110860344L},
			{-106217756L, 57360148L, -10882172L, -40442060L, -102086364L, 14818388L, 31434692L, -152033676L, 28292964L, -49705580L},
			{34283127L, 151927467L, -63892273L, -61411805L, -6025497L, 51296859L, 124719807L, 12266835L, -122448297L, -91257589L},
			{52786254L, 45500726L, 128950270L, 83526438L, 134441774L, 129017494L, 117219806L, 43421574L, 24768526L, 14907894L},
			{-167644423L, -59616763L, 109605409L, 23373325L, -41899639L, 208006485L, -93064399L, 6126045L, 14432537L, -109056603L},
			{-57263749L, 26997599L, 235377011L, -109316105L, -10496469L, -11449777L, 39499171L, 50921575L, -6386213L, -132308929L},
			{12218042L, -84291278L, -121156022L, -34414206L, 229351514L, -58019246L, 114223338L, 817058L, -82949126L, -40336014L},
			{61125926L, -38357714L, 26421398L, -66454562L, 112320902L, 1563150L, 57416694L, -70198338L, 232092646L, -149525266L},
			{-80092662L, -130004414L, -83117542L, 13510930L, -29951830L, -89674654L, 95830458L, 108832818L, 117365066L, -101511038L},
			{-6443794L, 250029398L, -61924962L, -173011898L, 64392654L, -52222794L, -55962242L, 74156710L, 141335726L, 177191446L}	
	};
	long[][] lattice_LLLinv = {
			{3L, -8L, 5L, -3L, -3L, -2L, -5L, 6L, -4L, -3L},
			{-7L, 3L, 1L, 4L, -2L, 1L, -1L, -2L, 0L, 3L},
			{0L, 1L, -4L, 3L, -2L, 6L, -1L, -1L, -2L, -1L},
			{-9L, 3L, -5L, 8L, -3L, -2L, -1L, -4L, 3L, -2L},
			{-4L, 0L, -4L, 3L, 1L, 1L, 7L, 0L, -2L, 2L},
			{6L, -3L, 6L, 0L, 7L, -5L, -2L, 3L, -2L, 0L},
			{5L, 5L, 3L, 5L, -3L, 0L, 0L, -1L, 4L, -1L},
			{0L, -8L, 3L, 0L, 2L, 1L, -2L, -2L, 5L, 2L},
			{0L, 1L, -2L, 2L, 0L, -2L, -3L, 4L, 3L, 2L},
			{6L, 2L, -3L, 2L, -1L, -2L, 0L, -3L, -1L, 2L}
	};

	// Take an ordered sequence of digit observations and output the potential seeds
	// length of sequence should be 10 or more, ideally, to avoid collisions
	// This is based on Matthew's implementation of lattice reduction using the LLL algorithm.
	ArrayList<Long> sequence_to_seed(String sequence) {
		
		// convert the sequence to an array.
		long[] seq = new long[sequence.length()];
		for (int i = 0; i < sequence.length(); i++) {
			seq[i] = sequence.charAt(i) - '0';
		}
		
		// find the upper and lower bounds for the seed
		// for each part of the sequence
		int N = 10;
		long[] LowerBounds = new long[N];
		long[] UpperBounds = new long[N];
		for (int i = 0; i < N; i++) {
			if (i < seq.length) {
				LowerBounds[i] = (seq[i]-1) * 0x10000 * (32768/9 + 1);
				UpperBounds[i] = seq[i] * 0x10000 * (32768/9 + 1);
			} else {
				LowerBounds[i] = 0;
				UpperBounds[i] = Integer.MAX_VALUE;
			}
		}
		
		// find upper and lower bounds in the LLL basis representation
		// this is the crucial step which reduces the number of vectors we must check
		double[] minD = new double[N];
		double[] maxD = new double[N];
		double M2_31 = M / 2;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (lattice_LLLinv[i][j] < 0) {
					minD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					maxD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				} else {
					maxD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					minD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				}
			}
		}
		// clamp these to integer values
		long[] min = new long[N];
		long[] max = new long[N];
		for (int i = 0; i < N; i++) {
			min[i] = (long)Math.ceil(minD[i]);
			max[i] = (long)Math.floor(maxD[i]);
		}
		
		// v is the vector of seeds that we are currently checking (initially min * LLL + P)
		// b is the vector v but in the basis representation (initially "min")
		long[] v = new long[N];
		long[] b = new long[N];
		for (int i = 0; i < N; i++) {
			b[i] = min[i];
			v[i] = lattice_P[i];
			for (int j = 0; j < N; j++)
				v[i] += min[j]*lattice_LLL[j][i];
		}

		// iterate over the set of possibilities contained in min/max. (we're checking all of them)
		ArrayList<Long> candidates = new ArrayList<Long>();		
		while(true) {

			// check if the current v vector falls in the desired region
			boolean isInRegion = true;
			for (int i = 0; i < N; i++) {
		        if (v[i] < LowerBounds[i] || v[i] > UpperBounds[i]) {
		            isInRegion = false;
		            break;
		        }
			}
			if (isInRegion) {
				candidates.add((v[0] & 0x7fffffff));
			}
			
			// move to the next b/v vector.
			boolean done = true;
			for (int i = 0; i < N; i++) {
		        b[i] += 1;
		        for (int j = 0; j < N; j++)
		        	v[j] += lattice_LLL[i][j];
		        if (b[i] > max[i]) {
		            b[i] = min[i];
		            for (int j = 0; j < v.length; j++)
		            	v[j] -= lattice_LLL[i][j]*(max[i]-min[i]+1);
		        }
		        else {
		        	done = false;
		            break;
		        }
			}
			if (done)
				break;
		}
		
		// Verify the candidates and return the ones that match
		ArrayList<Long> ret = new ArrayList<Long>();
		for (int i = 0; i < candidates.size(); i++) {
			if (seed_to_sequence(candidates.get(i), sequence.length()).equals(sequence))
				ret.add(candidates.get(i));
		}
		
		return ret;
    }
    
    ArrayList<Float> out_disutil_for_vs_array;
    ArrayList<Long> vs_array_to_seed(float[] vs, boolean[] is_space, float tol) {
		
		// find the upper and lower bounds for the seed
		// for each part of the sequence
		int N = 10;
		long[] LowerBounds = new long[N];
		long[] UpperBounds = new long[N];
		for (int i = 0; i < N; i++) {
			if (i < vs.length && !is_space[i]) {
                float lb = Math.max(0, vs[i] - tol);
                float ub = Math.min(5, vs[i] + tol);
				LowerBounds[i] = Math.max(0, Math.round(lb/5.0f * 0x80000000l));
				UpperBounds[i] = Math.min(Integer.MAX_VALUE, Math.round(ub/5.0f * 0x80000000l));
			} else {
				LowerBounds[i] = 0;
				UpperBounds[i] = Integer.MAX_VALUE;
			}
        }
        
        //System.out.println(Arrays.toString(LowerBounds));
        //System.out.println(Arrays.toString(UpperBounds));
		
		// find upper and lower bounds in the LLL basis representation
		// this is the crucial step which reduces the number of vectors we must check
		double[] minD = new double[N];
		double[] maxD = new double[N];
		double M2_31 = M / 2;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (lattice_LLLinv[i][j] < 0) {
					minD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					maxD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				} else {
					maxD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					minD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				}
			}
		}
		// clamp these to integer values
		long[] min = new long[N];
		long[] max = new long[N];
		for (int i = 0; i < N; i++) {
			min[i] = (long)Math.ceil(minD[i]);
			max[i] = (long)Math.floor(maxD[i]);
        }
        
        //System.out.println(Arrays.toString(min));
        //System.out.println(Arrays.toString(max) + "\n");
		
		// v is the vector of seeds that we are currently checking (initially min * LLL + P)
		// b is the vector v but in the basis representation (initially "min")
		long[] v = new long[N];
		long[] b = new long[N];
		for (int i = 0; i < N; i++) {
			b[i] = min[i];
			v[i] = lattice_P[i];
			for (int j = 0; j < N; j++)
				v[i] += min[j]*lattice_LLL[j][i];
		}

		// iterate over the set of possibilities contained in min/max. (we're checking all of them)
        ArrayList<Long> candidates = new ArrayList<Long>();		
        ArrayList<Long> ret = new ArrayList<Long>();
        int num_attempts = 0;

		while(true) {
            num_attempts += 1;
            if (num_attempts > 130000000) {
                System.out.println("lattice timeout");
                break;
            }

			// check if the current v vector falls in the desired region
			boolean isInRegion = true;
			for (int i = 0; i < N; i++) {
		        if (v[i] < LowerBounds[i] || v[i] > UpperBounds[i]) {
		            isInRegion = false;
		            break;
		        }
			}
			if (isInRegion) {
				candidates.add((v[0] & 0x7fffffff));
			}
			
			// move to the next b/v vector.
			boolean done = true;
			for (int i = 0; i < N; i++) {
		        b[i] += 1;
		        for (int j = 0; j < N; j++)
		        	v[j] += lattice_LLL[i][j];
		        if (b[i] > max[i]) {
		            b[i] = min[i];
		            for (int j = 0; j < v.length; j++)
		            	v[j] -= lattice_LLL[i][j]*(max[i]-min[i]+1);
		        }
		        else {
		        	done = false;
		            break;
		        }
			}
			if (done)
				break;
        }
		
		// Verify the candidates and return the ones that match
        out_disutil_for_vs_array = new ArrayList<Float>();
		for (int i = 0; i < candidates.size(); i++) {
            Long s = candidates.get(i);
            s = next_seed(s, -1);
            float[] vout = seed_to_vel_vector(clamp(s), vs.length);
            //System.out.println("candidate: " + Drawer.seedToString(s) + " " + Arrays.toString(vout));

            float disutil = 0;
            float worst = 0;
            boolean good = true;
			for (int j = 0; j < vs.length; j++) {
                if (is_space[j]) continue;
                disutil += Math.abs(vout[j]-vs[j]);
                if (Math.abs(vout[j]-vs[j]) > tol) good = false;
                worst = Math.max(worst, Math.abs(vout[j]-vs[j]));
            }

            if (good) {
                ret.add((long)clamp(s));
                out_disutil_for_vs_array.add(disutil);
            }
            //System.out.println("worst " + worst + " dis " + disutil);
		}
		
		return ret;
	}
	
        
    // ---------- slow versions -----------

    long nth_slow(long n) {
		long ret = 0;
		for (long i = 0; i < n; i++) {
			ret = (A * ret + C) % M;
		}
		return ret;
	}
	
	long nth_inv_slow(long a) {
		a = a % M;
		long cnt = 0;
		long r = 0;
		while (r != a) {
			r = (A * r + C) % M;
			cnt++;
		}
		return cnt;
    }
    
	long dist_slow(long a1, long a2) {
		return (nth_inv_slow(a2) - nth_inv_slow(a1) + M) % M;
	}
	
	ArrayList<Integer> sequence_to_seed_slow(String sequence) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		long[] seq = new long[sequence.length()];
		for (int i = 0; i < sequence.length(); i++) {
			seq[i] = sequence.charAt(i) - '0';
        }
        
		outer:
		for (int i = 0; i >= 0; i++) { // i from 0 to 2^31 - 1
			long seed = i;
			for (int j = 0; j < seq.length; j++) {
				if (digit_observation(seed) != seq[j])
					continue outer;
				seed = (A*seed+C) % M;
			}
			ret.add(i);
		}
		
		return ret;
    }
    
    long frames_between_seeds_slow(long start, long end) {
        long frames = seed_duration_first(start);
        long seed = next_seed(start);
        while (seed != end) {
            frames += seed_duration(seed);
            seed = next_seed(seed);
        }
        return frames;
    }

    long best_timing_slow(int cave, long startSeed, long targetSeed) {
 
        long curFrameCount = 0;
        ArrayList<Long> goodFrames = new ArrayList<Long>();
        long latestEarlyFrame = -1;
        long earliestLateFrame = Long.MAX_VALUE;

        long targetM1 = next_seed(targetSeed, -1);
        long targetM2 = next_seed(targetSeed, -2);
        long targetM999 = next_seed(targetSeed, -999);
        long targetM1000 = next_seed(targetSeed, -1000);
        long targetP1 = next_seed(targetSeed, 1);
        long targetP2 = next_seed(targetSeed, 2);

        long curSeed = startSeed;
        long framesBeforeAdvance = delay_between_dont_save_and_first_advance + seed_duration_first(startSeed);
        long framesOnThisSeed = 0;
        long dist = dist(curSeed, targetM1000);

        while (true) {

            if (dist <= 3) { // close (check frame by frame)

                long resSeed = seed_from_A(cave, curSeed, framesBeforeAdvance - framesOnThisSeed);
                if (resSeed == targetSeed) {
                    goodFrames.add(curFrameCount);
                    //System.out.println((curFrameCount-framesOnThisSeed) + " " + framesOnThisSeed + " " + nth_inv(curSeed) + " " + nth_inv(resSeed));
                } else if (resSeed == targetM1 || resSeed == targetM2) {
                    latestEarlyFrame = Math.max(latestEarlyFrame, curFrameCount);
                } else if (resSeed == targetP1 || resSeed == targetP2) {
                    earliestLateFrame = Math.min(earliestLateFrame, curFrameCount);
                }

                curFrameCount += 1;
                framesOnThisSeed += 1;

                if (framesOnThisSeed >= framesBeforeAdvance) {
                    framesOnThisSeed = 0;
                    curSeed = next_seed(curSeed);
                    framesBeforeAdvance = seed_duration(curSeed);
                    dist = dist(curSeed, targetM1000);
                }
            } else if (curSeed == targetM999 || dist > 1000001005) {
                break;
            } else { // far away
                curFrameCount += framesBeforeAdvance;
                curSeed = next_seed(curSeed);
                framesBeforeAdvance = seed_duration(curSeed);
                dist = dist(curSeed, targetM1000);
            }
        }

        // no good frames
        if (goodFrames.size() == 0) {
            frame_window = Math.min(0, earliestLateFrame-latestEarlyFrame);
            return (earliestLateFrame + latestEarlyFrame) / 2;
        }

        // check for the longest contiguous block of good frames
        int bestStartOfBlock = 0;
        int bestLengthOfBlock = 0;
        int curStartOfBlock = 0;
        for (int i = 0; i < goodFrames.size(); i++) {
            if (i > 0 && goodFrames.get(i) == goodFrames.get(i-1) + 1) {
                // block is still good
            } else {
                curStartOfBlock = i;
            }

            if (i - curStartOfBlock + 1 > bestLengthOfBlock) {
                bestLengthOfBlock = i - curStartOfBlock + 1;
                bestStartOfBlock = curStartOfBlock;
            }
        }

        frame_window = bestLengthOfBlock;
        return goodFrames.get(bestStartOfBlock + bestLengthOfBlock/2);
    }
    
}

