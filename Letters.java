import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Letters {

    int nearbySearchDist = 1000000;

    Seed seedCalc;
    Letters(Seed s) {
        seedCalc = s;
    }

    final JFrame jfr = new JFrame("Letters");
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

    void letterSim() {
        //searchForLowDisutilNearGenerous(0x70CDA723, new float[]{4.67f, 2.51f, 2.47f, 3.14f, 1.67f, 1.63f, 2.02f,
        //     1.82f, 2.81f, 0.60f, -0.00f, 3.77f, 0.00f, 4.26f, 2.96f, 3.73f, 1.50f, 0.48f, 4.17f, 2.66f}
        //, new boolean[]{false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
        // 0.5f, 1000000);
        //if (1>0) return;

        // Set up the LEtters UI
        jfr.getContentPane().setLayout(null);
		jfr.setSize(410, 660);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        jtext.setText("letters: ");
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
        
        jfr.revalidate();
		jfr.repaint();
        jfr.setVisible(true);

        Graphics G = jfr.getGraphics();

        
        for (int iter = 0; iter < 10; iter++) {

            String s = "Snagret Hole";
            int n = s.length();

            float[] delay = new float[n];
            float[] ypos = new float[n];
            float[] yvel = new float[n];

            int initialSeed = 0x732368c0;
            seed = initialSeed;

            for (int i = 0; i < n; i++) {
                delay[i] = (float)i / n;
                ypos[i] = 200.0f;
                yvel[i] = randFloat() * 5.0f;
            }

            System.out.println(velString(yvel));

            for (int j = 0; j < 30*5; j++) {

                jtext2.setText(""+j);

                for (int i = 0; i < n; i++) {
                    if (delay[i] <= 0) {
                        yvel[i] -= 1.0f;
                        ypos[i] += yvel[i];

                        if (ypos[i] < 0) {
                            //if (i==0 && yvel[i] < -10)
                            //    System.out.println(j);
                            yvel[i] *= -0.3f;
                            ypos[i] = yvel[i];
                        }
                    } else {
                        delay[i] -= 1.0f/30;
                    }

                    G.drawOval(10+10*i, 300-(int)ypos[i], 7, 7);
                }

                try {
                    Thread.sleep(1000/15);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                G.clearRect(0, 0, 410, 600);
            }
        } 
    }

    long letters(String info, long seedLastRead) {

        // process a string from python code
        //String info = "lettersinfo,720,12,250,245,250,252,248,248,249,-1,249,246,249,248;;;0,17,;0,34,;0,51,;0,70,;1,19,3,18,;1,35,2,14,3,33,;1,53,2,29,3,49,;1,73,2,46,3,67,;2,64,4,20,;2,84,4,35,;4,53,;4,71,;;;5,8,;5,24,;5,41,;5,59,6,12,;5,80,6,28,8,20,;6,45,8,35,;6,64,8,52,9,16,;6,84,8,70,9,31,;9,48,;9,65,;10,12,11,11,;10,27,11,26,;10,43,11,42,;10,61,11,59,;10,81,11,79,;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
        //String info = "lettersinfo,720,12,250,245,250,252,248,248,249,-1,249,246,250,248;;;;;;;;0,14,;0,35,;0,51,;0,70,;1,20,;1,36,;1,54,;1,73,2,13,3,19,;2,29,3,33,;2,45,3,50,;2,63,3,68,;2,83,4,19,;4,34,5,8,;4,51,5,23,;4,70,5,39,;5,56,;5,75,;;;;6,21,8,9,;6,37,8,23,;6,55,8,39,;6,75,8,56,;8,75,10,14,;10,29,;10,45,;9,8,10,62,;9,24,10,82,;9,42,;9,60,;9,80,;11,10,;11,25,;11,42,;11,61,;11,81,;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
        
        String[] info1 = info.split(";");
        String[] info10 = info1[0].split(",");
        int height = Integer.parseInt(info10[2]);
        int num_chars = Integer.parseInt(info10[3]);
        out_num_chars = num_chars;
        out_cave = info10[1].replace("_", " ");
        System.out.println(out_cave + " h=" + height + " n=" + num_chars);

        if (out_cave.equals("")) {
            System.out.println("bad cave. :(");
            return -1;
        }

        // pull the letter locations from the python code
        int[][] locs = new int[num_chars][300];
        int[] offsets = new int[num_chars];
        int num_char_readings = 0;
        for (int i = 0; i < num_chars; i++)
            offsets[i] = Integer.parseInt(info10[i+4]);

        for (int i = 0; i < 300; i++) {
            if (info1.length <= i+1) break;
            String[] info20 = info1[i+1].split(",");
            if (info20.length < 1) continue;

            for (int j = 0; j < info20.length-1; j += 2) {
                int ch = Integer.parseInt(info20[j]);
                int lc = Integer.parseInt(info20[j+1]);
                locs[ch][i] = lc+1;
                num_char_readings += 1;
            }
        }

        if (num_char_readings < 2) {
            System.out.println("too few char readings :(");
            return -1;
        }

        // shift the locs by delay time and adjust for the height of each char
        int[][] shifted_locs = new int[num_chars][400];
        int minc = 401, maxc = -1;
        for (int i = 0; i < num_chars; i++) {
            float delay_time = ((float)i) / num_chars;
            int frames_of_delay = 0;
            while (delay_time > 0) {
                delay_time -= 1.0f/30;
                frames_of_delay += 1;
            }
            for (int j = 0; j < 400; j++) {
                int c = j - 50 + frames_of_delay;
                if (c >= 0 && c < 300) {
                    shifted_locs[i][j] = locs[i][c];
                    if (shifted_locs[i][j] > 0) {
                        shifted_locs[i][j] += (offsets[i] > 0 ? offsets[i] : 0);
                        minc = Math.min(minc,j);
                        maxc = Math.max(maxc,j);
                    }
                }
            }
        }

        int[][] clamped_locs = new int[num_chars][Math.max(1,maxc-minc+1)];
        for (int i = 0; i < num_chars; i++) {
            for (int j = 0; j < clamped_locs[0].length; j++) {
                clamped_locs[i][j] = shifted_locs[i][j+minc];
            }
            System.out.println(Arrays.toString(clamped_locs[i]));
            //for (int j = 0; j < clamped_locs[0].length-1; j++) {
            //    System.out.print(" " + (clamped_locs[i][j+1]-clamped_locs[i][j]));
            //}
            //System.out.println();
        }

        // compute when we crossed y=h*6/100
        int cross_point = height*6/100;
        System.out.println("Cross point:" + cross_point);
        float[] cross_points = new float[num_chars]; // in frames
        boolean[] is_space = new boolean[num_chars];
        int min_cross_idx = -1, max_cross_idx = -1;
        float min_cross = 1000, max_cross = -1;
        for (int i = 0; i < num_chars; i++) {
            is_space[i] = true;
            for (int j = 0; j < clamped_locs[0].length; j++) {
                if (clamped_locs[i][j] > cross_point && j > 0) {
                    cross_points[i] = (j-1) + (cross_point*1.0f-clamped_locs[i][j-1]) / (clamped_locs[i][j] - clamped_locs[i][j-1]);
                    is_space[i] = false;
                    if (cross_points[i] < min_cross) {
                        min_cross = cross_points[i];
                        min_cross_idx = i;
                    }
                    if (cross_points[i] > max_cross) {
                        max_cross = cross_points[i];
                        max_cross_idx = i;
                    }
                    break;
                }
            }
            if (out_cave.charAt(i) == '\'') {
                is_space[i] = true;
            } 
        }
        System.out.println("cross frames " + velString(cross_points));

        // number between 0 and 200 corresponding to cross_point.
        // assumes 0 = 35/100 * h and 135 = 5/720 * h
        float cross_point_pos = (6.0f/100 - 35.0f/100) / (5.0f/720 - 35.0f/100) * 135;
        //System.out.println(cross_point_pos);

        // assume the min_cross_idx is 0. then, what are the initial velocities?
        float min_vels[] = new float[num_chars], max_vels[] = new float[num_chars];
        float f_0 = f_in_terms_of_v_and_pos(0, cross_point_pos);
        float f_5 = f_in_terms_of_v_and_pos(5, cross_point_pos);
        //System.out.println(f_0);
        //System.out.println(f_5);
        for (int i = 0; i < num_chars; i++) {
            if (is_space[i]) continue;
            min_vels[i] = v_in_terms_of_f_and_pos(f_0 + cross_points[i] - cross_points[min_cross_idx], cross_point_pos);
            max_vels[i] = v_in_terms_of_f_and_pos(f_5 + cross_points[i] - cross_points[max_cross_idx], cross_point_pos);
        }

        System.out.println("min " + velString(min_vels));
        System.out.println("max " + velString(max_vels));


        float tolerance = num_chars >= 10 ? 0.2f : 0.15f;
        float range = max_vels[0] - min_vels[0];

        long bestSeed = -1;
        float bestDisutil = 100000;

        // try faster method first...
        if (seedLastRead != -1 && bestSeed == -1) {
            System.out.println("starting nearby search " + nearbySearchDist);
            bestSeed = searchForLowDisutilNear(seedLastRead, min_vels, is_space, 0.5f, nearbySearchDist);
            System.out.println("nearby serach done");
            if (bestSeed != -1) {
                System.out.println("found via close seeds");
                bestDisutil = out_disutil_for_near;
            }
        }

        if (bestSeed == -1) {
            System.out.println("starting lattice search");
            for (float offs = -tolerance/2; offs <= range + tolerance/2; offs += tolerance / 4) {
                float[] vs = new float[num_chars];
                
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i]) continue;
                    vs[i] = offs + min_vels[i];
                }

                // System.out.println("checking: " + velString(vs));

                ArrayList<Long> candidates = seedCalc.vs_array_to_seed(vs, is_space, tolerance);

                for (int i = 0; i < candidates.size(); i++) {
                    if (seedCalc.out_disutil_for_vs_array.get(i) < bestDisutil) {
                        bestDisutil = seedCalc.out_disutil_for_vs_array.get(i);
                        bestSeed = candidates.get(i);
                    }
                }

            }
            if (bestSeed != -1) {
                System.out.println("found via lattice search");
            }
            System.out.println("lattice search done");
        }

        // error correction via allowing one mistake
        if (seedLastRead != -1 && bestSeed == -1) {
            System.out.println("starting nearby generous search " + nearbySearchDist);
            bestSeed = searchForLowDisutilNearGenerous(seedLastRead, min_vels, is_space, 0.35f, nearbySearchDist);
            System.out.println("nearby generous serach done");
            if (bestSeed != -1) {
                System.out.println("found via close seeds generous");
                bestDisutil = out_disutil_for_near;
            }
        }

        

        System.out.println("bvs " + velString(seedCalc.seed_to_vel_vector(seedCalc.clamp(bestSeed), num_chars)));
        System.out.println("best: " + Drawer.seedToString(bestSeed) + " (disutil " + bestDisutil + ")" + " (n " + seedCalc.nth_inv(bestSeed) + ")");
        
        
        //System.exit(0);

        out_disutil = bestDisutil;

        /*out_cave = "";
        for (String s: Parser.fullNames) {
            if (s.length() == num_chars) {
                boolean good = true;
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i] ^ (s.charAt(i)==' ')) {
                        good = false;
                    }
                }
                if (good) {
                    out_cave = s;
                    break;
                }
            }
        }*/

        
        System.out.println("done checking");

        return bestSeed;
    }

    int out_num_chars = 0;
    String out_cave = "";
    float out_disutil = -1;

    int seed;
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


    float f_in_terms_of_v_and_pos(float v, float pos) {
        return ( (0.5f - v) - (float)(Math.sqrt((v-0.5f)*(v-0.5f) - 4 * (-0.5f) * (200-pos))) ) / (2 * (-0.5f));
    }
    
    float v_in_terms_of_f_and_pos(float f, float pos) {
        return (pos - 200 + f * (f+1) / 2) / f;
    }

    int lastSearchSeedForPrecompute = -1;
    float[] precomputeVs = new float[1];
    void precomputeExpectedFutureVs(long searchSeed, int len) {
        int seed = seedCalc.clamp(searchSeed);
        if (lastSearchSeedForPrecompute == seed)
            return;
        lastSearchSeedForPrecompute = seed;

        len += 50;
        System.out.println("precompute vs for " + Drawer.seedToString(searchSeed) + " " + len);
        
        precomputeVs = new float[len];
        for (int j = 0; j < len; j++) {
            seed = seed * 0x41c64e6d + 0x3039;
            int ret = (seed >>> 0x10) & 0x7fff;
            precomputeVs[j] = ret * 5.0f / 32768.0f;
        }
        System.out.println("done precompute vs");
        
    }

    float out_disutil_for_near;
    long searchForLowDisutilNear(long searchSeed, float[] vtarg, boolean[] is_space, float tolerance, int range) {

        long bestSeed = -1;
        float bestDisutil = 100000;

        float targsum = 0;
        int cnt = 0;
        for (int j = 0; j < vtarg.length; j++) {
            if (is_space[j]) continue;
            targsum += vtarg[j];
            cnt++;
        }

        precomputeExpectedFutureVs(searchSeed, range);
        int seed = seedCalc.clamp(searchSeed);

        for (int i = 0; i < range; i++) {

            float vssum = 0;
            for (int j = 0; j < vtarg.length; j++) {
                if (is_space[j]) continue;
                vssum += precomputeVs[j+i];
            }
            float adjustment = (targsum-vssum)/cnt;

            float disutil = 0;
            boolean good = true;
            for (int j = 0; j < vtarg.length; j++) {
                if (is_space[j]) continue;
                float dis = Math.abs(vtarg[j]-precomputeVs[j+i]-adjustment);
                if (dis > tolerance) {
                    good = false;
                    break;
                }
                disutil += dis;
            }

            if (good) {
                //System.out.println(disutil + " " + Drawer.seedToString(check) + " " + velString(vs));
                if (disutil < bestDisutil) {
                    bestDisutil = disutil;
                    bestSeed = seed;
                }
                if (bestDisutil <= 0.1*cnt) {
                    break; //good enough
                }
            }
            
            seed = seed * 0x41c64e6d + 0x3039;

        }
        out_disutil_for_near = bestDisutil;
        return bestSeed;
    
    }


    // allows a single char to be discarded.
    long searchForLowDisutilNearGenerous(long searchSeed, float[] vtarg, boolean[] is_space, float tolerance, int range) {

        long bestSeed = -1;
        float bestDisutil = 100000;

        float targsum = 0;
        int cnt = 0;
        for (int j = 0; j < vtarg.length; j++) {
            if (is_space[j]) continue;
            targsum += vtarg[j];
            cnt++;
        }

        precomputeExpectedFutureVs(searchSeed, range);
        int seed = seedCalc.clamp(searchSeed);

        for (int i = 0; i < range; i++) {

            float vssum = 0;
            for (int j = 0; j < vtarg.length; j++) {
                if (is_space[j]) continue;
                vssum += precomputeVs[j+i];
            }
            float adjustment = (targsum-vssum)/cnt;

            float disutil = 0;
            boolean good = true;
            int numBad = 0;
            for (int j = 0; j < vtarg.length; j++) {
                if (is_space[j]) continue;
                float dis = Math.abs(vtarg[j]-precomputeVs[j+i]-adjustment);
                if (dis > tolerance) {
                    numBad += 1;
                    if (numBad >= 2) {
                        good = false;
                        break;
                    }
                    disutil += tolerance;
                } else disutil += dis;
            }

            if (good) {
                //System.out.println(disutil + " " + Drawer.seedToString(check) + " " + velString(vs));
                if (disutil < bestDisutil) {
                    bestDisutil = disutil;
                    bestSeed = seed;
                }
                if (bestDisutil <= 0.1*cnt) {
                    break; //good enough
                }
            }
            
            seed = seed * 0x41c64e6d + 0x3039;

        }
        out_disutil_for_near = bestDisutil;
        return bestSeed;
    
    }

    static String velString(float[] v) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i= 0; i < v.length; i++) {
            if (i > 0) s.append(", ");
            s.append(String.format("%.2f",v[i]));
        }
        s.append("]");
        return s.toString();
    }
    static String velString(double[] v) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i= 0; i < v.length; i++) {
            if (i > 0) s.append(", ");
            s.append(String.format("%.2f",v[i]));
        }
        s.append("]");
        return s.toString();
    }

}
