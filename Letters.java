import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Letters {

    int nearbySearchDist = 600000;
    int nearbySearchDistLong = 12000000;
    long nearbySearchLongSeed = -1;

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
        out_cave = info10[1];
        System.out.println(out_cave + " height:" + height + " num:" + num_chars);

        if (out_cave.equals("")) {
            System.out.println("bad cave. :(");
            return -1;
        }

        int substr_mult = out_cave.length() / num_chars;

        // pull the letter locations from the python code
        float[][] locs = new float[num_chars][300];
        float[] offsets = new float[num_chars];
        int num_char_readings_frames = 0;
        for (int i = 0; i < num_chars; i++)
            offsets[i] = Integer.parseInt(info10[i+4]);

        for (int i = 0; i < 300; i++) {
            if (info1.length <= i+1) break;
            String[] info20 = info1[i+1].split(",");
            if (info20.length < 1) continue;

            for (int j = 0; j < info20.length-1; j += 2) {
                int ch = Integer.parseInt(info20[j]);
                float lc = Float.parseFloat(info20[j+1]);
                locs[ch][i] = lc;
                num_char_readings_frames += 1;
            }
        }

        if (num_char_readings_frames < 3) {
            System.out.println("too few char frame readings :(");
            return -1;
        }

        // shift the locs by delay time and adjust for the height of each char
        float[][] shifted_locs = new float[num_chars][400];
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

        float[][] clamped_locs = new float[num_chars][Math.max(1,maxc-minc+1)];
        for (int i = 0; i < num_chars; i++) {
            for (int j = 0; j < clamped_locs[0].length; j++) {
                clamped_locs[i][j] = shifted_locs[i][j+minc];
            }
            System.out.println(out_cave.substring(i*substr_mult,(i+1)*substr_mult) + " " + velString0(clamped_locs[i]));
            //for (int j = 0; j < clamped_locs[0].length-1; j++) {
            //    System.out.print(" " + (clamped_locs[i][j+1]-clamped_locs[i][j]));
            //}
            //System.out.println();
        }

        // compute when we crossed y=h*6/100
        // assumes bottom of the cave name text is at 249/720
        // assumes 0 = 249/720 * h and 164.65 = top of screen
        float cross_point = (int)(height*(Manip.thisManip.params.containsKey("crossPoint") ? Double.parseDouble(Manip.thisManip.params.get("crossPoint")) : 0.06)); // IMPORTANT PARAM...
        //float sublevel_start_ratio = 249.0f/720;
        float pos_for_top_of_screen = Manip.thisManip.params.containsKey("letterPosOffset") ? Float.parseFloat(Manip.thisManip.params.get("letterPosOffset")) : 164.0f;
        // number between 0 and 200 corresponding to cross_point.
        //float cross_point_pos = (sublevel_start_ratio - cross_point*1.0f/height) / sublevel_start_ratio * pos_for_top_of_screen;
        float cross_point_pos = pos_for_top_of_screen - cross_point*480.0f/height;


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

        int num_good_chars = 0;
        for (int i = 0; i < num_chars; i++)
            if (!is_space[i])
                num_good_chars += 1;
        
        System.out.println("cross_point:" + rnd(cross_point,1) + "  pos:" + rnd(cross_point_pos,1) + "  good:" + num_good_chars);
        System.out.println("cross_frames:" + velString(cross_points));
        //System.out.println(cross_point_pos);

        if (num_good_chars < 4) {
            System.out.println("too few good char readings :(");
            return -1;
        }

        // assume the min_cross_idx is 0. then, what are the initial velocities?
        float min_vels[] = new float[num_chars], max_vels[] = new float[num_chars], diff_vels[] = new float[num_chars];
        float f_0 = f_in_terms_of_v_and_pos(0, cross_point_pos);
        float f_5 = f_in_terms_of_v_and_pos(5, cross_point_pos);
        //System.out.println(f_0);
        //System.out.println(f_5);
        for (int i = 0; i < num_chars; i++) {
            if (is_space[i]) continue;
            min_vels[i] = v_in_terms_of_f_and_pos(f_0 + cross_points[i] - cross_points[min_cross_idx], cross_point_pos);
            max_vels[i] = v_in_terms_of_f_and_pos(f_5 + cross_points[i] - cross_points[max_cross_idx], cross_point_pos);
            diff_vels[i] = max_vels[i] - min_vels[i];
        }
        float range = max_vels[min_cross_idx] - min_vels[min_cross_idx];
        
        //System.out.println("diff " + velString(diff_vels));

        //float tolerance = num_chars >= 10 ? 0.2f : num_chars >= 8 ? 0.16f : 0.13f;
        //float toleranceLax = num_chars >= 10 ? 0.5f : num_chars >= 8 ? 0.35f : 0.25f;

        boolean useAbsolute = num_good_chars <= 7;

        long bestSeed = -1;
        float bestDisutil = 100000;
        float[] bestVs = new float[num_chars];

        if (useAbsolute) {

            // these tolerances are all radius terms
            float falsePositiveRate = num_good_chars >= 10 ? 0.995f : 0.95f;
            float toleranceNearby = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/nearbySearchDist), 1.0/(num_good_chars)));
            float toleranceAnchor = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/nearbySearchDistLong), 1.0/(num_good_chars)));
            float toleranceLattice = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/Math.pow(2,31)), 1.0/(num_good_chars)));
            toleranceNearby = Math.min(toleranceNearby, 0.2f);
            toleranceAnchor = Math.min(toleranceAnchor, 0.2f);
            toleranceLattice = Math.min(toleranceLattice, 0.2f);
            if (num_good_chars <= 4) toleranceNearby = 0.07f;
            System.out.println("tolerance:: near:" + String.format("%.2f",toleranceNearby) + " anchor:" 
            + String.format("%.2f",toleranceAnchor) + " lattice:" + String.format("%.2f",toleranceLattice));

            // generate vtarg array...
            float[][] vtarg = new float[5001][num_chars];
            float[] vtargsums = new float[vtarg.length];
            for (int i = 0; i < vtargsums.length; i++) vtargsums[i] = -1;

            float[][] possibleVs = new float[10][num_chars];
            int numPossible = 0;
            /*for (int crossFrameOffset = 0; crossFrameOffset < 30; crossFrameOffset++) {
                float[] vs = new float[num_chars];
                float min_v = 10, max_v = -5;
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i]) continue;

                    vs[i] = v_in_terms_of_f_and_pos(cross_points[i]+crossFrameOffset, cross_point_pos);
                    min_v = Math.min(min_v, vs[i]);
                    max_v = Math.max(max_v, vs[i]);
                }
                //System.out.println(min_v + " " + max_v);
                if (min_v < -3*toleranceNearby || max_v > 5+3*toleranceNearby) continue;

                System.out.println("possible" + numPossible + " " + velString(vs));
                possibleVs[numPossible] = vs;
                numPossible += 1;
            }*/

            for (int crossFrameOffset = 0; crossFrameOffset < 30; crossFrameOffset++) {
                float[] vs = new float[num_chars];
                float min_v = 10, max_v = -5;
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i]) continue;

                    ArrayList<Float> vel_measured = new ArrayList<Float>();
                    for (int q = 0; q < clamped_locs[i].length; q++) {
                        if (clamped_locs[i][q] > 0) {
                            //float posu = (sublevel_start_ratio - clamped_locs[i][q]*1.0f/height) / sublevel_start_ratio * pos_for_top_of_screen;
                            float posu = pos_for_top_of_screen - clamped_locs[i][q]*480.0f/height;
                            vel_measured.add(v_in_terms_of_f_and_pos(q+crossFrameOffset, posu));
                        }
                    }
                    if (vel_measured.size() == 0) continue; // should never happen
                    /*Collections.sort(vel_measured);
                    if (vel_measured.size() >= 5) {
                        vel_measured.remove(vel_measured.size()-1);
                        vel_measured.remove(0);
                    }*/
                    float velsum = 0;
                    for (float fff: vel_measured) velsum += fff;
                    velsum /= vel_measured.size();
                    vs[i] = velsum;
                    min_v = Math.min(min_v, vs[i]);
                    max_v = Math.max(max_v, vs[i]);
                }
                //System.out.println(min_v + " " + max_v);
                if (min_v < -3*toleranceNearby || max_v > 5+3*toleranceNearby) continue;

                System.out.println("possible" + (crossFrameOffset-8) + " " + velString(vs));
                possibleVs[numPossible] = vs;
                numPossible += 1;
            }

            if (numPossible == 0) {
                System.out.println("No possible velocities ??");
                return -1; // should never happen?
            }

            int possIdx = 0;
            for (int i = 0; i < vtarg.length; i++) {
                float sum = 0;
                for (int j = 0; j < num_chars; j++) {
                    if (is_space[j]) continue;
                    sum += possibleVs[possIdx][j];
                }
                sum /= num_good_chars;

                if (sum-toleranceNearby > i*5.0f/vtarg.length) {
                    continue;
                } else if (sum+toleranceNearby < i*5.0f/vtarg.length) {
                    if (possIdx+1 < numPossible) {
                        possIdx += 1;
                    } else {
                        break;
                    }
                } else {
                    for (int j = 0; j < num_chars; j++)
                        vtarg[i][j] = possibleVs[possIdx][j];
                    vtargsums[i] = sum;
                }
                //System.out.println(i + " " + velString(vtarg[i]) + " " + vtargsums[i]);
            }
            

            // try fasteest method first, using a recent nearby seed
            if (seedLastRead != -1 && bestSeed == -1) {
                System.out.println("starting absolute nearby search " + (precomputeVs.length-50));

                bestSeed = searchForLowDisutilNear(seedLastRead, vtarg, vtargsums, is_space, toleranceNearby, nearbySearchDist);
                System.out.println("absolute nearby search done");
                if (bestSeed != -1) {
                    System.out.println("found via absolute nearby search");
                    bestDisutil = out_disutil_for_near;
                    bestVs = out_vs_for_near;
                }
            }

            // next best method, an anchor seed has been set in challenge mode
            if (nearbySearchLongSeed != -1 && bestSeed == -1) {
                System.out.println("starting absolute anchor search " + (precomputeVsLong.length-50));

                bestSeed = searchForLowDisutilNearLong(nearbySearchLongSeed, vtarg, vtargsums, is_space, toleranceAnchor, nearbySearchDistLong);
                System.out.println("absolute anchor search done");
                if (bestSeed != -1) {
                    System.out.println("found via absolute anchor search");
                    bestDisutil = out_disutil_for_near;
                    bestVs = out_vs_for_near;
                }
            }

            if (bestSeed == -1 && num_good_chars >= 7 && !is_space[0] && !is_space[1] && !is_space[2] && !is_space[3]) {
                System.out.println("starting absolute lattice search");
                long startTime = System.currentTimeMillis();
                for (int ii = 0; ii < numPossible; ii++) { 
                    float[] vs = possibleVs[ii];

                    ArrayList<Long> candidates = seedCalc.vs_array_to_seed(vs, is_space, toleranceLattice);

                    for (int i = 0; i < candidates.size(); i++) {
                        if (seedCalc.out_disutil_for_vs_array.get(i) < bestDisutil) {
                            bestDisutil = seedCalc.out_disutil_for_vs_array.get(i);
                            bestSeed = candidates.get(i);
                            bestVs = vs;
                        }
                    }

                    if (System.currentTimeMillis() - startTime > 8000) {
                        System.out.println("lattice timeout 8000");
                        break;
                    }

                }
                System.out.println("absolute lattice search done");
                if (bestSeed != -1) {
                    System.out.println("found via absolute lattice search");
                }
            }

        } else {

            System.out.println("min " + velString(min_vels));
            System.out.println("max " + velString(max_vels));

            // these tolerances are all radius terms
            float falsePositiveRate = num_good_chars >= 10 ? 0.995f : 0.95f;
            float toleranceNearby = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/nearbySearchDist), 1.0/(num_good_chars-0.5)));
            float toleranceAnchor = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/nearbySearchDistLong), 1.0/(num_good_chars-0.5)));
            float toleranceLattice = (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/Math.pow(2,31)), 1.0/(num_good_chars-0.5)));
            float toleranceGenerous = 0.7f * (float)(5.0/2 * Math.pow(1 - Math.pow(falsePositiveRate, 1.0/nearbySearchDist), 1.0/(num_good_chars-1)));
            toleranceNearby = Math.min(toleranceNearby, 0.2f);
            toleranceAnchor = Math.min(toleranceAnchor, 0.2f);
            toleranceLattice = Math.min(toleranceLattice, 0.2f);
            toleranceGenerous = Math.min(toleranceGenerous, 0.2f);

            System.out.println("tolerance:: near:" + String.format("%.2f",toleranceNearby) + " anchor:" 
                + String.format("%.2f",toleranceAnchor) + " lattice:" + String.format("%.2f",toleranceLattice)
                + " generous:" + String.format("%.2f",toleranceGenerous));

            // generate vtarg array...
            float[][] vtarg = new float[5001][num_chars];
            float[] vtargsums = new float[vtarg.length];
            for (int i = 0; i < vtargsums.length; i++) vtargsums[i] = -1;
            for (float offs = -0.05f; offs <= range + 0.05f; offs += 0.00025f) {
                float foffs = f_in_terms_of_v_and_pos(offs, cross_point_pos);
                float[] vs = new float[num_chars];
                float sum = 0;
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i]) continue;
                    vs[i] = v_in_terms_of_f_and_pos(foffs + cross_points[i] - cross_points[min_cross_idx], cross_point_pos); //  offs + min_vels[i];
                    sum += vs[i];
                }
                sum /= num_good_chars;
                int idx = (int)(sum * (vtarg.length-1) / 5);
                float targetSumHere = (idx+0.5f) * 5 / (vtarg.length-1);
                if (Math.abs(sum - targetSumHere) < Math.abs(vtargsums[idx] - targetSumHere)) {
                    vtarg[idx] = vs;
                    vtargsums[idx] = sum;
                }
            }

            // try fasteest method first, using a recent nearby seed
            if (seedLastRead != -1 && bestSeed == -1) {
                System.out.println("starting nearby search " + (precomputeVs.length-50));

                bestSeed = searchForLowDisutilNear(seedLastRead, vtarg, vtargsums, is_space, toleranceNearby, nearbySearchDist);
                System.out.println("nearby search done");
                if (bestSeed != -1) {
                    System.out.println("found via nearby search");
                    bestDisutil = out_disutil_for_near;
                    bestVs = out_vs_for_near;
                }
            }

            // next best method, an anchor seed has been set in challenge mode
            if (nearbySearchLongSeed != -1 && bestSeed == -1) {
                System.out.println("starting anchor search " + (precomputeVsLong.length-50));

                bestSeed = searchForLowDisutilNearLong(nearbySearchLongSeed, vtarg, vtargsums, is_space, toleranceAnchor, nearbySearchDistLong);
                System.out.println("anchor search done");
                if (bestSeed != -1) {
                    System.out.println("found via anchor search");
                    bestDisutil = out_disutil_for_near;
                    bestVs = out_vs_for_near;
                }
            }

            if (bestSeed == -1 && num_good_chars >= 8 && !is_space[0] && !is_space[1] && !is_space[2] && !is_space[3]) {
                System.out.println("starting lattice search");
                long startTime = System.currentTimeMillis();
                for (float offs = -toleranceLattice/2; offs <= range + toleranceLattice/2; offs += toleranceLattice / 4) { // adds tol/8 error to each one...
                    float[] vs = new float[num_chars];
                    
                    float foffs = f_in_terms_of_v_and_pos(offs, cross_point_pos);
                    for (int i = 0; i < num_chars; i++) {
                        if (is_space[i]) continue;
                        vs[i] = v_in_terms_of_f_and_pos(foffs + cross_points[i] - cross_points[min_cross_idx], cross_point_pos); //  offs + min_vels[i];
                    }

                    //System.out.println("checking: " + velString(vs));

                    ArrayList<Long> candidates = seedCalc.vs_array_to_seed(vs, is_space, toleranceLattice);

                    for (int i = 0; i < candidates.size(); i++) {
                        if (seedCalc.out_disutil_for_vs_array.get(i) < bestDisutil) {
                            bestDisutil = seedCalc.out_disutil_for_vs_array.get(i);
                            bestSeed = candidates.get(i);
                            bestVs = vs;
                        }
                    }

                    if (System.currentTimeMillis() - startTime > 8000) {
                        System.out.println("lattice timeout 8000");
                        break;
                    }

                }
                System.out.println("lattice search done");
                if (bestSeed != -1) {
                    System.out.println("found via lattice search");
                }
            }

            // error correction via allowing one mistake
            if (seedLastRead != -1 && bestSeed == -1 && num_good_chars >= 8) {
                System.out.println("starting nearby generous search " + (precomputeVs.length-50));
                bestSeed = searchForLowDisutilNearGenerous(seedLastRead, vtarg, vtargsums, is_space, toleranceGenerous, nearbySearchDist);
                System.out.println("nearby generous search done");
                if (bestSeed != -1) {
                    System.out.println("found via nearby generous search");
                    bestDisutil = out_disutil_for_near;
                    bestVs = out_vs_for_near;
                }
            }
        }
        

        float[] bvs = seedCalc.seed_to_vel_vector(seedCalc.clamp(bestSeed), num_chars);

        // this is also a bad computation
        // float bSum = 0, mSum = 0, ccnt = 0;
        // for (int i = 0; i < num_chars; i++) {
        //     if (is_space[i]) continue;
        //     bSum += bvs[i];
        //     mSum += min_vels[i];
        //     ccnt += 1;
        // }
        float diffvs[] = new float[bvs.length];
        float worst = 0;
        for (int i = 0; i < bvs.length; i++) {
            if (is_space[i]) continue;
            diffvs[i] = bvs[i]-bestVs[i];
            worst = Math.max(Math.abs(diffvs[i]),worst);
        }
        
        //System.exit(0);

        out_disutil = bestDisutil;

        //positions out
        float predicted_locs[][] = new float[clamped_locs.length][clamped_locs[0].length];
        if (bestSeed != -1) {
            //float[] bvs = seedCalc.seed_to_vel_vector(seedCalc.clamp(bestSeed), num_chars);
            int n = bvs.length;
            float sum = 0;
            float diff = 0;
            float disB = 0;
            for (int i = 0; i < n; i++) {
                float[] p = new float[18];
                float[] q = new float[p.length];
                for (int j = 0; j < p.length; j++) {
                    p[j] = pos_in_terms_of_v_and_f(bvs[i], j+8);
                    //q[j] = (int)((175.5-p[j])/480*height); // dec -2 per. predictions not falling fast enough
                    q[j] = (pos_for_top_of_screen-p[j])/480.0f*height; // dec .5 per. predictions not falling fast enough...
                    //q[j] = (int)((152.5-p[j])/480*height); // inc ~1 per. predictions falling too fast.
                    //q[j] = (int)((132.5-p[j])/480*height/1.248f);
                    if (is_space[i]) q[j] = 0;
                    
                    // else {
                    //     q[j] -= clamped_locs[i][j];
                    //     if (clamped_locs[i][j] == 0) q[j] = 0;
                    //     sum += q[j];
                    //     if (j > 0) diff += q[j]-q[j-1];
                    // }  
                }
                int bestk = -1;
                if (!is_space[i]) {
                    float bestDisutil1 = 1000000;
                    for (int k = 0; k < q.length-clamped_locs[i].length; k++) {
                        float dis = 0;
                        for (int j = 0; j < clamped_locs[i].length; j++) {
                            float diff1 = clamped_locs[i][j] - q[j+k];
                            dis += clamped_locs[i][j] == 0 ? 0 : Math.abs(diff1);
                        }
                        if (dis < bestDisutil1) {
                            bestDisutil1 = dis;
                            bestk = k;
                        }
                     }
                }
                System.out.print(out_cave.substring(i*substr_mult,(i+1)*substr_mult) + " [");
                float[] k = new float[clamped_locs[i].length];
                for (int j = 0; j < k.length; j++) {
                    k[j] = clamped_locs[i][j] == 0 || is_space[i] ? 0 : q[j+bestk]-clamped_locs[i][j];
                    sum += k[j];
                    disB += Math.abs(k[j]);
                    if (clamped_locs[i][j] != 0) {
                        System.out.print(String.format("%2d, ", Math.round(k[j])));
                        predicted_locs[i][j] = q[j+bestk];
                    }
                    if (j > 0 && clamped_locs[i][j] != 0 && clamped_locs[i][j-1] != 0) {
                        diff += k[j] - k[j-1];
                    }
                }
                System.out.println("]");
                //System.out.println(out_cave.substring(i*substr_mult,(i+1)*substr_mult) + " " + velString(p));
                //System.out.println(out_cave.substring(i*substr_mult,(i+1)*substr_mult) + " " + Arrays.toString(q) + " " + bestk);
                
            }
            System.out.println("offsetsum:" + rnd(sum,1) + "  diff:" + rnd(diff,1) + "  dis:" + rnd(disB,1));
            // if sum is big, take down the 135 number. if diff > 0, take down 1.25

            System.out.println("seedVs " + velString(bvs));
            System.out.println("calcVs " + velString(bestVs));
            System.out.println("diffVs " + velString(diffvs));
            System.out.println("best: " + Drawer.seedToString(bestSeed) + " (disutil " + rnd(bestDisutil/num_good_chars,3) + ") (worst " + rnd(worst,3) + ") (n " + seedCalc.nth_inv(bestSeed) + ") (dist " + (seedLastRead == -1 ? "?" : ""+seedCalc.dist(seedLastRead, bestSeed)) + ")");

        }

        //System.out.println("predicted locs");
        //for (int i = 0; i < num_chars; i++) {
        //    System.out.println(out_cave.substring(i*substr_mult,(i+1)*substr_mult) + " " + velString1(predicted_locs[i]));
       // }
        /*{
            float[][] possibleVs = new float[10][num_chars];
            int numPossible = 0;

            for (int crossFrameOffset = 0; crossFrameOffset < 30; crossFrameOffset++) {
                float[] vs = new float[num_chars];
                float min_v = 10, max_v = -5;
                for (int i = 0; i < num_chars; i++) {
                    if (is_space[i]) continue;

                    ArrayList<Float> vel_measured = new ArrayList<Float>();
                    for (int q = 0; q < clamped_locs[i].length; q++) {
                        if (clamped_locs[i][q] > 0) {
                            float posu = pos_for_top_of_screen - predicted_locs[i][q]*480.0f/height;
                            vel_measured.add(v_in_terms_of_f_and_pos(q+crossFrameOffset, posu));
                        }
                    }
                    if (vel_measured.size() == 0) continue; // should never happen

                    float velsum = 0;
                    for (float fff: vel_measured) velsum += fff;
                    velsum /= vel_measured.size();
                    vs[i] = velsum;
                    min_v = Math.min(min_v, vs[i]);
                    max_v = Math.max(max_v, vs[i]);
                }
                //System.out.println(min_v + " " + max_v);
                if (min_v < -.1 || max_v > 5.1) continue;

                System.out.println("possible" + crossFrameOffset + " " + velString(vs));
                possibleVs[numPossible] = vs;
                numPossible += 1;
            }
        }*/
        

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

    float pos_in_terms_of_v_and_f(float v, float f) {
        return 200 + (v - 0.5f) * f - f*f / 2;
    }

    int lastSearchSeedForPrecompute = -1;
    float[] precomputeVs = new float[1];
    void precomputeExpectedFutureVs(long searchSeed, int len) {
        int seed = seedCalc.clamp(searchSeed);
        if (lastSearchSeedForPrecompute == seed)
            return;
        lastSearchSeedForPrecompute = seed;

        len += 50;
        System.out.print("precompute vs for " + Drawer.seedToString(searchSeed) + " " + len);
        
        precomputeVs = new float[len];
        for (int j = 0; j < len; j++) {
            seed = seed * 0x41c64e6d + 0x3039;
            int ret = (seed >>> 0x10) & 0x7fff;
            precomputeVs[j] = ret * 5.0f / 32768.0f;
        }
        System.out.println(" ...done");
        
    }

    int lastSearchSeedForPrecomputeLong = -1;
    float[] precomputeVsLong = new float[1];
    void precomputeExpectedFutureVsLong(long searchSeed, int len) {
        int seed = seedCalc.clamp(searchSeed);
        if (lastSearchSeedForPrecomputeLong == seed)
            return;
        lastSearchSeedForPrecomputeLong = seed;

        len += 50;
        System.out.print("precompute vs long for " + Drawer.seedToString(searchSeed) + " " + len);
        
        precomputeVsLong = new float[len];
        for (int j = 0; j < len; j++) {
            seed = seed * 0x41c64e6d + 0x3039;
            int ret = (seed >>> 0x10) & 0x7fff;
            precomputeVsLong[j] = ret * 5.0f / 32768.0f;
        }
        System.out.println(" ...done");
    }

    float out_disutil_for_near;
    float[] out_vs_for_near;
    long searchForLowDisutilNear(long searchSeed, float[][] vtarg, float[] vtargsums, boolean[] is_space, float tolerance, int range) {

        long bestSeed = -1;
        float bestDisutil = 100000;

        int cnt = 0;
        int n = vtarg[0].length;
        for (int j = 0; j < n; j++) {
            if (is_space[j]) continue;
            cnt++;
        }

        precomputeExpectedFutureVs(searchSeed, range);
        int seed = seedCalc.clamp(searchSeed);

        for (int i = 0; i < range && i < precomputeVs.length - 50; i++) {

            float vssum = 0;
            for (int j = 0; j < n; j++) {
                if (is_space[j]) continue;
                vssum += precomputeVs[j+i];
            }
            vssum /= cnt;

            int idx = (int)(vssum * (vtarg.length-1) / 5);

            if (idx >= 0 && idx < vtarg.length && vtargsums[idx] != -1) {
                float disutil = 0;
                boolean good = true;
                for (int j = 0; j < n; j++) {
                    if (is_space[j]) continue;
                    float dis = Math.abs(vtarg[idx][j]-precomputeVs[j+i]);
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
                        out_vs_for_near = new float[n];
                        for (int j = 0; j < n; j++)
                            out_vs_for_near[j] = vtarg[idx][j]; 
                    }
                    if (n >= 5 && bestDisutil <= 0.02*cnt) {
                        break; //good enough
                    }
                }
            }
            
            seed = seed * 0x41c64e6d + 0x3039;

        }
        out_disutil_for_near = bestDisutil;
        return bestSeed;
    
    }


    // searches over larger window
    long searchForLowDisutilNearLong(long searchSeed, float[][] vtarg, float[] vtargsums, boolean[] is_space, float tolerance, int range) {

        long bestSeed = -1;
        float bestDisutil = 100000;

        int cnt = 0;
        int n = vtarg[0].length;
        for (int j = 0; j < n; j++) {
            if (is_space[j]) continue;
            cnt++;
        }

        precomputeExpectedFutureVsLong(searchSeed, range);
        int seed = seedCalc.clamp(searchSeed);

        for (int i = 0; i < range && i < precomputeVsLong.length-50; i++) {

            float vssum = 0;
            for (int j = 0; j < n; j++) {
                if (is_space[j]) continue;
                vssum += precomputeVsLong[j+i];
            }
            vssum /= cnt;

            int idx = (int)(vssum * (vtarg.length-1) / 5);

            if (idx >= 0 && idx < vtarg.length && vtargsums[idx] != -1) {
                float disutil = 0;
                boolean good = true;
                for (int j = 0; j < n; j++) {
                    if (is_space[j]) continue;
                    float dis = Math.abs(vtarg[idx][j]-precomputeVsLong[j+i]);
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
                        out_vs_for_near = new float[n];
                        for (int j = 0; j < n; j++)
                            out_vs_for_near[j] = vtarg[idx][j]; 
                    }
                    if (n >= 5 && bestDisutil <= 0.02*cnt) {
                        break; //good enough
                    }
                }
            }
            
            seed = seed * 0x41c64e6d + 0x3039;

        }
        out_disutil_for_near = bestDisutil;
        return bestSeed;
    
    }


    // allows a single char to be discarded.
    long searchForLowDisutilNearGenerous(long searchSeed, float[][] vtarg, float[] vtargsums, boolean[] is_space, float tolerance, int range) {

        long bestSeed = -1;
        float bestDisutil = 100000;

        int cnt = 0;
        int n = vtarg[0].length;
        for (int j = 0; j < n; j++) {
            if (is_space[j]) continue;
            cnt++;
        }

        precomputeExpectedFutureVs(searchSeed, range);
        int seed = seedCalc.clamp(searchSeed);

        for (int i = 0; i < range && i < precomputeVs.length-50; i++) {

            float vssum = 0;
            for (int j = 0; j < n; j++) {
                if (is_space[j]) continue;
                vssum += precomputeVs[j+i];
            }
            vssum /= cnt;

            int idx = (int)(vssum * (vtarg.length-1) / 5);

            if (idx >= 0 && idx < vtarg.length && vtargsums[idx] != -1) {
                float disutil = 0;
                boolean good = true;
                int numBad = 0;
                for (int j = 0; j < n; j++) {
                    if (is_space[j]) continue;
                    float dis = Math.abs(vtarg[idx][j]-precomputeVs[j+i]);
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
                        out_vs_for_near = new float[n];
                        for (int j = 0; j < n; j++)
                            out_vs_for_near[j] = vtarg[idx][j]; 
                    }
                    if (n >= 5 && bestDisutil <= 0.02*cnt) {
                        break; //good enough
                    }
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
            s.append(String.format("%5.2f",v[i]));
        }
        s.append("]");
        return s.toString();
    }
    static String velString(double[] v) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i= 0; i < v.length; i++) {
            if (i > 0) s.append(", ");
            s.append(String.format("%5.2f",v[i]));
        }
        s.append("]");
        return s.toString();
    }
    static String velString0(float[] v) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i= 0; i < v.length; i++) {
            if (i > 0) s.append(", ");
            s.append(String.format("%3d",(int)(v[i]+.5)));
        }
        s.append("]");
        return s.toString();
    }
    static String velString1(float[] v) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i= 0; i < v.length; i++) {
            if (i > 0) s.append(", ");
            s.append(String.format("%6.1f",v[i]));
        }
        s.append("]");
        return s.toString();
    }

    String rnd(double d, int i) {
        return String.format("%."+i+"f", d);
    }

}
