package projects.sssj1556.figs;

import infra.io.Gnuplot;
import infra.os.OSChecker;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.data.ParallaxDataset;
import time.Epoch;
import astrometry.Ephemeris;

/**
 * This class reads the coordinates and observation epochs for each study 
 * object and generates a Gnuplot script for each that produces a nice figure
 * depicting the position of the Earth relative to the direction towards the
 * star at each epoch. This gives a clear impression of the quality of parallax
 * observations in terms of the achievable parallax factors.
 * 
 * @author nickrowell
 */
public class ParallaxFigs
{
    
    public static void main(String[] args) throws IOException
    {
        
        Ephemeris ephemeris = Ephemeris.getEphemeris(Ephemeris.Body.EARTH);
        
        // Loop over all stars
        
        for(ParallaxDataset dataset : ParallaxDataset.datasets)
        {
            // Print designation
            System.out.println("Star = "+dataset.designation);
            
            // Execute script
            BufferedImage plot = Gnuplot.executeScript(getFig2Script(dataset, ephemeris));
            
            // Save graph image to disk
            ImageIO.write(plot, "png", new File(dataset.designation+"_parallax_factor.png"));
            
        }
        
        
    }
    
    
    /**
     * This Gnuplot script plots a figure showing the position of the Earth in
     * it's orbit about the Sun at the time each parallax observation was taken,
     * in addition to an arrow showing the direction to the star.
     * @param dataset
     * @return 
     */
    private static String getFig1Script(ParallaxDataset dataset)
    {
        
        StringBuilder script = new StringBuilder();
        
        script.append("set terminal pngcairo enhanced color size 410,350").append(OSChecker.newline);
        script.append("set size square").append(OSChecker.newline);
        script.append("set title \'"+dataset.designation+"\'").append(OSChecker.newline);
        script.append("set key off").append(OSChecker.newline);
        script.append("set xrange [-2:2]").append(OSChecker.newline);
        script.append("unset xtics").append(OSChecker.newline);
        script.append("set yrange [-2:2]").append(OSChecker.newline);
        script.append("unset ytics").append(OSChecker.newline);
        
        // Draw a unit circle centred on the origin
        script.append("set object 1 circle at 0,0 size 1").append(OSChecker.newline);
        
        // Arrow from origin towards star right ascension
        double x = -Math.cos(dataset.target.ra);
        double y = -Math.sin(dataset.target.dec);  
        script.append("set arrow 1 from 0,0 to "+1.5*x+","+1.5*y).append(OSChecker.newline);
        
        // Get set of distinct years in which star was observed
        List<Integer> years = new LinkedList<Integer>();
        
        for(StackedImage image : dataset.slaves)
        {
            Integer year = new Integer(image.epoch.year);
            if(!years.contains(year)) years.add(year);
        }
        
        // Point type for plotting
        int pt = 3;
        
        script.append("plot");
        for(Integer year : years)
        {
            if(years.indexOf(year) != years.size()-1)
                script.append(" \'-\' w p pt "+pt+" title \'"+year+"\' ,\\").append(OSChecker.newline);
            else
                script.append(" \'-\' w p pt "+pt+" title \'"+year+"\'").append(OSChecker.newline);
        }
        // Write inline data
        for(Integer year : years)
        {
            for(StackedImage image : dataset.slaves)
            {
                if(image.epoch.year==year)
                {
                    // Convert epoch to a fraction of a year after March 20th
                    double angle = 2.0 * Math.PI * image.epoch.getFractionOfYearAfterSpringEquinox();
                    // Coordinates of Earth in orbit about Sun
                    script.append(Math.cos(angle)).append(" ").append(Math.sin(angle)).append(OSChecker.newline);
                }
            }
            script.append("e").append(OSChecker.newline);
            
            pt++;
        }
        
                
        return script.toString();
    }
    
    
    
    
    
    
    
    /**
     * This Gnuplot script plots a graph showing the parallax factor as a 
     * function of observation epoch for each star.
     * @param dataset
     * @return 
     */
    private static String getFig2Script(ParallaxDataset dataset, Ephemeris ephemeris)
    {
        
        StringBuilder script = new StringBuilder();
        
        script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
//        script.append("set size square").append(OSChecker.newline);
        script.append("set title \'"+dataset.designation+"\'").append(OSChecker.newline);
        script.append("set key top right out").append(OSChecker.newline);
        script.append("set xrange ["+(2453736.5-Epoch.MJD_CORRECTION)+":"+(2455197.5-Epoch.MJD_CORRECTION)+"]").append(OSChecker.newline);
        script.append("set xtics 500").append(OSChecker.newline);
        script.append("set xlabel \"MJD\"").append(OSChecker.newline);
        script.append("set ylabel \"Parallax Factor\"").append(OSChecker.newline);
        script.append("set arrow 1 from "+(2453736.5-Epoch.MJD_CORRECTION)+",0 "
                + "to "+(2455197.5-Epoch.MJD_CORRECTION)+",0 nohead lt 0").append(OSChecker.newline);
        
        script.append("plot");
        script.append(" \'-\' w l title \'F_{/Symbol d}\' ,\\").append(OSChecker.newline);
        script.append(" \'-\' w l title \'F_{/Symbol a}\' ,\\").append(OSChecker.newline);
        script.append(" \'-\' w p pt 5 notitle,\\").append(OSChecker.newline);
        script.append(" \'-\' w p pt 6 notitle").append(OSChecker.newline);
        
        // Write inline data
        
        for(double jd = 2453736.5; jd < 2455197.5; jd += 20)
        {
            // Get parallax factors
            double fd = ephemeris.getFd(jd, dataset.target.ra, dataset.target.dec);
            
            script.append((jd-Epoch.MJD_CORRECTION)+" "+fd).append(OSChecker.newline);
        }
        script.append("e").append(OSChecker.newline);
        
        for(double jd = 2453736.5; jd < 2455197.5; jd += 20)
        {

            // Get parallax factors
            double fa = ephemeris.getFa(jd, dataset.target.ra, dataset.target.dec);
            
            script.append((jd - Epoch.MJD_CORRECTION)+" "+fa).append(OSChecker.newline);
            
        }
        script.append("e").append(OSChecker.newline);
        
        for(StackedImage image : dataset.slaves)
        {
            double fd = ephemeris.getFd(image.epoch.jd(), dataset.target.ra, dataset.target.dec);
            script.append(image.epoch.mjd+" "+fd).append(OSChecker.newline);
        }
        script.append("e").append(OSChecker.newline);
        
        for(StackedImage image : dataset.slaves)
        {
            double fa = ephemeris.getFa(image.epoch.jd(), dataset.target.ra, dataset.target.dec);
            
            script.append(image.epoch.mjd+" "+fa).append(OSChecker.newline);
        }
        script.append("e").append(OSChecker.newline);    
        
                
        return script.toString();
    }
    
    
    
}
