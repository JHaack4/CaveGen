import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import javax.imageio.*;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.Document;

public class CaveViewer {

    static CaveViewer caveViewer;

    static boolean active = false;
    Thread caveGenThread = null;
    static boolean manipActive = false;
    static boolean manipKeepImages = false;
    int lastSSeed = 0;

    ArrayList<String> nameBuffer = new ArrayList<String>();
    ArrayList<BufferedImage> imageBuffer = new ArrayList<BufferedImage>();
    StringBuilder reportBuffer = new StringBuilder();
    int currentImage = 0;
    boolean firstImageDisplayedYet = false;
    static boolean guiOnly = false;
    boolean autoLaunch = false;

    static float cvMaxXSize = 1180, cvMaxYSize = 780;

    public static void main(String args[]) {
        caveViewer = new CaveViewer();
        caveViewer.run(args);
    }

    final JFrame jfr = new JFrame("CaveViewer");
    final JButton jbuttonRun = new JButton("Run");
    final JTextPane jtextReport = new JTextPane();
    KeyListener keyListener, keyListener2;

    final JFrame jfrView = new JFrame("");
    final NavigableImagePanel viewPanel = new NavigableImagePanel();

    final String fontfamily = "Arial, sans-serif";
    final String fontfamilyMono = "Monospaced";
    final Font font = new Font(fontfamily, Font.PLAIN, 12);
    final Font font10 = new Font(fontfamily, Font.PLAIN, 10);
    final Font fontMono = new Font(fontfamilyMono, Font.PLAIN, 12);
	final Font fontMono16 = new Font(fontfamilyMono, Font.PLAIN, 16);
    final Font fontMono10 = new Font(fontfamilyMono, Font.PLAIN, 10);

    class Arg {
        String name;
        JTextPane jtext = new JTextPane();
        JCheckBox jCheckBox = null;
        JTextField jTextField = null;
        JComboBox<String> jComboBox = null;

        Arg(String config, int x, int y) {
            String[] s = config.split(",");
            name = s[0].toLowerCase();
            jtext.setBounds(x+111, y-3, s[0].length() < 10 ? 85 : 115, 20);
            jtext.setText(s[0]);
            jtext.setFont(font);
            //jtext.setMargin(new Insets(1, 1, 1, 1));
            jtext.setEditable(false);
            jtext.setContentType("text/plain");
            jtext.setBackground(null);
            jtext.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            jtext.addKeyListener(keyListener);
            jfr.add(jtext);

            for (int i = 1; i < s.length; i++) {
                if (s[i].equals("#")) {
                    jCheckBox = new JCheckBox("",false);
                    jCheckBox.setBounds(x+93,y-2,20,20);
                    jCheckBox.addKeyListener(keyListener);
                    jfr.add(jCheckBox);
                } else if (s[i].equals("_")) {
                    jTextField = new JTextField();
                    jTextField.setBounds(x+10, y, 100, 18);
                    jTextField.setFont(font10);
                    jTextField.addKeyListener(keyListener2);
                    //jTextField.setMargin(new Insets(1, 1, 1, 1));
                    jfr.add(jTextField);
                } else if (s[i].equals("dropdown")) {
                    String[] options = s[++i].split("[|]+");
                    jComboBox = new JComboBox<String>(options);
                    jComboBox.setFont(font10);
                    jComboBox.setEditable(s[++i].equals("editable"));
                    jComboBox.setBounds(x+10, y, 100, 18);
                    jComboBox.addKeyListener(keyListener2);
                    jComboBox.getEditor().getEditorComponent().addKeyListener(keyListener2);
                    jfr.add(jComboBox);
                }
            }
        }
    }

    String caveArgs = "EC,SCx,FC,HoB,WFG,BK,SH,CoS,GK,SR,SC,CoC,HoH,DD,Story,PoD,CMAL,All,CH1,CH2,CH3,CH4,CH5,CH6,CH7,CH8,CH9,CH10,CH11,CH12,CH13,CH14,CH15,CH16,CH17,CH18,CH19,CH20,CH21,CH22,CH23,CH24,CH25,CH26,CH27,CH28,CH29,CH30,AT,IM,AD,GD,FT,WF,GdD,SC,AS,SS,CK,PoW,PoM,EA,DD,PP";
    String argString1 = "Output,dropdown,GUI only|None (no images)|Cave Folder|Seed Folder|Both Folders,x;Cave,dropdown," + caveArgs.replace(",", "|") + ",editable;Sublevel,dropdown,1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|All,x;Num,_;Seed,_";
    String argString2 = "AdditionalArgs,_;Region,dropdown,US|JPN|PAL,x;Judge,dropdown,|attk|pod|at|key|cmat|score|colossal,x;JudgeFilter,_;ConsecutiveSeeds,#";
    String argString3 = "NoImages,NoStats,NoPrints,NoWayPointGraph,ChallengeMode,StoryMode,DrawSpawnPoints,DrawAngles,DrawScores,DrawEnemyScores,DrawDoorLinks,DrawAllScores,DrawDoorIds,DrawWayPoints,DrawAllWayPoints,DrawTreasureGauge,DrawHoleProbs,DrawNoPlants,DrawNoObjects,DrawNoWaterBox,DrawNoBuriedItems,DrawNoItems,DrawNoTekis,DrawNoGates,DrawNoHoles,DrawNoFallType,CaveInfoReport,Aggregator,AggRooms,WriteMemo,ReadMemo,251";
    ArrayList<Arg> args = new ArrayList<Arg>();
    HashMap<String,Arg> argMap = new HashMap<String,Arg>();

    void run(String commandArgs[]) {

        active = true;

        jfr.getContentPane().setLayout(null);
		jfr.setSize(434, 555);
        jfr.setResizable(false);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET || e.getKeyCode() == KeyEvent.VK_BRACELEFT || (e.getKeyCode() == KeyEvent.VK_B && e.isShiftDown())) {
                    if (currentImage >= 0 && currentImage < nameBuffer.size() && Manip.thisManip != null) {
                        Manip.thisManip.nextStoryModeLevel(-1);
                    }                    
                }
                if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET || e.getKeyCode() == KeyEvent.VK_BRACERIGHT || (e.getKeyCode() == KeyEvent.VK_N && e.isShiftDown())) {
                    if (currentImage >= 0 && currentImage < nameBuffer.size() && Manip.thisManip != null) {
                        Manip.thisManip.nextStoryModeLevel(1);
                    }                    
                }
                if (e.getKeyCode() == KeyEvent.VK_K && e.isShiftDown()) {
                    if (Manip.thisManip != null)
                        Manip.thisManip.toggleCaptain();               
                }
				if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                    if (currentImage >= 0 && currentImage < nameBuffer.size()) {
                        String[] ss = nameBuffer.get(currentImage).split(" ");
                        if (ss.length == 2 && !ss[0].contains("Agg") && !ss[0].contains("Report")) {
                            CaveViewer.manipKeepImages = true;
                            String args2 = "cave " + ss[0] + " -noprints -drawpodangle "
                            + "-seed 0x" + Drawer.seedToString(new Seed().next_seed(Long.decode("0x"+ss[1]), -1));
                            CaveGen.main(args2.split(" "));
                            CaveViewer.manipKeepImages = false;
                            lastImg();
                        }
                    } 
                }
                if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
                    if (currentImage >= 0 && currentImage < nameBuffer.size()) {
                        String[] ss = nameBuffer.get(currentImage).split(" ");
                        if (ss.length == 2 && !ss[0].contains("Agg") && !ss[0].contains("Report")) {
                            CaveViewer.manipKeepImages = true;
                            String args2 = "cave " + ss[0] + " -noprints -drawpodangle "
                            + "-seed 0x" + Drawer.seedToString(new Seed().next_seed(Long.decode("0x"+ss[1]), 1));
                            CaveGen.main(args2.split(" "));
                            CaveViewer.manipKeepImages = false;
                            lastImg();
                        }
                    }                    
				}
				if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                    if (currentImage >= 0 && currentImage < nameBuffer.size()) {
                        String[] ss = nameBuffer.get(currentImage).split(" ");
                        if (ss.length == 2 && !ss[0].contains("Agg") && !ss[0].contains("Report")) {
                            CaveViewer.manipKeepImages = true;
                            String args2 = "cave " + ss[0] + " -noprints -drawpodangle "
                            + "-seed 0x" + Drawer.seedToString(new Seed().next_seed(Long.decode("0x"+ss[1]), -1));
                            CaveGen.main(args2.split(" "));
                            CaveViewer.manipKeepImages = false;
                            lastImg();
                        }
                    } 
                }
                if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                    System.exit(0);
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {

				}
				if (e.getKeyCode() == KeyEvent.VK_A) {

                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    lastSSeed = currentImage;
                    try {
                        long seed = -1;
                        if (nameBuffer.size() > 0) {
                            String[] sa = nameBuffer.get(currentImage).split(" ");
                            if (sa.length >= 2 && !sa[0].equals("Report:") && !sa[0].equals("Agg:")) {
                                seed = Long.decode("0x" + sa[1]); 
                            }
                        }
                        if (seed > -1) {
                            /*PrintWriter oWriter = new PrintWriter(new BufferedWriter(new FileWriter("files/seed_desired.txt")));
                            oWriter.write(Drawer.seedToString(seed) + "\n");
                            oWriter.close();*/
                            System.out.println("Desired: " + nameBuffer.get(currentImage) + "\t\t\t\t\t\t");
                            ImageIO.write(imageBuffer.get(currentImage), "png", new File("seed_chosen.png"));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
				}
				if (e.getKeyCode() == KeyEvent.VK_D) {

                }
                if (e.getKeyCode() == KeyEvent.VK_X) {
                    jfrView.setVisible(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runCaveGen();
                }
                if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_PERIOD) && e.isControlDown()) {
                    lastImg();
                }
                else if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_COMMA) && e.isControlDown()) {
                    firstImg();
                }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_PERIOD) {
                    nextImg(1);
                }
                else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_COMMA) {
                    prevImg(1);
                }
                else if (e.getKeyCode() == KeyEvent.VK_L) {
                    nextImg(10);
                }
                else if (e.getKeyCode() == KeyEvent.VK_J) {
                    prevImg(10);
                }
			}
        };
        keyListener2 = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runCaveGen();
                }
			}
        };
        jfr.addKeyListener(keyListener);
        jfrView.addKeyListener(keyListener);

        jbuttonRun.setFont(font);
		jbuttonRun.setBounds(10, 10, 400, 20);
        jfr.add(jbuttonRun);
        
        jbuttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				runCaveGen();
			}
		});
        
        jtextReport.setBounds(10, 375, 400, 135);
		jtextReport.setFont(font);
		jtextReport.setMargin(new Insets(3, 3, 3, 3));
		jtextReport.setEditable(false);
		JScrollPane jtextChatSP = new JScrollPane(jtextReport);
		jtextChatSP.setBounds(10, 375, 400, 135);
		jtextReport.setContentType("text/plain");
		jtextReport.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        jfr.add(jtextChatSP);
        
        jfrView.getContentPane().setLayout(null);
		jfrView.setSize(100, 100);
        jfrView.setResizable(true);

        NavigableImagePanel.navigationImageEnabled = false;
        viewPanel.setBounds(0, 0, 100, 100);
        jfrView.add(viewPanel);
        jfrView.setVisible(false);
        jfrView.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        jfrView.addComponentListener(new ComponentAdapter() {  
            public void componentResized(ComponentEvent evt) {
                viewPanel.setBounds(0,0,jfrView.getWidth()-14,jfrView.getHeight()-37);
            }
        });

        int y = 35;
        int x = 0;
        for (String s: argString1.split("[;]")) {
            Arg a = new Arg(s,x,y);
            y += 20;
            args.add(a);
            argMap.put(s.split(",")[0].toLowerCase(), a);
        }

        y = 35;
        x = 190;
        for (String s: argString2.split("[;]")) {
            Arg a = new Arg(s,x,y);
            y += 20;
            args.add(a);
            argMap.put(s.split(",")[0].toLowerCase(), a);
        }

        y = 145;
        x = -85;
        for (String s: argString3.split("[,]")) {
            Arg a = new Arg(s+",#",x,y);
            y += 20;
            args.add(a);
            argMap.put(s.split(",")[0].toLowerCase(), a);
            if (y > 345) {
                x += 134;
                y = 145;
            }
        }

        for (int i = 0; i < commandArgs.length; i++) {
            String a = commandArgs[i].toLowerCase();
            if (a.equals("run") || a.equals("-run")) {
                autoLaunch = true;
                continue;
            }
            if (i == 0) {
                argMap.get("output").jComboBox.setSelectedIndex(
                    a.equalsIgnoreCase("gui") ? 0 :
                    a.equalsIgnoreCase("none") ? 1 :
                    a.equalsIgnoreCase("cave") ? 2 :
                    a.equalsIgnoreCase("seed") ? 3 :
                    a.equalsIgnoreCase("both") ? 4 : 0
                );
            } else if (i == 1) {
                argMap.get("cave").jComboBox.setSelectedItem(a);
            } else {
                if (i == 2) {
                    try {
                        int in = Integer.parseInt(a);
                        argMap.get("sublevel").jComboBox.setSelectedItem(in==0?"All":in+"");
                        continue;
                    } catch (Exception e) {
                        argMap.get("sublevel").jComboBox.setSelectedItem("All");
                    }
                }
                if (a.equalsIgnoreCase("combine") || a.equalsIgnoreCase("vsavg")) {
                    argMap.get("additionalargs").jTextField.setText(argMap.get("additionalargs").jTextField.getText() + " -judge " + a);
                    continue;
                }
                if (!argMap.containsKey(a) && !argMap.containsKey(a.substring(1))) {
                    argMap.get("additionalargs").jTextField.setText(argMap.get("additionalargs").jTextField.getText() + " " + a);
                    continue;
                }
                if (a.charAt(0) == '-') a = a.substring(1);
                Arg arg = argMap.get(a);
                if (arg.jCheckBox != null) arg.jCheckBox.setSelected(true);
                if (arg.jTextField != null) arg.jTextField.setText(commandArgs[++i]);
                if (arg.jComboBox != null) {
                    String trg = commandArgs[++i];
                    boolean set = false;
                    for (int j = 0; j < arg.jComboBox.getItemCount(); j++) {
                        if ( arg.jComboBox.getItemAt(j).equalsIgnoreCase(trg)) {
                            arg.jComboBox.setSelectedIndex(j);
                            set = true;
                        }
                    }
                    if (!set)
                        arg.jComboBox.setSelectedItem(trg);
                }
            }
        }

        if (autoLaunch) {
            runCaveGen();
        }

        jfr.revalidate();
		jfr.repaint();
		jfr.setVisible(true);
    }

    void nextImg(int i) {
        currentImage += i;
        currentImage = Math.min(imageBuffer.size() - 1, currentImage);
        currentImage = Math.max(0, currentImage);
        dispCurrentImage();
    }

    void prevImg(int i) {
        currentImage -= i;
        currentImage = Math.max(0, currentImage);
        dispCurrentImage();
    }

    void firstImg() {
        currentImage = 0;
        dispCurrentImage();
    }

    void lastImg() {
        currentImage = imageBuffer.size() - 1;
        currentImage = Math.max(0, currentImage);
        dispCurrentImage();
    }

    void dispCurrentImage() {
        EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
                if (imageBuffer.size() > 0) {
                    try {
                        BufferedImage img = imageBuffer.get(currentImage);
                        viewPanel.setImage(img);
                        int w = img.getWidth();
                        int h = img.getHeight();
                        //System.out.println(w + " " + h);
                        float scale = Math.min(cvMaxYSize / h, Math.min(cvMaxXSize / w, 1));
                        jfrView.setSize((int)(w * scale) + 14, (int)(h * scale) + 37);
                        jfrView.setVisible(true);
                        jfrView.setTitle(nameBuffer.get(currentImage) + " (" + (currentImage+1) + "/" + nameBuffer.size() + ")");
                        viewPanel.setBounds(0, 0, (int)(w * scale), (int)(h * scale));
                        firstImageDisplayedYet = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void update() {
        if (!CaveViewer.active) return;
        final String s = reportBuffer.toString();
        reportBuffer = new StringBuilder();
        EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
                Document doc = jtextReport.getStyledDocument();
                try {
                    doc.insertString(doc.getLength(), s, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (!firstImageDisplayedYet) {
            prevImg(1);
        }
    }

    @SuppressWarnings( "deprecation" )
    void runCaveGen() {
        if (manipActive) return;
        try {
            if (caveGenThread != null && caveGenThread.isAlive()) {
                caveGenThread.stop();
                //Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        nameBuffer.clear();
        imageBuffer.clear();
        reportBuffer = new StringBuilder();
        jtextReport.setText("");
        currentImage = 0;
        firstImageDisplayedYet = false;

        StringBuilder s = new StringBuilder();
        for (Arg a: args) {
            if (a.name.equals("output")) {
                guiOnly = false;
                switch(a.jComboBox.getSelectedIndex()) {
                    case 0:
                        guiOnly = true;
                        s.append("cave ");
                        break;
                    case 1:
                        s.append("none ");
                        break;
                    case 2:
                        s.append("cave ");
                        break;
                    case 3:
                        s.append("seed ");
                        break;
                    case 4:
                        s.append("both ");
                } 
            } else if (a.name.equals("cave")) {
                s.append((String)(a.jComboBox.getSelectedItem()) + " ");
            } else if (a.name.equals("sublevel")) {
                if (((String)(a.jComboBox.getSelectedItem())).equalsIgnoreCase("all"))
                    s.append("0 ");
                else s.append((String)(a.jComboBox.getSelectedItem()) + " ");
            } else if (a.name.equals("additionalargs")) {
                s.append(a.jTextField.getText() + " ");
            } else {
                if (a.jCheckBox != null && a.jCheckBox.isSelected()) {
                    s.append("-" + a.name + " ");
                }
                else if (a.jTextField != null && !a.jTextField.getText().equals("")) {
                    s.append("-" + a.name + " " + a.jTextField.getText() + " ");
                }
                else if (a.jComboBox != null && !((String)(a.jComboBox.getSelectedItem())).equals("") ) {
                    s.append("-" + a.name + " " + (String)(a.jComboBox.getSelectedItem()) + " ");
                }
            }
        }
        final String ss = s.toString();
        caveGenThread = new Thread() {
            public void run() {
                try {
                    CaveGen.run(ss.split("[ ]+"));
                } catch (Exception e) {
                    e.printStackTrace();
                    reportBuffer.append("Crash log:\n");
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    CaveViewer.caveViewer.reportBuffer.append(errors.toString());
                }
            }
        };

        caveGenThread.start();
    }

}