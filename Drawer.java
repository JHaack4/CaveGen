import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;

// This code draws the pictures

public class Drawer {

    final int U = 8; // num pixels per input unit
    final int N = 80; // num pixels per unit output
    final float M = 170.0f; // meters per unit
    final int Tsize = 40; // teki/item size
    final int Isize = 40;

    float alpha1 = 0.82f;
    float alpha2 = 0.91f;


    HashMap<String, Image> IMG = new HashMap<String, Image>();
    HashMap<String, String> missing = new HashMap<String, String>();
    HashMap<String, String> special = new HashMap<String, String>();
    String plantNames = "", buriedItems;
    String purple20 = "", white20 = "";

    Color[] colorsFT = new Color[] {
                new Color(0,0,0),
                new Color(160,0,160),
                new Color(0,0,240),
                new Color(240,0,0),
                new Color(200,100,0),
                new Color(70,160,70),
                new Color(0,0,0)
            };
    Color[] colorsSP = new Color[] {
                new Color(255,130,220),
                new Color(220,0,0),
                new Color(220,120,0),
                new Color(0,0,0),
                new Color(0,140,60),
                new Color(80,80,80),
                new Color(140,225,100),
                new Color(0,150,150),
                new Color(160,0,160),
                new Color(0,0,255)
            };
    Color bgt = new Color(0,0,0);
    Color spc = new Color(80,140,220);
    Color spc2 = new Color(0,0,0);

    float[] gaugeTicks = new float[] {0.5f,1,2,3,4,5,6,7,8};
    Color[] gaugeColors = new Color[] {
        new Color(160,255,255), //0
        new Color(255,255,160),
        new Color(180,180,255),
        new Color(255,180,180),
        new Color(220,160,255),
        new Color(255,220,140),// 5
        new Color(160,210,150),
        new Color(190,150,120),
        new Color(0,0,255),
        //new Color(0,0,0) //9
    };

    Drawer() {
        missing.put("fminihoudai", "minihoudai");
        missing.put("fkabuto","kabuto");
        
        special.put("pod", "pod_icon");
        special.put("bomb", "Bingo_Battle_Bomb_icon");
        special.put("hole", "Cave_icon");
        special.put("geyser", "Geyser_icon");
        special.put("clog", "36px-Clog_icon");
        special.put("gashiba", "Gas_pipe_icon");
        special.put("hiba", "Fire_geyser_icon");
        special.put("elechiba", "Electrical_wire_icon");
        special.put("gate", "Gray_bramble_gate_icon");
        special.put("rock", "Roulette_Wheel_boulder");
        special.put("egg", "36px-Egg_icon");
        special.put("daiodogreen", "daiodogreen");
        special.put("kareooinu_l", "kareooinu");
        special.put("kareooinu_s", "kareooinu_s");
        special.put("ooinu_s", "ooinu_s");
        special.put("wakame_s", "wakame_s");
        special.put("chiyogami", "chiyogami");

        purple20 = ",EC2,FC1,HoB2,CoS2,GK2,SR2,";
        white20 = ",WFG3,BK1,SH2,SR1,";
        plantNames = ",ooinu_s,ooinu_l,wakame_s,wakame_l,kareooinu_s,kareooinu_l,daiodored,"
            + "daiodogreen,clover,hikarikinoko,tanpopo,zenmai,nekojarashi,tukushi,magaret,watage";
        buriedItems = ",leaf_yellow,teala_dia_a,teala_dia_c,xmas_item,yoyo_red,akagai,toy_ring_a_green,"
            + "sinjyu,makigai,momiji_kare,bane_yellow,toy_ring_a_red,diamond_blue,donutswhite_s,gum_tape,toy_ring_b_blue,";

        if (CaveGen.drawSpawnPoints
             || CaveGen.drawAngles) {
            alpha1 = 0.65f;
            alpha2 = 0.75f;
        } else if (CaveGen.drawScores || CaveGen.drawDoorLinks || CaveGen.drawWayPoints) {
            alpha1 = 0.7f;
            alpha2 = 0.8f;
        }
    }

    Image getMapUnit(MapUnit m) throws Exception {
        String loc = "files/" + CaveGen.fileSystem + "/" + "arc/" + m.name + "/arc.d/texture.bti.png";
        String hash = loc + m.rotation;
        if (IMG.containsKey(hash)) return IMG.get(hash);

        BufferedImage im = ImageIO.read(new File(loc));
        im = rotateImage(im, m.rotation * 90);
        Image im2 = im.getScaledInstance(m.dX*N, m.dZ*N, Image.SCALE_DEFAULT);
            
        IMG.put(hash, im2);
        return im2;
    }

    Image getTeki(Teki t) throws Exception {
        String name = t.tekiName.toLowerCase();
        if (missing.containsKey(name)) name = missing.get(name);
        if (special.containsKey(name)) return getSpecial(name);
        String loc = "files/" + CaveGen.fileSystem + "/" + "enemytex/arc.d/" + name + "/texture.bti.png";
        String hash = loc + t.type;
        if (IMG.containsKey(hash)) return IMG.get(hash);

        BufferedImage im = ImageIO.read(new File(loc));
        modAlpha(im, alpha1);
        //int Tsize = t.type == 6 ? this.Tsize / 2 : this.Tsize;
        Image im2 = im.getScaledInstance(Tsize, Tsize, Image.SCALE_DEFAULT);

        IMG.put(hash, im2);
        return im2;
    }

    Image getSpecial(String s) throws Exception {
        return getSpecial(s, 0);
    }

    Image getSpecial(String s, int rotation) throws Exception {
        String loc = "files/" + CaveGen.fileSystem + "/" + "enemytex/special/" + special.get(s) + ".png";
        String hash = loc + rotation;
        if (IMG.containsKey(hash)) return IMG.get(hash);

        BufferedImage im = ImageIO.read(new File(loc));
        modAlpha(im, alpha1);
        im = rotateImage(im, rotation * 90);
        int Tsize = this.Tsize;
        if (s.equals("gate")) Tsize = 80;
        if (s.equals("egg")) Tsize = Tsize * 3 / 5;
        if (s.equals("bomb")) Tsize = Tsize * 3 / 5;
        Image im2 = im.getScaledInstance(Tsize, Tsize, Image.SCALE_DEFAULT);

        IMG.put(hash, im2);
        return im2;
    }

    Image getItem(Item t, String inside, String region) throws Exception {
        String name = t != null ? t.itemName : inside;
        String loc = "files/" + CaveGen.fileSystem + "/" + "resulttex/" + region + "/arc.d/" + name.toLowerCase() + "/texture.bti.png";
        if (CaveGen.p251) loc =  "files/" + CaveGen.fileSystem + "/" + "resulttex/" + region + "/arc.d/" + name.toLowerCase() + ".bti.png";
        String hash = loc + inside;
        if (IMG.containsKey(hash)) return IMG.get(hash);

        BufferedImage im = ImageIO.read(new File(loc));
        modAlpha(im,alpha2);
        int Isize = t != null ? this.Isize : this.Isize*3/4;
        Image im2 = im.getScaledInstance(Isize, Isize, Image.SCALE_DEFAULT);

        IMG.put(hash, im2);
        return im2;
    }

    void draw(CaveGen g, boolean drawAsReport) throws Exception {

        BufferedImage img = new BufferedImage(N*g.mapMaxX, N*g.mapMaxZ,
                                                        BufferedImage.TYPE_INT_RGB);
        Graphics G = img.getGraphics();  

        // Draw the map units
        for (MapUnit m: g.placedMapUnits) {
            String loc = "files/" + g.fileSystem + "/" + "arc/" + m.name + "/arc.d/texture.bti.png";
            G.drawImage(getMapUnit(m), N*m.offsetX, N*m.offsetZ, null);
        }

        G.setFont(new Font("Serif", Font.BOLD, 12));

        if (drawAsReport) {
            bgt = new Color(0,0,0,0);
            G.setColor(new Color(225,225,225));
            G.fillRect(0, 0, g.mapMaxX*N, 5*N-45);
            for (MapUnit m: g.placedMapUnits) {
                int x = (int)(m.offsetX*N);
                int z = (int)(m.offsetZ*N);
                int w = (int)(m.dX*N);
                int h = (int)(m.dZ*N);
                G.setColor(new Color(0,225,225));
                G.drawRect(x,z,w,h);
            }
            G.setFont(new Font("Serif", Font.BOLD, 36));
            int zz = 40;
            int x = 10;
            int z = 0;
            String s1 = g.specialCaveInfoName + "-" + g.sublevel;
            G.setColor(new Color(0,0,0));
            G.drawString(s1, x, z+=zz);
            G.setFont(new Font("Serif", Font.BOLD, 24));
            String s2 = "   units:" + g.caveUnitFile 
                + "   lighting:" + g.lightingFile;
            String s4 = "   music:" + g.musicType 
                + "   echo:" + g.echoStrength
                + "   plane:" + g.hasFloorPlane
                + "   skybox:" + g.skyboxFile;
            G.drawString(s2, x + 100 + 20*s1.length(), 24);
            G.drawString(s4, x + 100 + 20*s1.length(), 52);
            String s3 = "   NumRooms:" + g.maxRoom
                + "   CorridorVsRoomProb:" + g.corridorProb
                + "   CapVsHallProb:" + (int)(g.capProb * 100) + "%";
            z+=zz;
            int holeo = 15;
            int spa = 45;
            G.drawString(s3, x + 100 + 20*s1.length(), 80);
            G.drawImage(getSpecial("pod"), x, z-holeo, null);
            if (!g.isFinalFloor) {
                G.drawImage(getSpecial("hole"), x+spa, z-holeo, null);
                if (g.holeClogged) G.drawImage(getSpecial("clog"), x+45, z-15, null);
            }
            if (g.isFinalFloor || g.hasGeyser) {
                G.drawImage(getSpecial("geyser"), x+spa*2, z-holeo, null);
                if (g.holeClogged) G.drawImage(getSpecial("clog"), x+90, z-15, null);
            }
            int xx = 0;
            int zzd = 20;
            z += 5;
            z += zz + zzd;
            int ext = 4;
            G.setFont(new Font("Serif", Font.BOLD, 36));
            drawTextOutline(G, "Teki M" + g.maxMainTeki, x, z, colorsSP[1], bgt);
            G.setFont(new Font("Serif", Font.BOLD, 16));
            xx = 170;
            int[] types = new int[] {5,8,1,0,6};
            for (int type: types) {
                for (Teki t: g.spawnMainTeki) {
                    if (t.type == type) {
                        drawTeki(G,g,t,xx,z-40);
                        if (type == 6)
                            drawTextOutline(G, "  m" + t.min, xx, z+8+ext, new Color(0,150,0), bgt);
                        else
                            drawTextOutline(G, "m" + t.min + "w" + t.weight, xx, z+8+ext, colorsSP[type], bgt);
                        xx += spa;
                    }
                }
            }
            z += zz + zzd;
            G.setFont(new Font("Serif", Font.BOLD, 36));
            drawTextOutline(G, "Item M" + g.maxItem, x, z, colorsSP[2], bgt);
            G.setFont(new Font("Serif", Font.BOLD, 16));
            xx = 170;
            for (Item t: g.spawnItem) {
                Image im = getItem(t, "", CaveGen.region);
                G.drawImage(im, xx, z-40, null);
                drawTextOutline(G, "m" + t.min + "w" + t.weight, xx, z+8+ext, colorsSP[2], bgt);
                xx += spa;
            }
            z += zz + zzd;
            G.setFont(new Font("Serif", Font.BOLD, 36));
            drawTextOutline(G, "CapTeki", x, z, colorsSP[9], bgt);
            G.setFont(new Font("Serif", Font.BOLD, 16));
            xx = 170;
            for (Teki t: g.spawnCapTeki) {
                drawTeki(G,g,t,xx,z-40);
                drawTextOutline(G, "m" + t.min + "w" + t.weight, xx, z+8+ext, colorsSP[t.type==0?0:9], bgt);
                xx += spa;
            }
            for (Teki t: g.spawnCapFallingTeki) {
                drawTeki(G,g,t,xx,z-40);
                drawTextOutline(G, "m" + t.min + "w" + t.weight, xx, z+8+ext, colorsSP[t.type==0?0:9], bgt);
                xx += spa;
            }
            z += zz + zzd;
            G.setFont(new Font("Serif", Font.BOLD, 36));
            drawTextOutline(G, "Gate M" + g.maxGate, x, z, colorsSP[5], bgt);
            G.setFont(new Font("Serif", Font.BOLD, 16));
            xx = 170;
            for (Gate t: g.spawnGate) {
                Image im = getSpecial("gate", 0);
                G.drawImage(im, xx, z-52, null);
                G.setColor(spc);
                String st = "" + (int)t.life;
                if (!g.drawNoGateLife) drawTextOutline(G,st,xx+36-3*st.length(),z-10,spc,spc2);//G.drawString(st,xx+36-3*st.length(),z-10);
                drawTextOutline(G, "w" + t.weight, xx+50, z+8+ext, colorsSP[5], bgt);
                xx += spa*2;
            }
            bgt = new Color(0,0,0);
        }

        if (g.drawWaterBox) {
            G.setColor(new Color(30,100,255,35));
            for (MapUnit m: g.placedMapUnits) {
                float[][] waterBoxes = m.waterBoxPos();
                for (int i = 0; i < waterBoxes.length; i++) {
                    G.fillRect((int)(waterBoxes[i][0]/M*N), (int)(waterBoxes[i][2]/M*N),
                               (int)((waterBoxes[i][3] - waterBoxes[i][0] + 1)/M*N),
                               (int)((waterBoxes[i][5] - waterBoxes[i][2] + 1)/M*N));

                }
            }
        }

        if (g.drawTreasureGauge && !g.challengeMode) {
            ArrayList<Integer> xs = new ArrayList<Integer>();
            ArrayList<Integer> zs = new ArrayList<Integer>();
            for (Item t: g.placedItems) {
                xs.add((int)(t.posX/M*N));
                zs.add((int)(t.posZ/M*N));
            }
            for (Teki t: g.placedTekis) {
                if (t.itemInside != null &&
                    (g.sublevel == 5 || !g.specialCaveInfoName.equals("SC")
                     || !t.tekiName.equalsIgnoreCase("blackman"))) {
                    xs.add((int)(t.posX/M*N));
                    zs.add((int)(t.posZ/M*N));
                }
            }
            /*for (int i = 0; i < xs.size(); i++) {
                for (int j = 1; j < 11; j++) {
                    if (j%2 == 1) continue;
                    if (j < 3 || j == 6)
                        G.setColor(new Color(20*j,20*j,255));
                    else G.setColor(new Color(255,255,20*j));
                    int rad = (int)(900.0f*N*j/10/M);
                    G.drawOval(xs.get(i)-rad/2, zs.get(i)-rad/2, rad, rad);
                }
            }*/
            for (int i = 0; i < xs.size(); i++) {
                for (int j = 0; j < gaugeTicks.length; j++) {
                    float tick = gaugeTicks[j];
                    G.setColor(gaugeColors[j]);
                    float distForTick = (1 - (float)Math.sqrt((tick - 0.5) / 9.5)) * 900f;
                    int rad = (int)(distForTick * 2 * N/M);
                    G.drawOval(xs.get(i)-rad/2, zs.get(i)-rad/2, rad, rad);
                }
            }
        }
      
        if (g.drawSpawnPoints) {

            G.setColor(new Color(255,0,0));

            ArrayList<SpawnPoint> doorSpawnPoints = new ArrayList<SpawnPoint>();
            for (MapUnit m: g.placedMapUnits) {
                for (Door d: m.doors) {
                    if (!doorSpawnPoints.contains(d.spawnPoint))
                        doorSpawnPoints.add(d.spawnPoint);
                }
            }
            for (SpawnPoint sp: doorSpawnPoints) {
                G.setColor(colorsSP[sp.type]);
                int rad = 10;
                int x = (int)(sp.posX/M*N - rad/2);
                int z = (int)(sp.posZ/M*N - rad/2);
                //G.drawString("5", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
                G.fillOval(x,z,rad,rad);
                if (g.drawAngles)
                    drawAngle(G,sp.posX,sp.posZ,sp.ang);
            }

            for (MapUnit m: g.placedMapUnits) {
                for (SpawnPoint sp: m.spawnPoints) {
                    if (m.type == 2 && sp.type == 9) continue;
                    float distToStart = CaveGen.spawnPointDist(g.placedStart, sp);
                    float distToHole = g.placedHole == null ? CaveGen.INF : CaveGen.spawnPointDist(g.placedHole, sp);
                    float distToGeyser = g.placedGeyser == null ? CaveGen.INF : CaveGen.spawnPointDist(g.placedGeyser, sp);
                    G.setColor(colorsSP[sp.type]);
                    //G.drawString(sp.type+"", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
                    int rad = 10;
                    if (sp.type == 0) rad = (int)(sp.radius/M*N) + 6;
                    if (sp.type == 4 || sp.type == 7 || sp.type == 2 || sp.type == 9)
                        rad += 6;
                    if ((sp.type == 0 && distToStart < 300) 
                        || (sp.type == 1 && (distToStart < 300 || distToHole < 200 || distToGeyser < 200))
                        || (sp.type == 4 && distToStart < 150)
                        || (sp.type == 8 && (distToStart < 300 || distToHole < 150 || distToGeyser < 150))) {
                            rad = 3;
                            int x = (int)(sp.posX/M*N - rad/2);
                            int z = (int)(sp.posZ/M*N - rad/2);
                            //G.fillOval(x,z,rad,rad);
                            continue;
                    }
                    int x = (int)(sp.posX/M*N - rad/2);
                    int z = (int)(sp.posZ/M*N - rad/2);
                    if (sp.type != 0) G.fillOval(x,z,rad,rad);
                    if (sp.type == 0) {
                        for (int i = 0; i < sp.maxNum; i++) {
                            rad -= 4;
                            x += 2;
                            z += 2;
                            if (rad < 1) rad = 1;
                            G.drawOval(x,z,rad,rad);
                        }
                    }
                    if (g.drawAngles)
                        drawAngle(G,sp.posX,sp.posZ,sp.ang);
                }
            }
        }

        // Draw the objects placed
        if (!g.drawNoObjects) {
            

            if (g.placedStart != null) {
                Image im = getSpecial("pod");
                int x = (int)(g.placedStart.posX/M*N - im.getWidth(null)/2);
                int z = (int)(g.placedStart.posZ/M*N - im.getHeight(null)/2);
                G.drawImage(im, x, z, null);
                if (g.drawAngles)
                    drawAngle(G, g.placedStart.posX, g.placedStart.posZ, g.placedStart.ang);
                //G.drawString("S", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
            }
            if (g.placedHole != null && !g.drawNoHoles) {
                Image im = getSpecial("hole");
                int x = (int)(g.placedHole.posX/M*N - im.getWidth(null)/2);
                int z = (int)(g.placedHole.posZ/M*N - im.getHeight(null)/2);
                G.drawImage(im, x, z, null);
                if (g.holeClogged) {
                    Image im2 = getSpecial("clog");
                    G.drawImage(im2,x,z,null);
                }
                //G.drawString("H", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
            }
            if (g.placedGeyser != null && !g.drawNoHoles) {
                Image im = getSpecial("geyser");
                int x = (int)(g.placedGeyser.posX/M*N - im.getWidth(null)/2);
                int z = (int)(g.placedGeyser.posZ/M*N - im.getHeight(null)/2);
                G.drawImage(im, x, z, null);
                if (g.holeClogged) {
                    Image im2 = getSpecial("clog");
                    G.drawImage(im2,x,z,null);
                }
                //G.drawString("G", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
            }
            
            if (!g.drawNoTeki) {
                for (Teki t: g.placedTekis) {
                    if (g.drawNoPlants && plantNames.contains("," + t.tekiName.toLowerCase() +",")) continue;
                    int yaddn = t.spawnPoint.type == 9 && t.fallType > 0 ? -12: 0;
                    try {
                        Image im = getTeki(t);
                        int x = (int)(t.posX/M*N - im.getWidth(null)/2);
                        int z = (int)(t.posZ/M*N + yaddn - im.getHeight(null)/2); 
                        G.drawImage(im, x, z, null);
                        if (g.drawAngles)
                            drawAngle(G, t.posX, t.posZ, t.ang);
                        if (t.fallType != 0 && !g.drawNoFallType) {
                            G.setColor(colorsFT[t.fallType]);
                            G.drawLine(x+5,z+5,x-10,z-10);
                            G.drawLine(x+9,z+1,x-6,z-14);
                            G.drawLine(x+1,z+9,x-14,z-6);
                        }
                        if (t.tekiName.equalsIgnoreCase("blackpom")) {
                            String sls = g.specialCaveInfoName + g.sublevel;
                            if (purple20.indexOf(","+sls+",") >= 0) {
                                drawTextOutline(G, "<20", x+2, z+5, spc, spc2);
                            }
                        }
                        if (t.tekiName.equalsIgnoreCase("whitepom")) {
                            String sls = g.specialCaveInfoName + g.sublevel;
                            if (white20.indexOf(","+sls+",") >= 0) {
                                drawTextOutline(G, "<20", x+2, z+5, spc, spc2);
                            }
                        }
                        if (t.tekiName.equalsIgnoreCase("blackman")) {
                            String bmt = g.waterwraithTimer + "";
                            drawTextOutline(G, "t"+bmt, x+2, z+5, spc, spc2);
                        }
                    } catch(Exception e) {
                        System.out.println("Failed Img: " + t.tekiName);
                        String st = String.format("T%4.4s", t.tekiName);
                        G.drawString(st, (int)(t.posX/M*N), (int)(t.posZ/M*N + yaddn));
                    }
                }
                for (Teki t: g.placedTekis) {
                    int yaddn = t.spawnPoint.type == 9 && t.fallType > 0 ? -12: 0;
                    if (t.itemInside != null) {
                        try {
                            Image im = getItem(null, t.itemInside, g.region);
                            int x = (int)(t.posX/M*N - im.getWidth(null)/2 + 5);
                            int z = (int)(t.posZ/M*N + yaddn - im.getHeight(null)/2 + 5);
                            G.drawImage(im, x, z, null);
                        } catch (Exception e) {
                            System.out.println("Failed Img: " + t.itemInside);
                            G.drawString("IN" + t.itemInside, (int)(t.posX/M*N), (int)(t.posZ/M*N));
                        }
                    }
                }
            }
            if (!g.drawNoItems) {
                for (Item t: g.placedItems) {
                    if (g.drawNoBuriedItems && buriedItems.contains(t.itemName))
                        continue;
                    try {
                        Image im = getItem(t, "", g.region);
                        int x = (int)(t.posX/M*N - im.getWidth(null)/2);
                        int z = (int)(t.posZ/M*N - im.getHeight(null)/2);
                        G.drawImage(im, x, z, null);
                    } catch (Exception e) {
                        System.out.println("Failed Img: " + t.itemName);
                        String st = String.format("I%4.4s", t.itemName);
                        G.drawString(st, (int)(t.posX/M*N), (int)(t.posZ/M*N));
                    }
                } 
            }
            if (!g.drawNoGates) {
                for (Gate t: g.placedGates) {
                    Image im = getSpecial("gate", (int)(t.ang/1.57f));
                    int x = (int)(t.posX/M*N - im.getWidth(null)/2);
                    int z = (int)(t.posZ/M*N - im.getHeight(null)/2);
                    G.drawImage(im, x, z, null);
                    G.setColor(spc);
                    String st = "" + (int)t.life;
                    if (!g.drawNoGateLife) drawTextOutline(G,st,x+39-3*st.length(),z+45,spc,spc2);//G.drawString(st,x+39-3*st.length(),z+45);
                    //G.drawString(st, (int)(t.posX/M*N), (int)(t.posZ/M*N));
                }
            }
        }

        if (g.drawWayPoints || g.drawWayPointEdgeDists || g.drawWayPointVertDists) {
            G.setFont(new Font("Serif", Font.BOLD, 12));
            for (MapUnit m: g.placedMapUnits) {
                for (WayPoint wp: m.wayPoints) {
                    int x = (int)(wp.posX/M*N);
                    int z = (int)(wp.posZ/M*N);
                    int rad = (int)(wp.radius/M*N) + 12;
                    G.setColor(new Color(153,153,0,wp.idx<m.doors.size()?20:40));
                    G.fillOval(x-rad/2, z-rad/2, rad, rad);
                    if (g.drawWayPointEdgeDists) {
                        for (WayPoint owp: wp.adj) {
                            int ox = (int)(owp.posX/M*N);
                            int oz = (int)(owp.posZ/M*N);
                            G.setColor(new Color(153,153,0,15));
                            if (drawAsReport)
                                G.setColor(new Color(80,80,0,150));
                            G.drawLine(x,z,(ox*5+x)/6,(oz*5+z)/6);
                            String s = "" + (int)(CaveGen.wayPointDist(wp,owp)/10);
                            if (s.equals("0")) continue;
                            drawTextOutline(G,s,(x+ox)/2-s.length()*5/2,(z+oz)/2+4,
                                              new Color(160,160,0), bgt);
                        }
                    }
                    G.setColor(new Color(80,80,0,150));
                    if (wp.backWp != null) {
                        int ox = (int)(wp.backWp.posX/M*N);
                        int oz = (int)(wp.backWp.posZ/M*N);
                        G.drawLine(x,z,ox,oz);
                    }
                    String s = "" + (int)(wp.distToStart/10);
                    if (g.drawWayPointVertDists && !drawAsReport)
                        drawTextOutline(G,s,x-s.length()*5/2,z+4,new Color(255,255,0),bgt);
                }
            }
        }

        if (g.drawDoorLinks || g.drawDoorIds) {
            for (MapUnit m: g.placedMapUnits) {
                int cx = m.offsetX * N + m.dX * N / 2;
                int cz = m.offsetZ * N + m.dZ * N / 2;
                for (int i = 0; i < m.doors.size(); i++) {
                    Door d = m.doors.get(i);
                    int dx = (int)(d.posX/M*N);
                    int dz = (int)(d.posZ/M*N);
                    if (d.dirSide == 0) dz += 10;
                    if (d.dirSide == 1) dx -= 10;
                    if (d.dirSide == 2) dz -= 10;
                    if (d.dirSide == 3) dx += 10;
                    G.setFont(new Font("Serif", Font.BOLD, 16));
                    if (g.drawDoorIds)
                        drawTextOutline(G, ""+i, dx-7, dz+7, new Color(155,100,255), bgt);
                    for (int j = 0; j < d.doorLinks.size(); j++) {
                        DoorLink l = d.doorLinks.get(j);
                        Door o = m.doors.get(l.otherIdx);
                        int ox = (int)(o.posX/M*N);
                        int oz = (int)(o.posZ/M*N);
                        if (o.dirSide == 0) oz += 10;
                        if (o.dirSide == 1) ox -= 10;
                        if (o.dirSide == 2) oz -= 10;
                        if (o.dirSide == 3) ox += 10;
                        G.setColor(new Color(255,0,255,40));
                        G.drawLine(dx,dz,ox,oz);
                        G.setFont(new Font("Serif", Font.BOLD, 12));
                        if (g.drawDoorLinks)
                            drawTextOutline(G, ((int)(l.dist/10)) + "",
                                        (5*dx+ox)/6-5-(l.dist>=1000?4:0), (5*dz+oz)/6+5,
                                        new Color(255,0,255), bgt);
                    }
                }
            }
        }

        if (g.drawEnemyScores) {
            G.setColor(new Color(255,0,0));
            G.setFont(new Font("Serif", Font.BOLD, 16));
            for (Teki t: g.placedTekis) {
                if (t.type == 0 && t.mapUnit.type == 1)
                    drawTextOutline(G, "2", (int)(t.posX/M*N+3), (int)(t.posZ/M*N-3), new Color(255,0,0), bgt);
                if (t.type == 1 && t.mapUnit.type == 1)
                    drawTextOutline(G, "10", (int)(t.posX/M*N+3), (int)(t.posZ/M*N-3), new Color(255,0,0), bgt);
                if (t.type == 5)
                    drawTextOutline(G, "5", (int)(t.posX/M*N+3), (int)(t.posZ/M*N-3), new Color(255,0,0), bgt);
            }
        }

        if (g.drawSpawnOrder && !drawAsReport) {
            G.setFont(new Font("Serif", Font.BOLD, 24));
            int i = 0;
            for (MapUnit m: g.placedMapUnits) {
                int x = (int)(m.offsetX*N);
                int z = (int)(m.offsetZ*N);
                int w = (int)(m.dX*N);
                int h = (int)(m.dZ*N);
                G.setColor(new Color(0,225,225));
                G.drawRect(x,z,w,h);
                drawTextOutline(G, "" + (i++), x+2, z+h-2, new Color(0,255,255), bgt);
            }
            if (!g.drawNoObjects) {
                G.setFont(new Font("Serif", Font.BOLD, 16)); 
                drawTextOutline(G, ""+(i++), (int)(g.placedStart.posX/M*N-15), (int)(g.placedStart.posZ/M*N+15), new Color(0,255,255), bgt);
                if (g.placedHole != null) {
                    drawTextOutline(G, ""+(i++), (int)(g.placedHole.posX/M*N-15), (int)(g.placedHole.posZ/M*N+15), new Color(0,255,255), bgt);
                }
                if (g.placedGeyser != null) {
                    drawTextOutline(G, ""+(i++), (int)(g.placedGeyser.posX/M*N-15), (int)(g.placedGeyser.posZ/M*N+15), new Color(0,255,255), bgt);
                }
                for (Teki t: g.placedTekis) {
                    if ((t.type == 0 || t.type == 1) && t.mapUnit.type == 0) continue;
                    drawTextOutline(G, ""+(i++), (int)(t.posX/M*N-15), (int)(t.posZ/M*N+15), new Color(0,255,255), bgt);
                }
                for (Item t: g.placedItems) {
                    drawTextOutline(G, ""+(i++), (int)(t.posX/M*N-15), (int)(t.posZ/M*N+15), new Color(0,255,255), bgt);
                }
                for (Teki t: g.placedTekis) {
                    int yaddn = t.fallType > 0 ? -14: 0;
                    if ((t.type == 0 || t.type == 1) && t.mapUnit.type == 0)
                        drawTextOutline(G, ""+(i++), (int)(t.posX/M*N-15), (int)(t.posZ/M*N+15+yaddn), new Color(0,255,255), bgt);
                }
                for (Gate t: g.placedGates) {
                    drawTextOutline(G, ""+(i++), (int)(t.posX/M*N-15), (int)(t.posZ/M*N+15), new Color(0,255,255), bgt);
                }
            }
        }

        if (g.drawUnitHoleScores) {
            G.setFont(new Font("Serif", Font.BOLD, 26));
            for (MapUnit m: g.placedMapUnits) {
                int x = m.offsetX*N + m.dX*N/2, z = m.offsetZ*N + m.dZ*N/2;
                String s = m.unitScoreByPhase.get(0)+"";
                if (m.type == 1)
                    drawTextOutline(G, s, x-s.length()*5, z-3, new Color(0,155,0), bgt);
            }
            G.setFont(new Font("Serif", Font.BOLD, 14)); 
            for (MapUnit m: g.placedMapUnits) {
                for (Door d: m.doors) {
                    int xx = (int)(d.posX/M*N);
                    int zz = (int)(d.posZ/M*N);
                    String s = d.doorScoreByPhase.get(0) + "";
                    drawTextOutline(G, s, xx-s.length()*3,zz-3, new Color(0,155,0), bgt);
                }
            }
        }

        if (g.drawUnitItemScores) {
            G.setFont(new Font("Serif", Font.BOLD, 26));
            for (MapUnit m: g.placedMapUnits) {
                int x = m.offsetX*N + m.dX*N/2, z = m.offsetZ*N + m.dZ*N/2;
                String s = m.unitScoreByPhase.get(1)+"";
                if (m.type == 1)
                    drawTextOutline(G, s, x-s.length()*5, z-3+24, new Color(175,110,0), bgt);
            }
            G.setFont(new Font("Serif", Font.BOLD, 14)); 
            for (MapUnit m: g.placedMapUnits) {
                for (Door d: m.doors) {
                    int xx = (int)(d.posX/M*N);
                    int zz = (int)(d.posZ/M*N);
                    String s = d.doorScoreByPhase.get(1) + "";
                    drawTextOutline(G, s, xx-s.length()*3,zz-3+16, new Color(175,110,0), bgt);
                }
            }
        }

        if (g.drawHoleProbs && g.challengeMode) {
            G.setFont(new Font("Serif", Font.BOLD, 14));
            float sumProbs = 0.00000001f;
            for (MapUnit m: g.placedMapUnits) {
                for (SpawnPoint sp: m.spawnPoints) {
                    if ((sp == g.placedHole || sp == g.placedGeyser || !sp.filled) && sp.type == 9 && sp.mapUnit.type == 0
                            || sp.type == 4 && g.spawnPointDist(g.placedStart, sp) >= 150) {
                        float prob = CaveGen.INF;
                        if (sp.probVisuallyEmpty > 0)
                            prob = sp.scoreHole / sp.probVisuallyEmpty;
                        sumProbs += prob;
                    }
                }
            }
            for (MapUnit m: g.placedMapUnits) {
                for (SpawnPoint sp: m.spawnPoints) {
                    if (sp.type == 4 && sp.scoreHole > -1 && g.spawnPointDist(g.placedStart, sp) >= 150) {
                        float prob = CaveGen.INF;
                        if (sp.probVisuallyEmpty > 0)
                            prob = sp.scoreHole / sp.probVisuallyEmpty;
                        prob /= sumProbs;
                        String st = (int)(prob*100 + 0.5f) + "%";
                        int x = (int)(sp.posX/M*N);
                        int z = (int)(sp.posZ/M*N);
                        drawTextOutline(G, st, x+8, z+5-17, new Color(0,255,0), bgt);
                    } else if ((sp == g.placedHole || sp == g.placedGeyser || !sp.filled) && sp.type == 9 && sp.mapUnit.type == 0) {
                        float prob = CaveGen.INF;
                        if (sp.probVisuallyEmpty > 0)
                            prob = sp.scoreHole / sp.probVisuallyEmpty;
                        prob /= sumProbs;
                        String st = (int)(prob*100 + 0.5f) + "%";
                        int x = (int)(sp.posX/M*N);
                        int z = (int)(sp.posZ/M*N);
                        if (sp.scoreHole > -1)
                            drawTextOutline(G, st, x+8, z-17, new Color(0,255,0), bgt);
                    }
                }
            }
        }

        if (g.drawScores) {
            G.setFont(new Font("Serif", Font.BOLD, 20));
            for (MapUnit m: g.placedMapUnits) {
                for (SpawnPoint sp: m.spawnPoints) {
                    if (sp.type == 2 && sp.scoreItem > -1) {
                        int x = (int)(sp.posX/M*N);
                        int z = (int)(sp.posZ/M*N);
                        drawTextOutline(G, sp.scoreItem+"", x+8, z+5, new Color(255,170,0), bgt);
                        //G.drawString(sp.scoreItem+"", (int)(pos[0]/M*N), (int)(pos[1]/M*N));
                    } else if (sp.type == 4 && sp.scoreHole > -1) {
                        int x = (int)(sp.posX/M*N);
                        int z = (int)(sp.posZ/M*N);
                        drawTextOutline(G, sp.scoreHole+"", x+8, z+5, new Color(0,255,0), bgt);
                    } else if (sp.type == 9) {
                        int x = (int)(sp.posX/M*N);
                        int z = (int)(sp.posZ/M*N);
                        if (sp.scoreItem > -1)
                            drawTextOutline(G, sp.scoreItem+"", x+8, z+18, new Color(255,170,0), bgt);
                        if (sp.scoreHole > -1)
                            drawTextOutline(G, sp.scoreHole+"", x+8, z, new Color(0,255,0), bgt);
                    }
                }
            }
        }
        
        String seedN = seedToString(g.initialSeed);
        String caveN = g.specialCaveInfoName;
        String output = g.p251 ? "output251/" : "output/";
        File outputDir0 = new File(output);
        outputDir0.mkdir();
        if (drawAsReport) {
            File outputDir3 = new File(output + "!caveinfo/");
            outputDir3.mkdir();
            File outputFile3 = new File(output + "!caveinfo/"
                                        + caveN + "-" + g.sublevel + ".png"); 
            ImageIO.write(img, "png", outputFile3);
            return;
        }
        if (g.folderSeed) {
            File outputDir2 = new File(output + seedN);
            outputDir2.mkdir();
            File outputFile2 = new File(output + seedN + "/"
                                        + caveN + "-" + g.sublevel + ".png"); 
            ImageIO.write(img, "png", outputFile2);
        }
        if (g.folderCave) {
            File outputDir = new File(output + caveN + "-" + g.sublevel);
            outputDir.mkdir();
            File outputFile = new File(output + caveN + "-"
                                       + g.sublevel + "/" + seedN + ".png");
            ImageIO.write(img, "png", outputFile);
        }
    }

    public void drawCaveInfo(CaveGen g) throws Exception {
        int maxZ = 16;
        // reset parameters
        g.queueCap = new LinkedList<MapUnit>();
        g.queueRoom = new LinkedList<MapUnit>();
        g.queueCorridor = new LinkedList<MapUnit>();
        g.placedMapUnits = new ArrayList<MapUnit>();
        g.placedTekis = new ArrayList<Teki>();
        g.placedItems = new ArrayList<Item>();
        g.placedGates = new ArrayList<Gate>();
        g.placedStart = null;
        g.placedHole = null;
        g.placedGeyser = null;
        g.mapMaxX = maxZ;
        g.mapMaxZ = 0;
        g.mapMaxX = Math.max(g.mapMaxX, (int)(1+(180+g.spawnMainTeki.size()*45.0)/N));
        g.mapMaxX = Math.max(g.mapMaxX, (int)(1+(180+(g.spawnCapFallingTeki.size()+g.spawnCapTeki.size())*45.0)/N));
        g.mapMaxX = Math.max(g.mapMaxX, (int)(1+(180+g.spawnItem.size()*45.0)/N));
        g.mapMaxX = Math.max(g.mapMaxX, (int)(1+(180+g.spawnGate.size()*2*45.0)/N));
        
        for (MapUnit m: g.spawnMapUnitsSortedAndRotated) {
            switch(m.type) {
            case 0: g.queueCap.add(m); break;
            case 1: g.queueRoom.add(m); break;
            case 2: g.queueCorridor.add(m); break;
            }
        }

        int x = 1, z = 6;
        for (MapUnit m: g.queueCap) {
            MapUnit a = m.copy();
            g.placedMapUnits.add(a);
            a.offsetX = x;
            a.offsetZ = z;
            x += 2;
            g.mapMaxX = Math.max(g.mapMaxX, x);
        }
        for (MapUnit m: g.queueCorridor) {
            MapUnit a = m.copy();
            g.placedMapUnits.add(a);
            a.offsetX = x;
            x += 2;
            a.offsetZ = z + 1 - a.dZ;
            g.mapMaxX = Math.max(g.mapMaxX, x);
        }
        z += 2; x = 0;
        for (MapUnit m: g.queueRoom) {
            MapUnit a = m.copy();
            g.placedMapUnits.add(a);
            if (a.dX + x > g.mapMaxX && x != 0) {
                x = 0;
                z = g.mapMaxZ + 1;
            }
            a.offsetX = x;
            a.offsetZ = z;
            x += a.dX + 1;
            //z += a.dZ;
            g.mapMaxX = Math.max(g.mapMaxX, x);
            g.mapMaxZ = Math.max(g.mapMaxZ, z + a.dZ);
            if (x >= g.mapMaxX) {
                x = 0;
                z = g.mapMaxZ + 1;
            }
        }   

        for (MapUnit m: g.placedMapUnits) {
            for (Door d: m.doors) {
                d.adjacentDoor = new Door();
            }
        }

        g.addSpawnPoints();
        
        // build fake waypoint graph
        for (MapUnit m: g.placedMapUnits) {
            for (WayPoint wp: m.wayPoints) {
                wp.adj = new ArrayList<WayPoint>();
                wp.inverts = new ArrayList<WayPoint>();
                for (Integer idx: wp.links) {
                    wp.adj.add(m.wayPoints.get(idx));
                }
            }
        }
        for (MapUnit m: g.placedMapUnits) {
            for (WayPoint w: m.wayPoints) {
                for (WayPoint w2: w.adj) {
                    w2.inverts.add(w);
                }
            }
        }
        for (MapUnit m: g.placedMapUnits) {
            for (WayPoint w: m.wayPoints) {
                w.distToStart = 0;
                w.isStart = false;
                w.backWp = w;
            }
        }

        draw(g, true);
    }

    public static String seedToString(int seedIn) {
        long seed = seedIn;
        if (seed < 0) seed = Integer.MAX_VALUE * 2l + seed + 2;
        String seedN = Long.toHexString(seed).toUpperCase();
        seedN = String.format("%8s",seedN).replace(" ", "0");
        return seedN;
    }
    
    public static BufferedImage rotateImage(BufferedImage src, int rotationAngle) {
        double theta = (Math.PI * 2) / 360 * rotationAngle;
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage dest;
        if (rotationAngle == 90 || rotationAngle == 270) {
            dest = new BufferedImage(src.getHeight(), src.getWidth(), src.getType());
        } else {
            dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        }

        Graphics2D graphics2D = dest.createGraphics();

        if (rotationAngle == 90) {
            graphics2D.translate((height - width) / 2, (height - width) / 2);
            graphics2D.rotate(theta, height / 2, width / 2);
        } else if (rotationAngle == 270) {
            graphics2D.translate((width - height) / 2, (width - height) / 2);
            graphics2D.rotate(theta, height / 2, width / 2);
        } else {
            graphics2D.translate(0, 0);
            graphics2D.rotate(theta, width / 2, height / 2);
        }
        graphics2D.drawRenderedImage(src, null);
        return dest;
    }

    public static void modAlpha(BufferedImage modMe, double modAmount) {
        for (int x = 0; x < modMe.getWidth(); x++) {          
            for (int y = 0; y < modMe.getHeight(); y++) {
                int argb = modMe.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;  //isolate alpha

                alpha *= modAmount;
                alpha &= 0xff;

                argb &= 0x00ffffff;
                argb |= (alpha << 24);
                modMe.setRGB(x, y, argb);            
            }
        }
    }

    public void drawTextOutline(Graphics G, String s, int x, int z, Color c1, Color c2) {
        G.setColor(c2);
        G.drawString(s, x-1,z);
        G.drawString(s, x+1,z);
        G.drawString(s, x,z+1);
        G.drawString(s, x,z-1);
        G.setColor(c1);
        G.drawString(s, x, z);
    }

    public void drawAngle(Graphics G, float fx, float fz, float ang) {
        G.setColor(new Color(0,0,0));
        int len = 25;
        int len2 = 20;
        float angd = 0.1f;
        float x = fx / M * N;
        float z = fz / M * N;
        G.drawLine((int)x, (int)z, (int)(x + len*Math.sin(ang) + 0.5), (int)(z + len*Math.cos(ang) + 0.5));
        G.drawLine((int)(x + len*Math.sin(ang) + 0.5), (int)(z + len*Math.cos(ang) + 0.5),
                   (int)(x + len2*Math.sin(ang-angd) + 0.5), (int)(z + len2*Math.cos(ang-angd) + 0.5));
        G.drawLine((int)(x + len*Math.sin(ang) + 0.5), (int)(z + len*Math.cos(ang) + 0.5),
                   (int)(x + len2*Math.sin(ang+angd) + 0.5), (int)(z + len2*Math.cos(ang+angd) + 0.5));
    }

    public void drawTeki(Graphics G, CaveGen g, Teki t, int x, int z) {
        int ox = x;
        int oz = z;
        try {
            Image im = getTeki(t);
            x += 20 - im.getWidth(null)/2;
            z += 20 - im.getHeight(null)/2;
            G.drawImage(im, x, z, null);
            if (t.fallType != 0 && !CaveGen.drawNoFallType) {
                G.setColor(colorsFT[t.fallType]);
                int d = 7;
                G.drawLine(ox+5+d,oz+5+d,ox-10+d,oz-10+d);
                G.drawLine(ox+9+d,oz+1+d,ox-6+d,oz-14+d);
                G.drawLine(ox+1+d,oz+9+d,ox-14+d,oz-6+d);
            }
            if (t.tekiName.equalsIgnoreCase("blackpom")) {
                String sls = CaveGen.specialCaveInfoName + CaveGen.sublevel;
                if (purple20.indexOf(","+sls+",") >= 0) {
                    drawTextOutline(G, "<20", x+2, z+5, spc, spc2);
                }
            }
            if (t.tekiName.equalsIgnoreCase("whitepom")) {
                String sls = CaveGen.specialCaveInfoName + CaveGen.sublevel;
                if (white20.indexOf(","+sls+",") >= 0) {
                    drawTextOutline(G, "<20", x+2, z+5, spc, spc2);
                }
            }
            if (t.tekiName.equalsIgnoreCase("blackman")) {
                String bmt = g.waterwraithTimer + "";
                drawTextOutline(G, "t"+bmt, x+2, z+5, spc, spc2);
            }
        } catch(Exception e) {}
        if (t.itemInside != null) {
            try {
                Image im = getItem(null, t.itemInside, CaveGen.region);
                G.drawImage(im, ox+10, oz+10, null);
            } catch (Exception e) {}
        }
    }

}
