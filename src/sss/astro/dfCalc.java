/*

Take as input:

Galactic velocity dispersions and mean velocites relative to the sun
Equatorial coordinates of field centres

As of 01/06/2010 I have decided to remove the solar motion wrt LSR and avoid having to correct mean population
velocities to the LSR then back to solar frame. This is because the velocity moments published
for each population are generally relative in the solar frame, and it is confusing and pointless to
try and remove this effect then add it in again from a different source. The reflex solar motions obtained are
generally different anyway so it is not clear what correction would be applied if I was to use e.g. the Binney
& Spergel Solar motion for each population.

For each field:

Get transformation matrix for normal -> equatorial triad (galactic -> equatorial same for all fields)
Transform covariance matrix of peculiar velocities to normal coordinates
Transform mean reflex motion to normal coordinates
Marginalise (analytically) over radial velocity to leave the tangential velocity ellipse
Transform peculiar velocity

Integrate numerically over position angle in small steps of Vtan.
Output P(Vtan) CDF(Vtan) at 1kms^{-1} intervals.


*/

package sss.astro;


import java.io.*;
import java.text.DecimalFormat;
import Jama.*;
import constants.Galactic;

import java.util.Scanner;

/**
 *
 * @author nickrowell
 */
public class dfCalc {




    public static void main(String args[]) throws IOException{

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//++++++++++                                                   ++++++++++++//
	//++++++++++      Input files giving field centres             ++++++++++++//
	//++++++++++                                                   ++++++++++++//


	File FieldN = new File("/spare/SSS/Resources/LookupTables/FieldCentres/FieldCentresN.txt");
	File FieldS = new File("/spare/SSS/Resources/LookupTables/FieldCentres/FieldCentresS.txt");

	DecimalFormat format7 = new DecimalFormat("0.0000000");
	String centre; //for reading in lines from field centre files



	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//++++++++++                                                   ++++++++++++//
	//++++++++++      Input parameters for Vtan distribution       ++++++++++++//
	//++++++++++                                                   ++++++++++++//




	//+++ solar motion vector in Galactic frame +++//
	//Matrix solar = new Matrix(new double[][]{{Solar.U},{Solar.V},{Solar.W}});


	String pop="";

	Matrix reflex     = new Matrix(3,1);
	Matrix covariance = new Matrix(3,3);
	double sigdet=0;

	//loop over all populations:

	for(int p = 2; p<3; p++){


	    switch(p){
	    case 0:{

		//+++ Thin disk +++//

		pop = "ThinDisk";

		//------------------------------------------------------------------------------------//
		//  "The Palomar/MSU nearby star spectroscopic survey II" Hawley, Gizis & Reid 1996
		//  Taken from table 9, the 'complete dM' sample. Chosen over Reid et al 1995 ellipsoid
		//  because Hawley et al use a more sophisticated analysis technique

		//double sigmaU = 40.6;
		//double sigmaV = 26.7;
		//double sigmaW = 21.2;

		//double Umean  =  -9.1;
                //double Vmean  = -23.3;
                //double Wmean  =  -7.6;

		//------------------------------------------------------------------------------------//
		//  From Fuchs et al. 2009 study of M dwarfs in the Solar cylinder.
		//  Values taken from 0 - 100 pc bin that is least affected by problems associated with
		//  deprojection of proper motions. This is also the bin least contaminated by thick
                //  disk stars and corresponds to the distance range in which most of my WDs lie.

		double sigmaU = 32.4;
		double sigmaV = 23.0;
		double sigmaW = 18.1;

		double Umean  =  -8.62;
        double Vmean  = -20.04;
        double Wmean  =  -7.10;


		//mean reflex population velocity:
		reflex = new Matrix(new double[][]{{Umean},{Vmean},{Wmean}});

		//velocity vector of sun relative to population mean:
		//reflex = solar.minus(mean);

		//covariance matrix for population peculiar velocities:
		covariance = new Matrix(new double[][]{{sigmaU*sigmaU,0,0},
                                                       {0,sigmaV*sigmaV,0},
                                                       {0,0,sigmaW*sigmaW}});

		//determinant:
		sigdet = sigmaU*sigmaV*sigmaW*sigmaU*sigmaV*sigmaW;

		break;

	    }

	    case 1:{

		//+++ Thick disk +++//

		pop = "ThickDisk";

		//---------------------------------------------------------------------------------------//
		//  "Kinematics of metal poor stars in the Galaxy III"   Chiba & Beers 2000              //
		//   Taken from table 1 line 1, -0.6 < [Fe/H]  -0.8, a metallicity range expected to represent
		//   the thick disk according to Binney & Merrifield (Gal. Astronomy pp 655)
                //
                //   Note that Chiba & Beers correct their mean velocities to the LSR by adopting a value
                //   for the Solar motion. I've removed this correction here.

		double sigmaU = 50.0;
		double sigmaV = 56.0;
		double sigmaW = 34.0;

		double Umean  = -11.0;
		double Vmean  = -42.0;              
                double Wmean  = -12.0;

		//mean reflex population velocity:
		reflex = new Matrix(new double[][]{{Umean},{Vmean},{Wmean}});

		//velocity vector of sun relative to population mean:
		//reflex = solar.minus(mean);

		//covariance matrix for population peculiar velocities:
		covariance = new Matrix(new double[][]{{sigmaU*sigmaU,0,0},
                                                       {0,sigmaV*sigmaV,0},
                                                       {0,0,sigmaW*sigmaW}});

		//determinant:
		sigdet = sigmaU*sigmaV*sigmaW*sigmaU*sigmaV*sigmaW;

		break;

	    }

	    case 2:{

		//+++ Halo +++//

		pop = "Halo";

		//---------------------------------------------------------------------------------------//
		//  "Kinematics of metal poor stars in the Galaxy III"   Chiba & Beers 2000              //
		//   Taken from table 1 line 5, [Fe/H] <= -2.2, which they say 'likely represents a pure
		//   halo component'
                //
                //   Same correction applied as above.

		double sigmaU = 141.0;
		double sigmaV = 106.0;
		double sigmaW = 94.0;

		double Umean  = -26;            
		double Vmean  = -199;           
		double Wmean  = -12;	        

		//mean population velocity:
		reflex = new Matrix(new double[][]{{Umean},{Vmean},{Wmean}});

		//velocity vector of sun relative to population mean:
		//reflex = solar.minus(mean);

		//covariance matrix for population peculiar velocities:
		covariance = new Matrix(new double[][]{{sigmaU*sigmaU,0,0},
                                                       {0,sigmaV*sigmaV,0},
                                                       {0,0,sigmaW*sigmaW}});

		//determinant:
		sigdet = sigmaU*sigmaV*sigmaW*sigmaU*sigmaV*sigmaW;

		break;

	    }
            }




	//initialise input reader:
	BufferedReader input = new BufferedReader(new FileReader(FieldS));
	//controls output file name:
	String hemi = "S";


	for(int k =0; k<2;k++){

	//read through all field centres, calculating discovery fractions for each:
	//skip first line of field centres file:
	input.readLine();

	while((centre = input.readLine())!=null){


        Scanner scan = new Scanner(centre);

        int fieldNum = scan.nextInt();

        if((fieldNum>700 && k==0) || k==1){

	//+++ Initialise output file +++//
	BufferedWriter out = new BufferedWriter(
                             new FileWriter("../../Resources/LookupTables/VtanDistributions/"+pop+"/"+fieldNum+""+hemi));

	//+++ Write heading for output file +++//
	out.write("Vt\tp(Vt)\tp(vt<Vt)");
	out.newLine();
	out.flush();

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//++++++++++                                                        +++++++//
	//++++++++++ Get coordinates of field centre, i.e. line of          +++++++//
	//++++++++++ sight onto which velocity ellipsoid is to be projected +++++++//

	double RA = Math.toRadians(scan.nextDouble());
	double dec = Math.toRadians(scan.nextDouble());



	//++++++++++ Calculate NTR (tranformation from normal to       ++++++++++++//
	//++++++++++ equatorial triad) explicitly along line of sight  ++++++++++++//
	//++++++++++ to field centre, and get inverse from transpose   ++++++++++++//

	double[][] ntr = {{-1*Math.sin(RA), -1*Math.sin(dec)*Math.cos(RA),Math.cos(dec)*Math.cos(RA)},
                          {Math.cos(RA),-1*Math.sin(dec)*Math.sin(RA),Math.cos(dec)*Math.sin(RA)},
                          {0,Math.cos(dec),Math.sin(dec)}};
	Matrix NTR = new Matrix(ntr);
	Matrix RTN = NTR.transpose();


	//++++++++++ Set up complete transformation from galactic -> normal +++++++//
	//++++++++++                                                        +++++++//

	Matrix RTG = RTN.times(Galactic.NTG); //transformation from Galactic to Normal triad
	Matrix GTR = Galactic.GTN.times(NTR);


	//++++++++++ Transform relative velocity vector to normal triad and get +++++++//
	//++++++++++ components perpendicular to line of sight                  +++++++//

	Matrix reflex_normal = RTG.times(reflex);
	double sol_p = reflex_normal.get(0,0);
	double sol_q = reflex_normal.get(1,0);


	//++++++++++ Transform covariance tensor to normal triad and get                +++++++//
	//++++++++++ necessary matrix elements for marginalising over radial velocity   +++++++//

	Matrix covariance_normal = RTG.times(covariance.times(GTR));
	double Cpp = covariance_normal.get(0,0);
	double Cqq = covariance_normal.get(1,1);
	double Cpq = covariance_normal.get(1,0);

	double crr = (Cpp*Cqq - Cpq*Cpq)/sigdet;  //element from inverse-sigma matrix using matrix of cofactors


	// np = peculiar velocity parallel to equator, positive east,
	// nq = peculiar velocity perpendicular to equator, positive north.
	//
	// Bivariate probability density function of these peculiar velocity components given by:
	//
	// Fpq = (1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr))) *
	//       Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np*np   +   Cpp*nq*nq   -   2*Cpq*np*nq))
	//
	// This can be transformed to a probability density function for tangential velocity and
	// position angle using the transformations:
	//
	// np -> Vt*sin(position angle)      nq -> Vt*cos(position angle)
	//
	// The mean reflex motion is accounted for by adding a shift in each coordinate:
	//
	// np -> Vt*sin(position angle) + reflex_p      nq -> Vt*cos(position angle) + reflex_q
	//




	//++++++++++      Set up array to store pdf and cdf   ++++++++++++//
        int N_vt = 600;                           // Number of 1kms^{-1} steps to evaluate pdf & cdf over
	final int Vstep = 1;

	double[][] vt_df = new double[N_vt][3];   // [vt steps][Vt value, pdf, cdf]


	//+++ Calculate pdf(vt) by integrating out position angle. Must be done +++//
	//+++ numerically. use trapezium rule with extrapolation                +++//

	double np1,nq1,np2,nq2,np3,nq3;                  // peculiar velocity components stepped at three values

	for(double Vt = 0; Vt < (double)N_vt; Vt += (double)Vstep){          //loop over steps in Vtan

	    vt_df[(int)Vt][0] = Vt;

	    //for this value of vt, integrate position angle from 0 to 2*PI:

	    double h = Math.toRadians(0.005);        // Position angle step. Trapezium width = h*Vt

	    double T_h  = 0;
	    double T_2h = 0;


	    for(double pos = 0; pos<(2.0*Math.PI - h); pos+=2.0*h){

		np1 = Vt*Math.sin(pos) + sol_p;          // transform to tangential velocity and position angle,
		nq1 = Vt*Math.cos(pos) + sol_q;          // include mean reflex motion in each direction

		np2 = Vt*Math.sin(pos+h) + sol_p;
		nq2 = Vt*Math.cos(pos+h) + sol_q;

		np3 = Vt*Math.sin(pos+(2.0*h)) + sol_p;
		nq3 = Vt*Math.cos(pos+(2.0*h)) + sol_q;


		//contributions to T(h) sum:
		T_h += ((1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np1*np1 + Cpp*nq1*nq1 - 2*Cpq*np1*nq1))   +   (1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np2*np2 + Cpp*nq2*nq2 - 2*Cpq*np2*nq2)))*(h*Vt/2.0);

		T_h += ((1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np2*np2 + Cpp*nq2*nq2 - 2*Cpq*np2*nq2))   +   (1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np3*np3 + Cpp*nq3*nq3 - 2*Cpq*np3*nq3)))*(h*Vt/2.0);


		//contribution to T_2h sum:
		T_2h += ((1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np1*np1 + Cpp*nq1*nq1 - 2*Cpq*np1*nq1))   +   (1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr)))*Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np3*np3 + Cpp*nq3*nq3 - 2*Cpq*np3*nq3)))*(h*Vt);



	    }



	    //+++ add pdf(vt) point to array +++//

	    vt_df[(int)Vt][1] = (1.0/3.0) * (4.0 * T_h  - T_2h);   //pdf at this Vt point by Richardson's extrapolation

	}






	//+++ Calculate cdf from pdf +++//

	//first point (vt=0) equals zero:

	vt_df[0][2] = 0;

	//+++ for subsequent points, sum integrals of pdf over all previous steps:

	for(int Vt = 1; Vt < N_vt; Vt += Vstep){

	    vt_df[Vt][2] += (((double)Vstep)/2.0)*(vt_df[Vt][1] + vt_df[(Vt-1)][1]);  // area of immediately preceeding step...
	    vt_df[Vt][2] += vt_df[(Vt-1)][2];                               // ...all previous steps

	}






	//+++ finished calculations for this field,
	//+++ write out pdf and cdf for each point in vt_pdf and vt_cdf arrays


	// Write to output file:

	for(int i=0;i<N_vt;i++){

	    out.write(""+vt_df[i][0]+"\t"+format7.format(vt_df[i][1])+"\t"+format7.format(vt_df[i][2]));
	    out.newLine();

	}

	out.flush();
        out.close();

	// Sanity check - does cdf equal 1 at large Vt?
	System.out.println(""+pop+", "+hemi+" hemisphere, field "+fieldNum+". Normalisation = "+format7.format(vt_df[599][2]));


        }


	}


	//swap over to southern hemisphere:
	hemi = "N";
	input = new BufferedReader(new FileReader(FieldN));



	}


	}   //switch to next galactic component


    }


}
