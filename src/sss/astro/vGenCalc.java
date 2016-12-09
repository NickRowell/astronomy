/*

This program calculates the total enclosed generalised volume along the line of
sight for all survey fields, for many different values of the scale height.

Integration is performed over conical region with identical solid angle to survey field.
Each distance step defines an annulus, which is then divided into angular elements
and the integration proceeds over each, with each element having a separately measured
galactic plane distance.


The resulting functions can then be used when calculating V and Vmax for stars to
avoid the need to re-integrate the volume and density law.

This has applications in luminosity function and scaleheight function measurement.


*/


package Field;

import Constants.*;
import java.io.*;
import java.text.DecimalFormat;
import Jama.*;

/**
 *
 * @author nickrowell
 */
public class vGenCalc {

    public static void main(String args[]) throws IOException, Exception{


	//+++ Select survey footprint to be used. Solid angles of survey fields differ depending on footprint +++//
        Footprint footprint = new Footprint("b30");

	//+++ set up varying scaleheight volume array +++//

	double sh_min  = 100.0;
	double sh_max  = 1500.0;
	double sh_step = 10.0;

	int sh_N = (int)Math.rint((sh_max - sh_min)/sh_step) + 1; //+1 because both limits are included

	//+++ LOS integration parameters +++//
	double d_min = 0.0;
	double d_max = 3000.0;
	double d_step = 2.0;

	int d_N = (int)Math.rint((d_max - d_min)/d_step);

	//+++ Angular integration parameters +++//
	double azimuth_step = (2.0*Math.PI)*(10.0/360.0);   // 10 degree steps
	double polar_N = 20.0;                              //number of steps to divide polar integration into
	double polar_lim, polar_step;                       //set interactively for each field

	//+++ set up array to store Vgen(d) function within each field +++//
	double[][] vgen_d = new double[d_N][sh_N];

	//+++ set up other variables used in program +++//

	double r, theta, phi;              //spherical coordinates of volume elements in integration

	double r_N[][] = new double[3][1]; //position vector to above point

	Matrix r_Norm, r_Gal;              // Above vector in matrix form so can be transformed between coordinate systems

	double z, modZ;                    //Galactic plane distance

	double dV;			   //true volume of integration elements

	double fra, fdec;                  //Celestial coordinates of field centres

	Matrix NTR, RTN, RTG, GTR;         // Transformations between normal & equatorial systems, and equatorial
	                                   // & Galactic systems.


	DecimalFormat dp1 = new DecimalFormat("0.0");
	DecimalFormat dp2 = new DecimalFormat("0.00");
	DecimalFormat dp3 = new DecimalFormat("0.000");

	/* Loop over all fields and integrate generalised volume along line of sight,
	   calculating how much falls into each z bin. */

	for(int h = 0; h<2; h++){

	    String hemi       = (h==0) ? "S" : "N";
	    String hemisphere = (h==0) ? "South" : "North";

	    for(int currentField = 1; currentField < 900; currentField++){


		//+++ Restrict to field numbers existing in SSS +++//
		if(Misc.checkField(currentField,hemi)){

		//+++ New field; zero all elements of vgen_d +++//
		for(int d = 0; d<vgen_d.length; d++){
		    for(int sh = 0; sh<vgen_d[d].length; sh++){
			    vgen_d[d][sh] = 0.0;
		    }
		}

		/*
		  Parameters constant within each field:

		  1) survey field area and drill fraction -> opening angle for conical FOV

		  2) Galactic coordinates of field centre. Provides transformation to normal coordinates
		  for getting |z| for small volume elements.

		*/

		if(footprint.getSolidAngle(currentField, hemi)!=0.0){

		    System.out.println("Field = "+currentField+""+hemi+" in footprint "+footprint.toString());

		    //+++ get opening angle of equivalent conical FOV, in radians +++//
		    polar_lim = Math.acos(1.0 - footprint.getSolidAngle(currentField, hemi)/(2.0*Math.PI));

		    polar_step = polar_lim/polar_N;  //20 steps in polar integration

		    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
		    //++++++++++                                                   ++++++++++++//
		    //++++++++++ Calculate NTR (tranformation from normal to       ++++++++++++//
		    //++++++++++ equatorial triad) explicitly along line of sight  ++++++++++++//
		    //++++++++++ to field centre, and get inverse from transpose   ++++++++++++//

		    fra  = Math.toRadians(footprint.getFieldCentreRA(currentField,hemi));
		    fdec = Math.toRadians(footprint.getFieldCentreDec(currentField,hemi));


		    //+++ field centre lies along z direction in normal triad +++//

		    double[][] ntr = {{-1*Math.sin(fra), -1*Math.sin(fdec)*Math.cos(fra),Math.cos(fdec)*Math.cos(fra)},{Math.cos(fra),-1*Math.sin(fdec)*Math.sin(fra),Math.cos(fdec)*Math.sin(fra)},{0,Math.cos(fdec),Math.sin(fdec)}};

		    NTR = new Matrix(ntr);     // Transformation normal <--> equatorial
		    RTN = NTR.transpose();

		    //++++++++++                                                        +++++++//
		    //++++++++++ Set up complete transformation from galactic -> normal +++++++//
		    //++++++++++                                                        +++++++//

		    RTG = RTN.times(GalacticCoordinates.NTG); //transformation from Galactic to Normal triad
		    GTR = RTG.transpose();

		    //can now get Galactic coordinates of volume elements from their coordinates relative
		    //to the Normal triad.

		    for(int d = 0; d<d_N; d++){

			//+++ translate distance step d into a physical distance of mid point +++//
			//+++ Use |z| of mid point to calculate generalised volume of element +++//

			r = (double)d*d_step + d_min + (d_step/2.0);

			double d_lower = (double)d*d_step + d_min;
			double d_upper = (double)d*d_step + d_min + d_step;


			//+++ Loop over angular elements within annulus +++//

			for(double polar = 0.0; (polar+(polar_step/2.0)) < polar_lim; polar += polar_step){

			    //polar component of spherical coordinates of mid point of element:
			    theta   = polar + (polar_step/2.0);


			    for(double azimuth = 0.0; (azimuth+(azimuth_step/2.0)) < 2.0*Math.PI; azimuth += azimuth_step){


				//...and azimuthal component:
				phi = azimuth + (azimuth_step/2.0);


				//+++ Transform coordinates of element centre back to Galactic system +++/

				//coomponents in spherical coordinates  in normal triad are (r, theta, phi)

				r_N[0][0] = r*Math.sin(theta)*Math.cos(phi);
				r_N[1][0] = r*Math.sin(theta)*Math.sin(phi);
				r_N[2][0] = r*Math.cos(theta);

				//position vector relative to normal triad
				r_Norm = new Matrix(r_N);

				//...transformed to Galactic triad
				r_Gal = GTR.times(r_Norm);

				//Galactic plane distance z is third component of this vector,
				//plus contribution from non-zero solar z distance:
				z = r_Gal.get(2,0) + Solar.Z_solar;

				//+++ Get |z| for the centre of this element +++//
				modZ = Math.abs(z);

				//Volume of current small region
				dV = (1.0/3.0)*(d_upper*d_upper*d_upper - d_lower*d_lower*d_lower)
				    *(Math.cos(polar) - Math.cos(polar+polar_step))*azimuth_step;


				//+++ add generalised volume contributions to accumulator array +++//

				for(int sh = 0; sh<sh_N; sh++){

				    //+++ Translate scaleheight step into physical scaleheight +++//

				    double H = (double)sh*sh_step + sh_min;

				    vgen_d[d][sh] += dV*Math.exp(-modZ/H);

				}

			    }
			}   //closes both angular element loops


			//+++ for cumulative volume totals, set initial value for next
			//+++ step equal to totals in previous step

			if((d+1)<d_N){        //skip on last integration step to avoid overshooting array d elements

			    for(int sh = 0; sh < sh_N; sh++){

				vgen_d[(d+1)][sh] = vgen_d[d][sh];

			    }
			}

		    }  //closes for(distance){}

		}  //closes if(solidAngle!=0.0)
		else{System.out.println("Field = "+currentField+""+hemi+" outside footprint area "+footprint);}

		//+++ Finished current field. If solid angle in this footprint is zero, array Vgen(d,SH) is still +++//
		//+++ loaded with zero values so can write these out in the same way as fields with solid angle   +++//

		String filestub = "/spare/SSS/Resources/SurveyVolume/Vgen_along_LOS/z_solar_"+(int)Solar.Z_solar+"pc/"+footprint.toString()+"/"+hemisphere+"/"+currentField+""+hemi+"/";

		//+++ Generate output files for current Vgen(d,SH) function. One for each scaleheight +++//
		File vgen_d_out;
		BufferedWriter out;

		for(int sh = 0; sh<sh_N; sh++){

		    //+++ Translate scaleheight step index to physical scaleheight +++//
		    double H = (double)sh*sh_step + sh_min;

		    vgen_d_out = new File(""+filestub+""+(int)H+"pc_rev");
		    out        = new BufferedWriter(new FileWriter(vgen_d_out));

		    //+++ File header +++//
		    out.write("# z_solar = "+Solar.Z_solar);
		    out.newLine();
		    out.write("# scaleheight = "+(int)H);
		    out.newLine();
		    out.write("#d\tenclosed volume");
		    out.newLine();

		    //+++ data +++//
		    out.write("0.0\t0.0");         //First line -> zero distance & enclosed volume
		    out.newLine();

		    for(int d = 0; d<d_N; d++){

			//+++ Distance of furthest edge of bin gives correct value for enclosed +++//
			//+++ volume point                                                      +++//

			double d_upper = (double)d*d_step + d_min + d_step;

			out.write(""+dp1.format(d_upper) + "\t" + dp2.format(vgen_d[d][sh]));
			out.newLine();
		    }

		    out.flush();

		}

		}      // closes if(checkField())
		else{System.out.println("Skipped field "+currentField+""+hemi);}

	    }          //closes for(int currentField = 1; currentField < 900; currentField++){

	}




    }









}
