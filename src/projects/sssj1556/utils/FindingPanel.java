package projects.sssj1556.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import projects.sssj1556.astrometry.Coordinates2D;

/**
 * This class provides a small standalone program that loads a catalogue of
 * sources obtained from SuperCOSMOS, plots them into a frame and allows the
 * user to click on individual sources and retrieve the source data in a
 * convenient format.
 * 
 * @author nickrowell
 */
public class FindingPanel extends JPanel
implements MouseListener
{
    
    // Scale factor between equatorial coordinates and standard coordinates,
    // accurate at the tangent point (distortion will get larger further
    // from tangent point).
    //
    // Can derive this relation by considering a tangent point and reference
    // point on the equator.
    static double tp_1_degree = Math.sin(Math.toRadians(1.0))        / Math.cos(Math.toRadians(1.0));
    static double tp_1_arcmin = Math.sin(Math.toRadians(1.0/60.0))   / Math.cos(Math.toRadians(1.0/60.0));
    static double tp_1_arcsec = Math.sin(Math.toRadians(1.0/3600.0)) / Math.cos(Math.toRadians(1.0/3600.0));
    
    // Colours used to draw normal Sources and the selected Source
    static Color COLOUR_DEFAULT  = new Color(0x000000);
    static Color COLOUR_SELECTED = new Color(0xFF0000);
    
    // User configurable parameters
    
    /** File containing the catalogue. */
    File catalogue;
    /** RA of tangent point [radians]. */
    double ra_tp;
    /** Dec of tangent point [radians]. */
    double dec_tp;
    /** Angular width of finding chart [arcmins]. */
    double width;
    
    // Internal parameters
    
    /** List of all Source objects parsed from catalogue file. */
    List<Source> sources;
    /** Screen size of (square) finder chart visualisation. */
    private final int imsize = 512;
    
    /** Reference to source that is currently selected (closest one clicked to). */
    Source selected = null;
    
    public FindingPanel(File pcatalogue, double pra_tp, double pdec_tp, double pwidth)
    throws IOException
    {
        // User configurable parameters
        catalogue = pcatalogue;
        ra_tp     = pra_tp;
        dec_tp    = pdec_tp;
        width     = pwidth;
        
        sources = new LinkedList<Source>();
        
        // Parse sources from catalogue and store in list
        parseSources(sources, catalogue);
        
        // Calculates coordinates for sources when projected into the finding
        // chart window.
        projectSources();
        
        setPreferredSize(new Dimension(imsize, imsize));
        // Capture mouse clicks
        addMouseListener(this);        
    }
    
    private void projectSources()
    {
    
        double[] tp   = {ra_tp, dec_tp};
        
        for(Source source : sources)
        {
            // Project coordinates into image
            double[] star = {source.ra, source.dec};
            
            Coordinates2D etaXsi = Coordinates2D.getStandardCoordinates(star, tp);
            
            // Scale standard coordinates to units of arcminutes, at the tangent
            // point.
            double x = etaXsi.x / tp_1_arcmin;
            double y = etaXsi.y / tp_1_arcmin;
            
            // Now scale to pixel units and translate origin to top left. Flip
            // so that declination increases up and RA increases to the left,
            // which matches north-up east-left convention for finding charts.
            x = 256 - x * (imsize / width);
            y = 256 - y * (imsize / width);
            
            source.x = (int)Math.rint(x);
            source.y = (int)Math.rint(y);
        }
        
        
    }
    
    
    
    /**
     * Draws sources into finder chart panel.
     * 
     * Note if we included the projection calculation in the paint method, then
     * the projection could be interactively updated using mouse drags etc.
     * 
     * @param g 
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor(COLOUR_DEFAULT);
        for(Source source : sources)
        {
            drawEllipse(g, source.x, source.y, source.A, source.B, source.PA);
        }
        
        // Now overdraw selected cluster
        if(selected!=null)
        {
            g.setColor(COLOUR_SELECTED);
            drawEllipse(g, selected.x, selected.y, selected.A, selected.B, selected.PA);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        // Only respond to single clicks with left mouse button
        if((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
        {
            // Left mouse button clicked
            if(e.getClickCount() == 1)
            {
                // Coordinates of click. Origin is top left.
                int x = e.getX();
                int y = e.getY();
                
                selected = lookupSource(x,y);
                
                if(selected!=null)
                    System.out.println(selected.writeConstructor());
                else
                    System.out.println("No closest source found!");
                
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    
    
    
    /**
     * Get coordinates of points on circumference of confidence ellipse, for
     * drawing into image plane.
     * 
     * @param g
     * @param x
     * @param y
     * @param A
     * @param B
     * @param PA 
     */
    public void drawEllipse(Graphics g, int x, int y, double A, double B, double PA)
    {
        
        // Get major & minor axis vectors, in pixel units
        double A_x = -A/(60*1000) * Math.sin(Math.toRadians(PA)) * (imsize / width);
        double A_y = -A/(60*1000) * Math.cos(Math.toRadians(PA)) * (imsize / width);
        
        double B_x =  B/(60*1000) * Math.cos(Math.toRadians(PA)) * (imsize / width);
        double B_y = -B/(60*1000) * Math.sin(Math.toRadians(PA)) * (imsize / width);
        
        // Number of points to draw around circumference of ellipse
        int N = 36;
        // Corresponding angular step size
        float ang_step = 2.0f*(float)Math.PI/(float)N;
        
        // Loop round unit circle circumference, and transform points to the
        // ellipse frame
        int prev_X=0, prev_Y=0;
        
        for(int n=0; n<=N; n++)
        {
            // Translate index n to angle
            double ang = n * ang_step;
            
            double c_ang = Math.cos(ang);
            double s_ang = Math.sin(ang);
            
            // Rotate point at (c_ang, s_ang) back to image frame
            int X = (int)Math.rint(x + A_x*c_ang + B_x*s_ang);
            int Y = (int)Math.rint(y + A_y*c_ang + B_y*s_ang);
            
            if(n>0) g.drawLine(X, Y, prev_X, prev_Y);
            
            // Get pixel coordinates by adding mean position
            prev_X = X;
            prev_Y = Y;
            
        }
        
    }
    
    /**
     * This method finds the Source that the user clicked on.
     * @param x
     * @param y 
     */
    private Source lookupSource(int x, int y)
    {
        // Set initial large maximum value for click->source distance
        double min_dist2 = imsize*imsize;
        
        // Reference to closest source
        Source closest = null;
        
        // Now find closest source
        for(Source source : sources)
        {
            double dist2 = (x-source.x)*(x-source.x) + (y-source.y)*(y-source.y);
            
            if(dist2 < min_dist2)
            {
                closest   = source;
                min_dist2 = dist2;
            }
        }
        return closest;
    }
    
    
    
    /**
     * Parse multiple Source objects from a catalogue file.
     * @param sources
     * @param catalogue
     * @throws IOException 
     */
    static void parseSources(List<Source> sources, File catalogue) throws IOException
    {
        // Sanity checks on catalogue file...
        if(!catalogue.exists()) throw new RuntimeException("Catalogue file "+
                catalogue.getPath()+" not found!");
        
        if(!catalogue.canRead()) throw new RuntimeException("Cannot read "
                + "catalogue file "+catalogue.getPath()+"!");
        
        BufferedReader in = new BufferedReader(new FileReader(catalogue));
        
        // Attempt to parse each line as a new Source. Non-source lines, such
        // as header lines, will fail and return null. This way, we are robust
        // to varying number of header lines and un-marked comment lines.
        String line;
        while((line=in.readLine())!=null)
        {
            Source source = Source.parseSource(line);
            if(source!=null)
            {
//                System.out.println(source.toString());
                sources.add(source);
            }
        }
    }
    
    
    
    
    /**
     * Write program so that we can run it as a standalone command line tool.
     * The name of the catalogue file is the first and only argument.
     * @param args 
     */
    public static void main(String[] args) throws IOException
    {
        // args[0] = filename string
        //
        // ...rest of the user parameters will be set from the command line too.
        //
        
        File catalogue = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/catalogues/35546.ascii.txt");
        
        // Centre of finding chart
        double ra_tp  = Coordinates2D.hmsToRadians(16, 54,  1.7603);
        double dec_tp = Coordinates2D.dmsToRadians(62, 53, 54.113);
        
        // Width of finding chart [arcmin]
        double width = 6;
        
        FindingPanel finder = new FindingPanel(catalogue, ra_tp, dec_tp, width);
        
        // Create a JFrame to display the FindingPanel
        JFrame frame = new JFrame("SuperCOSMOS catalogue finder chart");
        // Exit cleanly on window close
        frame.addWindowListener(new WindowAdapter()
                          {
                              @Override
                              public void windowClosing(WindowEvent e)
                              {
                                  System.exit(0);
                              }
                          });
        
        // Add finder chart panel
        frame.add(finder, BorderLayout.CENTER);
        
        // Prepare main GUI form but don't display it yet: we launch PANGU
        // server via another small JFrame first.
        frame.pack();
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }
    
}

/**
 * Class represents individual sources parsed from the catalogue file.
 * @author nickrowell
 */
class Source
{
    /** Pixel coordinates in finding chart window, used to look up Source. */
    int x, y;
    
    /** Right ascension, radians. */
    double ra;
    /** Declination, radians. */
    double dec;
    /** Epoch, decimal years. */
    double epoch;
    /** Component of proper motion parallel to equator (mas/year). */
    double mu_acosd;
    /** Uncertainty. */
    double sigma_mu_acosd;
    /** Component of proper motion perpendicular to equator (mas/year). */
    double mu_d;
    /** Uncertainty. */
    double sigma_mu_d;
    /** Magnitudes. */
    double b, r1, r2, i;
    /** Area (0.67" pixels). */
    double area;
    /** Ellipse parameters A, B [milliarcseconds] and position angle [degrees]. */
    double A, B, PA;
    /** Class. */
    int sclass;
    /** Profile statistic. */
    double p;
    /** Blend number. */
    int blend;
    /** Quality number. */
    int q;
    /** Field number. */
    int field;
    
    @Override
    public String toString()
    {
        double[] hms = Coordinates2D.radsToHms(ra);
        double[] dms = Coordinates2D.radsToDms(dec);
        
        return (int)hms[0]+" "+(int)hms[1]+" "+hms[2]+" "+
                (dms[0]>0 ?  "+" : "")+(int)dms[0]+" "+(int)dms[1]+" "+dms[2]+
                " "+mu_acosd+" "+mu_d;
    }
    
    public String writeConstructor()
    {
        return "new Star("+ra+", "+dec+", new Epoch("+epoch+"), "+(mu_acosd/1000)
                +", "+(sigma_mu_acosd/1000)+", "+(mu_d/1000)+", "+(sigma_mu_d/1000)+");";
    }
    
    
    /**
     * Parses a single Source object from a String.
     * @param str
     * @return 
     */
    static Source parseSource(String str)
    {
        // Create empty Source
        Source source = new Source();
        
        // Open Scanner on Source string
        Scanner scan = new Scanner(str);
        
        // Now parse values from string...
        
        // Parse right ascension...
        double hour, min, sec;
        
        if(scan.hasNextDouble()) hour = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) min  = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) sec  = scan.nextDouble(); else return null;
        
        source.ra = Coordinates2D.hmsToRadians(hour, min, sec);
        
        // Parse declination
        double deg, amin, asec;
        if(scan.hasNextDouble()) deg  = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) amin = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) asec = scan.nextDouble(); else return null;
        
        source.dec = Coordinates2D.dmsToRadians(deg, amin, asec);
        
        if(scan.hasNextDouble()) source.epoch = scan.nextDouble(); else return null;
        
        if(scan.hasNextDouble()) source.mu_acosd = scan.nextDouble();       else return null;
        if(scan.hasNextDouble()) source.mu_d = scan.nextDouble();           else return null;
        if(scan.hasNextDouble()) source.sigma_mu_acosd = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.sigma_mu_d = scan.nextDouble();     else return null;
        
        if(scan.hasNextDouble()) source.b  = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.r1 = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.r2 = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.i  = scan.nextDouble(); else return null;
        
        if(scan.hasNextDouble()) source.area = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.A    = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.B    = scan.nextDouble(); else return null;
        if(scan.hasNextDouble()) source.PA   = scan.nextDouble(); else return null;
        
        if(scan.hasNextInt()) source.sclass = scan.nextInt(); else return null;
        if(scan.hasNextDouble()) source.p   = scan.nextDouble(); else return null;
        if(scan.hasNextInt()) source.blend  = scan.nextInt(); else return null;
        if(scan.hasNextInt()) source.q      = scan.nextInt(); else return null;
        if(scan.hasNextInt()) source.field  = scan.nextInt(); else return null;
        
        return source;
    }
}