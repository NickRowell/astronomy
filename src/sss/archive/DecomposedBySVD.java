
package Archive;


import Jama.*;
import java.io.*;
import java.util.*;
import java.text.*;
import Star.*;
import Field.*;
import Survey.*;
import Kinematics.*;
import Constants.Disks;

class DecomposedBySVD{




    public static void main(String arg[]) throws IOException, Exception{

	//+++ Desired sequence of lower v_{tan} limits +++//
	double[] v_min = {30,40,50,80,110,150,240};
        //double[] v_min = {30,160};

        //+++ Select survey footprint to be used +++//
        Footprint FOOTPRINT = new Footprint("b10_GC20");

        //+++ Load survey parameters  +++//
        LowMuSurvey lowMuSurvey = new LowMuSurvey(FOOTPRINT.toString());
        IntermediateMuSurvey intermediateMuSurvey = new IntermediateMuSurvey(FOOTPRINT.toString());
        HighMuSurvey highMuSurvey = new HighMuSurvey(FOOTPRINT.toString());
        Survey[] survey = {lowMuSurvey,intermediateMuSurvey,highMuSurvey};

	//+++ Get array of input files corrsponding to WD candidates from each survey +++//
	File input[] = {new File("/spare/SSS/Catalogues/LowPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/ExtraPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/HighPM/WDs_fit.txt")};

        //+++ String array of labels for each kinematic population. Use   +++//
        //+++ this to set which populations are included in decomposition +++//
        String[] population = {"ThinDisk","Halo"};

        //+++ Get discovery fraction functions for each populations +++//
        DiscoveryFractions[] discoveryFractions  = {new DiscoveryFractions("ThinDisk"),
                                                    new DiscoveryFractions("ThickDisk"),
                                                    new DiscoveryFractions("Halo")};

        //+++ Luminosity function resolution. Bin width must have corresponding mean absolute magnitudes +++//
    	double bin_width = 0.5;
	double Mbol_min = 1.5;
	double Mbol_max = 20.0;


	DecimalFormat xpxxxxxEx = new DecimalFormat("0.00000E0");
	DecimalFormat xpxEx  = new DecimalFormat("0.0E0");
	DecimalFormat xpxxxEx  = new DecimalFormat("0.000E0");

        DecimalFormat xpxx   = new DecimalFormat("0.00");
        DecimalFormat xpx    = new DecimalFormat("0.0");

        //+++ Array to store column vector N for each M_{bol} bin +++//
	double Ns[][] = new double[(int)((Mbol_max - Mbol_min)/bin_width)][v_min.length];

      	//+++ Array to store design matrix for each kinematic population +++//
        //+++
        //+++ First dimension is for bolometric magnitude bin
        //+++ Second is for each kinematic population and star count
        //+++ Third is for each tangential velocity threshold/range
        double[][][] designMatrix = new double[Ns.length][4][v_min.length];

	//+++ Arrays to store mean absolute mags in given Mbol bin +++//
	double absmags[] = new double[8];

	//+++ Get precise date and time of program run, for output file names and logging  +++//
        Date date = new Date();
        DateFormat filename = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
	DateFormat header   = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

	//+++ Output Luminosity function file +++//
        File wdlf = new File("/spare/SSS/LuminosityFunction/Decomposed/"+filename.format(date)+"_lf");
	BufferedWriter out_LF = new BufferedWriter(new FileWriter(wdlf));

	//+++ Write headers for output LF file +++//
        out_LF.write("# Date and time of run: " + header.format(date) + "\n");
        out_LF.write("# Sequence of v_{tan} thresholds  = " + v_min[0]);
        for (int v = 1; v < v_min.length; v++) { out_LF.write(", " + v_min[v]);}
        out_LF.write("\n# Scaleheight for thin disk = " + Disks.getThinDiskScaleheight() + "\n");
        out_LF.write("# Scaleheight for thick disk = " + Disks.getThickDiskScaleheight() + "\n");
        out_LF.write("# Densities are per unit M_{bol}, at half magnitude points, obtained\n");
        out_LF.write("# by doubling half-magnitude density.\n#\n#\n");

        //+++ Header for LF output table. Include covariance terms after density for each pop +++//
        out_LF.write("#     \t");
        for(int pop=0; pop<population.length; pop++)
            out_LF.write(population[pop]+" ("+pop+")\t\t\t");
        out_LF.write("Covariance terms");
        out_LF.write("\n#M_bol\t");
        for(int pop=0; pop<population.length; pop++)
            out_LF.write("LF\t\tsigLF\t\t");
        for(int pop=0; pop<population.length; pop++)
            for(int pop2=pop+1; pop2<population.length; pop2++)
                out_LF.write("sig_"+pop+""+pop2+"\t\t");

        out_LF.flush();

	//+++ loop over all survey fields and calculate max volumes as described in 3) above +++//
	System.out.println("Calculating design matrix elements");

        //+++ loop over all M_{bol} bins +++//
	for(int M=0; M<Ns.length; M++){

            double mag = ((M*bin_width)+Mbol_min+(bin_width/2));
            System.out.println("magnitude "+xpxx.format(mag));

            //+++ Get mean absolute magnitudes for stars in this bin, assuming H model +++//
            //+++ absmags[0,1,2,3,4->7] = [bj, r1, r2, i, sig b->i] +++//
	    absmags = PhotoPI.getMeanMags("0.5",((M*bin_width)+Mbol_min+(bin_width/2.0)),"DA","8.0");

            //+++ Now, loop over each survey and calculate marginalized vmax for a star with these magnitudes +++//
            //+++ and for each kinematic population.
            for(int s=0; s<survey.length; s++){

                //+++ Get marginalized vmax for survey, for each kinematic population and velocity range +++//
                double[][] V = survey[s].getMarginalizedVmax(absmags, discoveryFractions, v_min, true);

                //+++ Add to design matrix +++//
                for(int pop=0; pop<3; pop++)
                    for(int v=0; v<v_min.length; v++)
                        designMatrix[M][pop][v] += V[pop][v];
            }

        }     // Closes loop over magnitude bins



	//+++
	//+++ Have finished calculating design matrix elements. Now calculate column vector N by
	//+++ counting stars in catalogue that pass each v_{min} threshold
	//+++
	//+++

	System.out.println("Constructing N from star catalogue");


	String stellarData;

        //+++ Track number of stars processed +++//
	int count = 0;

        //+++ Loop over each input catalogue +++//
        for(int s=0; s<input.length; s++){

            BufferedReader in = new BufferedReader(new FileReader(input[s]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (s==0) ? 0:1;

            while ((stellarData = in.readLine()) != null) {

                WhiteDwarf star = new WhiteDwarf(stellarData, flag);

                //+++ Get synthetic model info from next six lines +++//
                String models[] = {in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine()};

                //+++ Check this star lies in footprint area set for surveys. It is possible that    +++//
                //+++ input WD candidates catalogues might be all-sky, and footprint region set here +++//
                if (FOOTPRINT.isStarInRegion(star) &&
                    FOOTPRINT.getSolidAngle(star.f,star.hemi) != 0.0) {


                //+++ Set distance to star using either fitted colours or values from literature +++//
                if (star.isKnownUCWD()) {
                    star.setPublishedAtmosphere();
                }
                else {
                    star.setModels(models);
                    star.setAtmosphereFractions();
                    star.setPhotoPI(models);
                }

                System.out.println("Star " + (count++));

                //+++ Tangential velocity from photometric distance and proper motion +++//
                double vtDA = 4.74 * star.dH * star.mu;
                double vtDB = 4.74 * star.dHe * star.mu;

                //+++ What M_{bol} bin does this object fall into? +++//
                int binDA = (int) Math.floor((star.mBolH - Mbol_min) / bin_width);
                int binDB = (int) Math.floor((star.mBolHe - Mbol_min) / bin_width);

                //+++ For now, chuck out stars that fall before the first M_{bol} bin +++//
                if (star.mBolH < Mbol_min) { continue;}


                //+++ check vt against each v_{tan} range +++//
                for (int v = 0; v < v_min.length; v++) {

                    //+++ Restrict v_tan limits +++//
                    double vt_lower = v_min[v];
                    //+++ On final vtan step, set large upper limit so  +++//
                    //+++ tangential velocity of stars is unconstrained +++//
                    double vt_upper = (v==(v_min.length-1)) ? 1000 : v_min[v+1];

                    //+++ For discrete ranges in v_{tan} +++//
                    if (vtDA > vt_lower && vtDA < vt_upper) { Ns[binDA][v] += star.fracH;}
                    if (vtDB > vt_lower && vtDB < vt_upper) { Ns[binDB][v] += star.fracHe;}

                    //+++ For cumulative star counts +++//
                    //if (vtDA > vt_lower) { Ns[binDA][v] += star.fracH;}
                    //if (vtDB > vt_lower) { Ns[binDB][v] += star.fracHe;}

                }

                

                }     // Closes isStarInRegion conditional

            }         // Closes while((stellarData=in.readLine())!=null)

        }             // Closes loop over survey catalogue



        /*
         *
         * Now have column vector N. Loop over each M_{bol} bin, construct matrix A and solve for n
         *
         */

	//+++ Write matrix algebra out to file  +++//
        File matrix = new File("/spare/SSS/LuminosityFunction/Decomposed/"+filename.format(date)+"_matrix");
	BufferedWriter out_M = new BufferedWriter(new FileWriter(matrix));

        //+++ Loop over magnitude bins. Using SVD, a solution can always be found. +++//
        //+++ A unique solution is obtained if one singular value is zero. Can use +++//
        //+++ this as a method to decide if decomposition has been succesful.      +++//

        for (int m = 0; m < Ns.length; m++) {

            double mag = ((m * bin_width) + Mbol_min + (bin_width / 2));

            //+++ How many non-zero elements are there in observation vector N for this bin? +++//
            int nonZero = 0;
            for (int v = 0; v < v_min.length; v++) {
                if (Ns[m][v] > 0) {
                    nonZero++;
                }
            }

            //+++ Only trivial solution is possible if all data are zero +++//
            if (nonZero == 0) {
                out_LF.write("\n#" + xpxx.format(mag) + " - No stars in magnitude bin. Only trivial solution possible.");
                out_M.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                out_M.write("\n\n" + xpxx.format(mag) + " bin has no stars and only trivial solution is possible.\n\n");
                continue;
            }
            //+++ Equation set is underdetermined if there are more parameters than data points +++//
            if (nonZero < population.length) {
                out_LF.write("\n#" + xpxx.format(mag) + " - Least squares under determined.");
                out_M.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                out_M.write("\n\n" + xpxx.format(mag) + " bin has insufficient data to constrain density.\n\n");
                continue;
            }

            //+++ Now construct design matrix +++//
           Matrix A = new Matrix(nonZero, population.length + 1);

            //+++ Decide which column of A to enter each desired population into. +++//
            //+++ Set by 'population' String array declared at start of code.     +++//
            int[] column = new int[population.length];
            for (int pop = 0; pop < population.length; pop++) {
                if (population[pop].equals("ThinDisk"))  column[pop] = 0;
                if (population[pop].equals("ThickDisk")) column[pop] = 1;
                if (population[pop].equals("Halo"))      column[pop] = 2;
            }

            //+++ Load values into matrix A. Skip rows with no stars +++//
            int row = 0;
            for (int v = 0; v < v_min.length; v++) {
                if(Ns[m][v]>0){

                    //+++ Load survey volumes into first columns +++//
                    for (int pop = 0; pop < population.length; pop++) {
                        A.set(row, pop, designMatrix[m][column[pop]][v]);
                    }

                    //+++ Add negative star count to complete homogenous equation set +++//
                    A.set(row, population.length, -1.0*Ns[m][v]);

                    //+++ Increment row index +++//
                    row++;
                }
            }

            //+++ Now take singular value decomposition of matrix A +++//
            
            // In the SVD, A = U*S*V^T
            //
            // The columns of V correspond to the eigenvectors of A^T*A
            // The columns of U correspond to the eigenvectors of A*A^T
            // The singular values can then be found by S = U^T*A*V
            //
            EigenvalueDecomposition V = new EigenvalueDecomposition(A.transpose().times(A));
            EigenvalueDecomposition U = new EigenvalueDecomposition(A.times(A.transpose()));

            //+++ Get singular values by S = UT*A*V +++//
            Matrix S = U.getV().transpose().times(A).times(V.getV());

            //+++ Sum columns so that column corresponding to smallest singular value can be found +++//
            double[] s = new double[S.getColumnDimension()];

            for(int i=0; i<s.length; i++){
                for(int j=0; j<S.getRowDimension(); j++){
                    s[i] += Math.abs(S.get(j, i));
                }
            }

            //+++ Get column index of smallest singular value +++//
            int lowest = 0;
            double sv  = s[0];

            for (int i = 1; i < s.length; i++) {
                if (s[i] < sv) {
                    lowest = i;
                    sv = s[i];
                }
            }

            //++ Column index of smallest singular value is same as column index +++//
            //+++ of solution vector in V.                                       +++//
            double rho_disk = V.getV().get(0, lowest);
            double rho_halo = V.getV().get(1, lowest);
            double one      = V.getV().get(2, lowest);

            System.out.println(xpxx.format(mag)+"\t"+(rho_disk/one)+"\t"+(rho_halo/one));


        }

        out_M.flush();
        out_LF.flush();

        System.exit(0);
    }
}

