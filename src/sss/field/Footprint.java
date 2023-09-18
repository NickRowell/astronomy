
package sss.field;

import java.io.*;

import sdss.footprint.SdssFootprintUtils;
import sss.field.drillfractions.DrillFraction;

/**
 *  TO DO - add SDSS support, i.e. load SDSS overlap area and install method to test if star
 *  is in SDSS area.
 *  Add isStarInRegion method that takes a star AND a footprint label so that basic object can be used to
 *  check if a star lies inside a footprint without having to load field areas.
 *
 * @author nickrowell
 */
public class Footprint {

    //+++ Data members for this class - array of solid angles for survey fields, given +++//
    //+++ the specified footprint, and a name for the footprint.                       +++//
    double[][] solidAngle = new double[900][2];

    //+++ Field centres +++//
    static double[][][] fieldCentre = new double[900][2][];

    //+++ Label for footprint. Initialise to null to identify basic objects +++//
    private String name = null;

    //+++ Basic Constructor - load field centres only +++//
    public Footprint() throws IOException{

        //+++ Load all field centres +++//
      	try{
            for(int f = 1; f<fieldCentre.length; f++){

                fieldCentre[f][0] = loadFieldCentre(f,"N"); //north
                fieldCentre[f][1] = loadFieldCentre(f,"S"); //south
            }
        }
	catch(IOException ioe){System.out.println("Error");}
    }

    //+++ Main contructor - also load all solid angles for given survey footprint +++//
    public Footprint(String footprint) throws Exception{

        //+++ Use basic constructor to load field centres +++//
        this();

        //+++ Check desired footprint prior to loading solid angles +++//
        if(!checkFootprint(footprint)) throw new Exception("Footprint does not exist.");

        name = footprint;

        //+++ Get drilling fractions for survey fields +++//
        DrillFraction drillFraction = new DrillFraction();

    	try{
            for(int f = 1; f<solidAngle.length; f++){

                //+++ Load solid angles, corrected by fraction of field not lost to drilling +++//
                this.solidAngle[f][0] = loadOmega(footprint,f,"N") * drillFraction.getDrillFraction(f,"N");
                this.solidAngle[f][1] = loadOmega(footprint,f,"S") * drillFraction.getDrillFraction(f,"S");

            }
        }
	catch(IOException ioe){System.out.println("Error");}

    }

    //+++ Data access methods +++//
    public String getName(){ return this.name;}

    @Override
    public String toString(){ return this.getName();}

    //+++ Method providing access to field centres +++//
    public double[] getFieldCentre(int field, String hemisphere){
           return fieldCentre[field][(hemisphere.equals("N") ? 0 : 1)];
    }
    public double getFieldCentreDec(int field, String hemisphere){
           return fieldCentre[field][(hemisphere.equals("N") ? 0 : 1)][1];
    }
    public double getFieldCentreRA(int field, String hemisphere){
           return fieldCentre[field][(hemisphere.equals("N") ? 0 : 1)][0];
    }

    //+++ Method providing access to solid angles +++//
    public double getSolidAngle(int field, String hemisphere){
        return solidAngle[field][(hemisphere.equals("N") ? 0 : 1)];
    }

    public static boolean checkFootprint(String footprint){
        
            //+++ Check desired footprint is included in survey models +++//
        if(footprint.equals("b10_GC20") ||
           footprint.equals("b20") ||
           footprint.equals("b30") ||
           footprint.equals("b40") ||
           footprint.equals("b50") ||
           footprint.equals("b60") ||
           footprint.equals("DR3") ||
           footprint.equals("DR4") ||
           footprint.equals("DR5") ||
           footprint.equals("DR6")){
            
            //+++ Footprint is recognised +++//
            return true;
        }

        else{ return false;}

    }



    //+++ Does a star lie in this footprint area? +++//
    public boolean isStarInRegion(WhiteDwarf star) throws IOException{

	//+++ Get galactic latitude and galactic centre distance of star +++//

	double mod_b  = Math.abs(Misc.galacticLatitude(Math.toRadians(star.ra),Math.toRadians(star.dec)));
	double GC = Misc.angSep(Math.toRadians(star.ra),Math.toRadians(star.dec),GalacticCoordinates.GCra,GalacticCoordinates.GCdec);

	if(this.getName().equals("b10_GC20")&&(mod_b>Math.toRadians(10.0))&&(GC>Math.toRadians(20.0))) return true;
	if(this.getName().equals("b20")&&(mod_b>Math.toRadians(20.0)))return true;
	if(this.getName().equals("b30")&&(mod_b>Math.toRadians(30.0))) return true;
        if(this.getName().equals("b40")&&(mod_b>Math.toRadians(40.0))) return true;
        if(this.getName().equals("b50")&&(mod_b>Math.toRadians(50.0))) return true;
	if(this.getName().equals("b60")&&(mod_b>Math.toRadians(60.0))) return true;
	if(this.getName().equals("DR3")&&(SdssFootprintUtils.isInSDSS(3,Math.toRadians(star.ra),Math.toRadians(star.dec)))) return true;
	if(this.getName().equals("DR4")&&(SdssFootprintUtils.isInSDSS(4,Math.toRadians(star.ra),Math.toRadians(star.dec)))) return true;
	if(this.getName().equals("DR5")&&(SdssFootprintUtils.isInSDSS(5,Math.toRadians(star.ra),Math.toRadians(star.dec)))) return true;
	if(this.getName().equals("DR6")&&(SdssFootprintUtils.isInSDSS(6,Math.toRadians(star.ra),Math.toRadians(star.dec)))) return true;

	return false;

    }




    
    //+++ Load solid angles into internal data array +++//
    private static double loadOmega(String footprint, int fieldNum, String hemi) throws IOException{

	footprint = (footprint.substring(0,2).equals("DR")) ? "SloanOverlap/"+footprint : "SurveyFieldAreas/"+footprint;

	//initialise file containing lookup table of mag limits:
	File limit = new File("/spare/SSS/Resources/SurveyVolume/"+footprint+"/solidAngles"+hemi+".txt");
      	BufferedReader in = new BufferedReader(new FileReader(limit));
	String data;
	//skip commented lines at start of file:
	while((data = in.readLine()).substring(0,1).equals("#")){}
	do{
	    if(Integer.parseInt(columns(data,1))==fieldNum){
		in.close();
		return Double.parseDouble(columns(data,2));
	    }
	}
	while((data = in.readLine())!=null);

	in.close();

	return 0.0;
    }

    //+++ Returns array of right ascension and declination coordinates for field centre +++//

    private static double[] loadFieldCentre(int fieldNum, String hemi) throws IOException{

	double coords[] = {9E9,9E9}; // Initialise coordinate array to flag values so that it's clear if call failed

	//initialise file containing lookup table of field centres:
	File limit = new File("/spare/SSS/Resources/LookupTables/FieldCentres/FieldCentres"+hemi+".txt");
      	BufferedReader in = new BufferedReader(new FileReader(limit));

	String data;

	//skip commented lines at start of file:
	while((data = in.readLine()).substring(0,1).equals("#")){}
	do{
	    if(Integer.parseInt(columns(data,1))==fieldNum){
		coords[0] = Double.parseDouble(columns(data,2));
		coords[1] = Double.parseDouble(columns(data,3));
	    }
	}
	while((data = in.readLine())!=null);

	in.close();

	return coords;
    }





}
