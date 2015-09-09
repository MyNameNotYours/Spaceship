
package spaceship;

import java.io.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

public class Spaceship extends JFrame implements Runnable {
    static final int WINDOW_WIDTH = 420;
    static final int WINDOW_HEIGHT = 445;
    final int XBORDER = 20;
    final int YBORDER = 20;
    final int YTITLE = 25;
    boolean animateFirstTime = true;
    int xsize = -1;
    int ysize = -1;
    Image image;
    Graphics2D g;

    sound zsound = null;
    sound bgSound = null;
    Image outerSpaceImage;

    boolean gameOver;
//variables for rocket.
    Image rocketImage;
    int rocketXPos;
    int rocketYPos;
    int rocketLife;
    boolean rocketRight;
    
    int rocketXSpeed;
    int rocketYSpeed;
    int rocketMaxSpeed;
//missiles for the rocket
    Missile missile[];
//score
    int score;
    int highScore;
    int numStars = 5;
    int starXPos[];
    int starYPos[];
    boolean starActive[];
    int starHit;
    

    static Spaceship frame;
    public static void main(String[] args) {
        frame = new Spaceship();
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public Spaceship() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.BUTTON1 == e.getButton()) {
                    //left button

// location of the cursor.
                    int xpos = e.getX();
                    int ypos = e.getY();
                    missile[Missile.currentIndex].active = true;
                    missile[Missile.currentIndex].xPos= rocketXPos;
                    missile[Missile.currentIndex].yPos = rocketYPos;
                    missile[Missile.currentIndex].missileRight = rocketRight;
                    Missile.currentIndex++;
                    if (Missile.currentIndex >= Missile.numMissile)
                        Missile.currentIndex = 0;
                        
                    

                }
                if (e.BUTTON3 == e.getButton()) {
                    //right button
                    reset();
                }
                repaint();
            }
        });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {

        repaint();
      }
    });

        addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.VK_UP == e.getKeyCode()) {
                    if(rocketYSpeed > rocketMaxSpeed * -1)
                        rocketYSpeed--;
                } else if (e.VK_DOWN == e.getKeyCode()) {
                    if(rocketYSpeed < rocketMaxSpeed)
                        rocketYSpeed++;
                } else if (e.VK_LEFT == e.getKeyCode()) {
                    if(rocketXSpeed > rocketMaxSpeed * -1)
                        rocketXSpeed--;
                } else if (e.VK_RIGHT == e.getKeyCode()) {
                    if(rocketXSpeed < rocketMaxSpeed)
                        rocketXSpeed++;
                }
                else if (e.VK_INSERT == e.getKeyCode()) {
                    zsound = new sound("ouch.wav");                    
                }
                repaint();
            }
        });
        init();
        start();
    }
    Thread relaxer;
////////////////////////////////////////////////////////////////////////////
    public void init() {
        requestFocus();
    }
////////////////////////////////////////////////////////////////////////////
    public void destroy() {
    }



////////////////////////////////////////////////////////////////////////////
    public void paint(Graphics gOld) {
        if (image == null || xsize != getSize().width || ysize != getSize().height) {
            xsize = getSize().width;
            ysize = getSize().height;
            image = createImage(xsize, ysize);
            g = (Graphics2D) image.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }       
//fill background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, xsize, ysize);

        int x[] = {getX(0), getX(getWidth2()), getX(getWidth2()), getX(0), getX(0)};
        int y[] = {getY(0), getY(0), getY(getHeight2()), getY(getHeight2()), getY(0)};
//fill border
        g.setColor(Color.black);
        g.fillPolygon(x, y, 4);
// draw border
        g.setColor(Color.red);
        g.drawPolyline(x, y, 5);     
        if (animateFirstTime) {
            gOld.drawImage(image, 0, 0, null);
            return;
        }
        if(gameOver)
            return;
        g.drawImage(outerSpaceImage,getX(0),getY(0),
                getWidth2(),getHeight2(),this);
        for(int index = 0; index < missile.length; index++)
        {
            if(missile[index].active)
            {
                g.setColor(Color.red);
                drawCircle(getX(missile[index].xPos),getYNormal(missile[index].yPos),90,.3,1.5);
            }
        }
        if(rocketRight)
        {
            drawRocket(rocketImage,getX(rocketXPos),getYNormal(rocketYPos),0.0,2.0,2.0 );
        }
        else
        {
            drawRocket(rocketImage,getX(rocketXPos),getYNormal(rocketYPos),0.0,-2.0,2.0 );
        }
        for(int index = 0; index < numStars; index++)
        {
            g.setColor(Color.yellow);
            if(starActive[index])
                drawCircle(getX(starXPos[index]),getYNormal(starYPos[index]),0,1.5,1.5);
        }
        g.setColor(Color.magenta);
        g.setFont(new Font("Impact",Font.BOLD,15));
        g.drawString("Score: " + score, 10, 45);
        g.setColor(Color.magenta);
        g.setFont(new Font("Impact",Font.BOLD,15));
        g.drawString("HighScore: " + highScore, 300, 45);
        g.setColor(Color.magenta);
        g.setFont(new Font("Impact",Font.BOLD,15));
        g.drawString("Lives: " + rocketLife, 150, 45);
          if(rocketLife == 0)
        {            
            g.setColor(Color.red);
            g.setFont(new Font("Impact",Font.BOLD,60));
            g.drawString("GAME OVER", getX(getWidth2()/6), getYNormal(getHeight2()/2));
        }            
        gOld.drawImage(image, 0, 0, null);
    }
////////////////////////////////////////////////////////////////////////////
    public void drawCircle(int xpos,int ypos,double rot,double xscale,double yscale)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.fillOval(-10,-10,20,20);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }
////////////////////////////////////////////////////////////////////////////
    public void drawMissile(int xpos,int ypos,double rot,double xscale,double yscale)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.fillRect(-10,-10,20,20);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }
////////////////////////////////////////////////////////////////////////////    
    public void drawRocket(Image image,int xpos,int ypos,double rot,double xscale,
            double yscale) {
        int width = rocketImage.getWidth(this);
        int height = rocketImage.getHeight(this);
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.drawImage(image,-width/2,-height/2,
        width,height,this);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }
////////////////////////////////////////////////////////////////////////////
// needed for     implement runnable
    public void run() {
        while (true) {
            animate();
            repaint();
            double seconds = 0.04;    //time that 1 frame takes.
            int miliseconds = (int) (1000.0 * seconds);
            try {
                Thread.sleep(miliseconds);
            } catch (InterruptedException e) {
            }
        }
    }
/////////////////////////////////////////////////////////////////////////
    public void reset() {

//init the location of the rocket to the center.
        rocketXPos = getWidth2()/2;
        rocketYPos = getHeight2()/2;
//rocket direction
        rocketRight = true;
//rocket speed.
        rocketXSpeed = 0;
        rocketYSpeed = 0;
        rocketMaxSpeed = 10;
//rocket lives
        rocketLife = 3;
//star position.
        starXPos = new int[numStars];
        starYPos = new int[numStars];
        starActive = new boolean[numStars];
        
        for(int index = 0; index < numStars; index++)
        {
        starXPos[index] = (int)(Math.random() * getWidth2());
        starYPos[index] = (int)(Math.random() * getHeight2()); 
        starActive[index] = true;
        }
//score stuff
        score = 0;
//missile stuff
        missile = new Missile[Missile.numMissile];
         for(int index = 0; index < missile.length; index++)
        {
            missile[index] = new Missile();
        }
        Missile.currentIndex = 0;
        starHit = -1;
        gameOver= false;
        
    }
/////////////////////////////////////////////////////////////////////////
    public void animate() {
        if (animateFirstTime) {
            animateFirstTime = false;
            if (xsize != getSize().width || ysize != getSize().height) {
                xsize = getSize().width;
                ysize = getSize().height;
            }
            readFile();
            outerSpaceImage = Toolkit.getDefaultToolkit().getImage("./outerSpace.jpg");
            rocketImage = Toolkit.getDefaultToolkit().getImage("./rocket.GIF");
            reset();
            
        }
            if(gameOver)
                return;
            if(rocketLife <= 0)
                gameOver = true;
//missile stuff
        for(int index = 0; index < missile.length; index++)
        {
            if(missile[index].active)
            {
                if(missile[index].missileRight)
                    missile[index].xPos += 5;
                else
                    missile[index].xPos -= 5;

                if(missile[index].xPos >= getWidth2() || missile[index].xPos <= 0)
                    missile[index].active = false;
            }
        }
//star speeds        
        for(int index = 0; index < numStars; index++)
        {
            starXPos[index] -= rocketXSpeed;
        }
        rocketYPos -= rocketYSpeed;
//stop rocket at edges of screen.
        if(rocketYPos >= getHeight2())
        {
            rocketYSpeed = 0;
            rocketYPos = getHeight2();
        }
        else if(rocketYPos <= 0)
        {
            rocketYSpeed = 0;
            rocketYPos = 0;
        }
        
        for(int index = 0; index < numStars; index++)
        {
            if(starXPos[index] > getWidth2())
            {
               starYPos[index] = (int)(Math.random() * getHeight2()); 
               starXPos[index] = 0; 
            }
            else if(starXPos[index] < 0)
            {
                starYPos[index] = (int)(Math.random() * getHeight2()); 
                starXPos[index] = getWidth2();  
            }
        }
        boolean hit = false;
        for (int index=0;index<numStars;index++)
            {    
                if(starActive[index])
                {
                    if (starXPos[index]-10 < rocketXPos && 
                        starXPos[index]+10 > rocketXPos &&
                        starYPos[index]-10 < rocketYPos &&
                        starYPos[index]+10 > rocketYPos)
                        {
                          hit = true; 
                          if(starHit != index)
                          {
                          starHit = index;
                          zsound = new sound("ouch.wav");
                          rocketLife--;
                          }
                        }
                }
            }
        for (int count=0;count<numStars;count++)
        {
        for (int index=0;index<missile.length;index++)
            {    
                if(missile[index].active && starActive[count])
                {
                    if (starXPos[count]-10 < missile[index].xPos && 
                        starXPos[count]+10 > missile[index].xPos &&
                        starYPos[count]-10 < missile[index].yPos &&
                        starYPos[count]+10 > missile[index].yPos)
                        {
                          missile[index].active = false;
                          starActive[count] = false;
                          score++;
                          if(score > highScore)
                              highScore = score;
                        }
                }
            }
        }
        if(!hit)
            starHit = -1;
        if(rocketXSpeed > 0)
            rocketRight = true;
        else if(rocketXSpeed < 0)
            rocketRight = false;
    }

////////////////////////////////////////////////////////////////////////////
    public void start() {
        if (relaxer == null) {
            relaxer = new Thread(this);
            relaxer.start();
        }
    }
////////////////////////////////////////////////////////////////////////////
    public void stop() {
        if (relaxer.isAlive()) {
            relaxer.stop();
        }
        relaxer = null;
    }
/////////////////////////////////////////////////////////////////////////
    public int getX(int x) {
        return (x + XBORDER);
    }

    public int getY(int y) {
        return (y + YBORDER + YTITLE);
    }

    public int getYNormal(int y) {
        return (-y + YBORDER + YTITLE + getHeight2());
    }
    
    
    public int getWidth2() {
        return (xsize - getX(0) - XBORDER);
    }

    public int getHeight2() {
        return (ysize - getY(0) - YBORDER);
    }
        public void readFile() {
        try {
            String inputfile = "info.txt";
            BufferedReader in = new BufferedReader(new FileReader(inputfile));
            String line = in.readLine();
            while (line != null) {
                String newLine = line.toLowerCase();
                if (newLine.startsWith("numstars"))
                {
                    String numStarsString = newLine.substring(9);
                    numStars = Integer.parseInt(numStarsString.trim());
                }
                if (newLine.startsWith("nummissiles"))
                {                  
                    String numMissileString = newLine.substring(12);  
                    Missile.numMissile = Integer.parseInt(numMissileString.trim());
                    
                }
                line = in.readLine();
            }
            in.close();
        } catch (IOException ioe) {
        }
    }
}

class sound implements Runnable {
    Thread myThread;
    File soundFile;
    public boolean donePlaying = false;
    sound(String _name)
    {
        soundFile = new File(_name);
        myThread = new Thread(this);
        myThread.start();
    }
    public void run()
    {
        try {
        AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
        AudioFormat format = ais.getFormat();
    //    System.out.println("Format: " + format);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine source = (SourceDataLine) AudioSystem.getLine(info);
        source.open(format);
        source.start();
        int read = 0;
        byte[] audioData = new byte[16384];
        while (read > -1){
            read = ais.read(audioData,0,audioData.length);
            if (read >= 0) {
                source.write(audioData,0,read);
            }
        }
        donePlaying = true;

        source.drain();
        source.close();
        }
        catch (Exception exc) {
            System.out.println("error: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

}

class Missile{
    public static int currentIndex;
    public static int numMissile = 10;
    public int xPos;
    public int yPos;
    public boolean active;
    public boolean missileRight;    
    
    Missile()
    {        
        active = false;
        missileRight = false;
    }
}