package wd.wdlf.algoimpl;

import infra.os.OSChecker;
import numeric.data.DiscreteFunction1D;
import util.CharUtil;
import wd.wdlf.algo.BaseWdlf;

/**
 * ModelWDLF extends BaseWdlf and adds other types of quantities such as the 
 * mass function and mean mass & stellar age as a function of luminosity.
 */
public class ModelWDLF extends BaseWdlf {
    
    /** 
     * Average mass-luminosity relation. 
     */
    public DiscreteFunction1D mass;
    
    /** 
     * Average age-luminosity relation. 
     */
    public DiscreteFunction1D age;
    
    /** 
     * Mass function - not implemented. 
     */
//    public DiscreteFunction1D mass_func;
    
    /** 
     * Constructor used by synthetic WDLF generator. 
     */
    public ModelWDLF(double[] binCentres, double[] binWidths, double[] lf, double[] lf_STD,
                     double[] mass, double[] mass_STD, double[] age, double[] age_STD) {
        
        // Call constructor of parent class to perform checks on magnitude bins
        // and set up the luminosity function
        super(binCentres, binWidths, lf, lf_STD);
        
        this.mass = new DiscreteFunction1D(binCentres, binWidths, mass, mass_STD);
        this.age  = new DiscreteFunction1D(binCentres, binWidths, age, age_STD);
    }
        
    /** 
     * Get string representation of WDLF. 
     */
    @Override
    public String toString() {
         
        StringBuilder out = new StringBuilder();
        
        for(int i=0; i<density.size(); i++)
            out.append(density.getBinCentre(i)).append("\t")
                        .append(density.getBinWidth(i)).append("\t")
                        .append(density.getBinContents(i)).append("\t")
                        .append(density.getBinUncertainty(i)).append("\t")
                        .append(mass.getBinContents(i)).append("\t")
                        .append(mass.getBinUncertainty(i)).append("\t")
                        .append(age.getBinContents(i)).append("\t")
                        .append(age.getBinUncertainty(i)).append("\t")
                        .append(OSChecker.newline);
        
        return out.toString();
    }
    
    /** 
     * Gnuplot script that plots average age-luminosity relation.
     * Script contains both plotting commands and data. 
     */
    public String getAgeLuminosityRelationGnuplotScript() {
        
        double[] xrange = getXRange();
        // Sensible range on time axis
        double[] yrange = {0,13};
        
        String script = 
                "set terminal pngcairo enhanced color size 410,350"+OSChecker.newline +
                // Plot size
                //"set size square"+OSChecker.newline +
                // Configure all tics
                "set tics out"+OSChecker.newline+
                // Configure X axis
                "set xrange ["+(xrange[0]-1)+":"+(xrange[1]+1)+"]"+OSChecker.newline+
                "set xlabel \"{/"+OSChecker.getFont()+"=14 M_{bol}}\""+OSChecker.newline+
                "set mxtics 2"+OSChecker.newline+
                "set xtics 2 font \""+OSChecker.getFont()+",10\""+OSChecker.newline+
                // Configure Y axis
                "set yrange ["+yrange[0]+":"+yrange[1]+"]"+OSChecker.newline+
                "set ytics 1 font \""+OSChecker.getFont()+",10\""+OSChecker.newline+
                "set mytics 2"+OSChecker.newline+
                "set ylabel \"{/"+OSChecker.getFont()+"=14 Mean total age [Gyrs]}\" offset 0,0"+OSChecker.newline+
                // Line style for WDLF
                "set style line 1 lt 1 pt 5 ps 0.5  lc rgb \"black\" lw 1"+OSChecker.newline+
                // Width of bars on top & bottom of error bars
                "set bar 0.25"+OSChecker.newline+
                "set key top left Left"+OSChecker.newline+
                "set title '{/"+OSChecker.getFont()+"=10 Age-Luminosity relation}'"+OSChecker.newline+
                "plot 	'-' u 1:($7/1e9) w lp ls 1 notitle,\\"+OSChecker.newline+
                "  	'-' u 1:($7/1e9):(($7-$8)/1e9):(($7+$8)/1e9) w yerrorbars ls 1 notitle"+OSChecker.newline;
        
                // Now append WDLF to script
                StringBuilder output = new StringBuilder();
                
                // Need two copies of inline data due to two uses of special
                // filename '-' in gnuplot plot command
                output.append(toString()).append("e").append(OSChecker.newline);
                output.append(toString()).append("e").append(OSChecker.newline);
                
                script = script.concat(output.toString());
                
                return script;
    }
    
        
    /** 
     * Gnuplot script that plots average mass-luminosity relation.
     * Script contains both plotting commands and data. 
     */
    public String getMassLuminosityRelationGnuplotScript() {
        
        double[] xrange = getXRange();
        double[] yrange = {0,1.3};
        
        String script = 
                "set terminal pngcairo enhanced color size 410,350"+OSChecker.newline +
                // Plot size
                //"set size square"+OSChecker.newline +
                // Configure all tics
                "set tics out"+OSChecker.newline+
                // Configure X axis
                "set xrange ["+(xrange[0]-1)+":"+(xrange[1]+1)+"]"+OSChecker.newline+
                "set xlabel \"{/"+OSChecker.getFont()+"=14 M_{bol}}\""+OSChecker.newline+
                "set mxtics 2"+OSChecker.newline+
                "set xtics 2 font \""+OSChecker.getFont()+",10\""+OSChecker.newline+
                // Configure Y axis
                "set yrange ["+yrange[0]+":"+yrange[1]+"]"+OSChecker.newline+
                "set ytics 0.2 font \""+OSChecker.getFont()+",10\""+OSChecker.newline+
                "set mytics 2"+OSChecker.newline+
                "set ylabel \"{/"+OSChecker.getFont()+"=14 Mean WD mass [M_{"+CharUtil.solar+"}]}\" offset 0,0"+OSChecker.newline+
                // Line style for WDLF
                "set style line 1 lt 1 pt 5 ps 0.5  lc rgb \"black\" lw 1"+OSChecker.newline+
                // Width of bars on top & bottom of error bars
                "set bar 0.25"+OSChecker.newline+
                "set title '{/"+OSChecker.getFont()+"=10 Mass-Luminosity relation}'"+OSChecker.newline+           
                "plot 	'-' u 1:5 w lp ls 1 notitle,\\"+OSChecker.newline+
                "  	'-' u 1:5:($5-$6):($5+$6) w yerrorbars ls 1 notitle"+OSChecker.newline;
        
                // Now append WDLF to script
                StringBuilder output = new StringBuilder();
                
                // Need two copies of inline data due to two uses of special
                // filename '-' in gnuplot plot command
                output.append(toString()).append("e").append(OSChecker.newline);
                output.append(toString()).append("e").append(OSChecker.newline);
                
                script = script.concat(output.toString());
                
                return script;
    }
    
}
