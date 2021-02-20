import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

class PrettyAlign {

    Drawer d;

    final JFrame jfr = new JFrame("PrettyAlign");
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
    KeyListener keyListener;
    MouseAdapter mouseAdapter;
    Graphics G;

    int index = 0;

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<BufferedImage> rawImages = new ArrayList<BufferedImage>();
    ArrayList<BufferedImage> mapUnits = new ArrayList<BufferedImage>();
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    HashMap<String,String> presets = new HashMap<String,String>();

    final JFrame jfr2 = new JFrame("PrettyAlign2");
    Graphics G2;

    int x1, y1, x2, y2;
    boolean pt1 = true;

    void run() {
        System.out.println("pretty align");

        jfr.getContentPane().setLayout(null);
		jfr.setSize(800, 800);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jfr2.getContentPane().setLayout(null);
		jfr2.setSize(600, 600);
        jfr2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jfr2.setVisible(true);
        G2 = jfr2.getGraphics();

        keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
                //System.out.println("key pressed");
                if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET || e.getKeyCode() == KeyEvent.VK_BRACELEFT || (e.getKeyCode() == KeyEvent.VK_B && e.isShiftDown())) {
                    index-=1;
                    reset();
                    update();                  
                }
                if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET || e.getKeyCode() == KeyEvent.VK_BRACERIGHT || (e.getKeyCode() == KeyEvent.VK_N && e.isShiftDown())) {
                    index+=1;
                    reset();
                    update();                 
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    rawImages.set(index, Drawer.rotateImage(rawImages.get(index), 90));  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    x1+=1*(e.isShiftDown() ? 5 : 1);
                    x2+=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    x1-=1*(e.isShiftDown() ? 5 : 1);
                    x2-=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    y1+=1*(e.isShiftDown() ? 5 : 1);
                    y2+=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    y1-=1*(e.isShiftDown() ? 5 : 1);
                    y2-=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
                    y1-=1*(e.isShiftDown() ? 5 : 1);
                    y2+=1*(e.isShiftDown() ? 5 : 1);  
                    x1-=1*(e.isShiftDown() ? 5 : 1);
                    x2+=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_COMMA) {
                    y1+=1*(e.isShiftDown() ? 5 : 1);
                    y2-=1*(e.isShiftDown() ? 5 : 1);  
                    x1+=1*(e.isShiftDown() ? 5 : 1);
                    x2-=1*(e.isShiftDown() ? 5 : 1);  
                    update();     
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    try {
                        File outputFile = new File("files/pretty/"+names.get(index)+ ".png");
                        ImageIO.write(images.get(index), "png", outputFile);    
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        PrintWriter pout = new PrintWriter(new BufferedWriter(new FileWriter("files/pretty_align.txt",true)));
                        pout.write(names.get(index) + "," + x1 + "," + y1 + "," + x2 + "," + y2 + "\n");
                        pout.close();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }      
                }
                if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                    System.exit(0);
                }
            }
        };
        jfr.addKeyListener(keyListener);

        mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();
                if (me.getButton()==MouseEvent.BUTTON1) {
                    x1 = x;
                    y1 = y;
                } else {
                    x2 = x;
                    y2 = y;
                }
                pt1 = !pt1;
                System.out.println("click " + x + " " + y);
                update();
            }
        };
        jfr.addMouseListener(mouseAdapter);
        

        File ff = new File("files/pretty_raw/");
        for (File f: ff.listFiles()) {
            String name = f.getPath().replace("\\","/").split("/")[2].replace(".jpg","");
            names.add(name);
            try {
                rawImages.add(ImageIO.read(new File("files/pretty_raw/"+name+".jpg")));
                String loc = "files/gc/" + "arc/" + name + "/arc.d/texture.bti.png";
                mapUnits.add(ImageIO.read(new File(loc)));

                //File outputFile = new File("files/pretty/"+name+ ".png");
                //ImageIO.write(rawImages.get(rawImages.size()-1), "png", outputFile);
                images.add(ImageIO.read(new File("files/pretty/"+name+".png")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(name);
        }

        jfr.setVisible(true);
        G = jfr.getGraphics();

        index = 0;
        reset();
        update();
        

    }

    void reset() {
        x1=-1;y1=-1;x2=-1;y2=-1;pt1=true;
        try {
            BufferedReader br = new BufferedReader(new FileReader("files/pretty_align.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (names.get(index).equals(line.split(",")[0])) {
                    x1 = Integer.parseInt(line.split(",")[1]);
                    y1 = Integer.parseInt(line.split(",")[2]);
                    x2 = Integer.parseInt(line.split(",")[3]);
                    y2 = Integer.parseInt(line.split(",")[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void update() {
        // todo: do some scaling, account for large images??
        int xo = 150, yo = 180;
        int dX = mapUnits.get(index).getWidth() / 8;
        int dY = mapUnits.get(index).getHeight() / 8;
        System.out.println(names.get(index) + " " + dX + " " + dY);
        // draw raw image
        //System.out.println(G);
        G.clearRect(0, 0, 2000, 2000);
        G.drawImage(rawImages.get(index), xo, yo, null);

        // draw map unit at current pos if ready.
        if ((x1!=-1 || y1!=-1) && (x2!=-1||y2!=-1) && x2>x1 && y2>y1) {
            y2 = y1 + (x2-x1)*dY/dX;

            // todo transparent
            BufferedImage imx = toBufferedImage(mapUnits.get(index).getScaledInstance(x2-x1, y2-y1, Image.SCALE_DEFAULT));
            Drawer.modAlpha(imx, 0.4f);
            G.drawImage(imx, x1, y1, null);
        }

        // todo compute & save & draw image out if ready.
        // (includes pulling out bad colors)
        BufferedImage im = rawImages.get(index);

        BufferedImage img = new BufferedImage(x2-x1, y2-y1,
                                                        BufferedImage.TYPE_INT_ARGB);
        Graphics G3 = img.getGraphics();  
        G3.setColor(new Color(0,0,0,0));
        G3.fillRect(0, 0, img.getWidth(), img.getHeight());
        G3.drawImage(im, xo-x1, yo-y1, null);

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int p = img.getRGB(i, j);
                //get alpha
                int a = (p>>24) & 0xff;
                int r = (p>>16) & 0xff;
                int g = (p>>8) & 0xff;
                int b = p & 0xff;
                //System.out.println(a+" " + r + " " + g + " " + b);
                if (Math.abs(a-255)+Math.abs(r-76)+Math.abs(g-53)+Math.abs(b-150) < 60) {
                    img.setRGB(i, j, 0);
                }
            }
        }

        images.set(index, img);



        G2.clearRect(0, 0, 2000, 2000);
        int xx = 30, zz = 50;
        G2.drawImage(images.get(index), xx, zz, null);
        G2.setColor(new Color(255,0,255));
        int r = 6, h = images.get(index).getHeight(), w=images.get(index).getWidth();
        for (int i = 0; i <= dX; i++) {
            G2.fillOval(xx+w*i/dX-r/2, zz-r/2, r, r);
            G2.fillOval(xx+w*i/dX-r/2, zz+h-r/2, r, r);
        }
        for (int i = 0; i <= dY; i++) {
            G2.fillOval(xx-r/2,zz+h*i/dY-r/2, r, r);
            G2.fillOval(xx+w-r/2,zz+h*i/dY-r/2, r, r);
        }
        

    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    // Image getMapUnit(MapUnit m) throws Exception {
        
    
    //     BufferedImage im;
    //     try {
    //         im = ImageIO.read(new File(loc));
    //     } catch (Exception e) {
    //         System.out.println(loc);
    //         e.printStackTrace();
    //         im = new BufferedImage(m.dX*d.N, m.dZ*d.N, BufferedImage.TYPE_INT_RGB);
    //         im.getGraphics().setColor(new Color(255,0,144));
    //         im.getGraphics().drawRect(0,0,m.dX*d.N, m.dZ*d.N);
    //     }
    //     im = d.rotateImage(im, m.rotation * 90);
    //     Image im2 = im.getScaledInstance(m.dX*d.N, m.dZ*d.N, Image.SCALE_DEFAULT);

    //     return im2;
    // }


}