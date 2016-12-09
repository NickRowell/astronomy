
package LuminosityFunction;

/********************************************************************************

This code implements LF decomposition method on all star catalogue. The steps it takes are:

1) Input -> Set of v_{tan} thresholds/ranges to be used
            Mean absolute magnitudes for each LF bin
            v_{tan} distribution as pdf, stored as a CubicSpline object for each field & population
            SSS parameters (survey field solid angles, magnitude limits, etc.)

2) For each LF bin, get mean and variance of absolute mags

3) Run through every survey field;

 3.1) Use magnitude limits and mean absolute mags to set magnitude-dependant distance limits
 3.2) Integrate generalized volume between these limits in narrow annuli. At each annulus, use the proper
      motion limits to set tangential velocity limits.
 3.3) Correct the volume of each annulus by the discovery fraction for each population, given the
      range of tangential velocities that pass the survey proper motion limits.
 3.4) Sum all annuli to get total 'marginalized volume' for each kinematic population in this field.
 3.5) Each volume in 3.4) contributes to a design matrix element alpha, beta or gamma. Sum these over all
      survey fields.

4) Run through catalogue of stars. Sum the number of stars that pass each successive v_{tan} cut

5) Now have matrix A and column vector N

         An = N

   recover density n by:

          (A^TA)n = (A^T)N

                n = (A^TA)^-1 (A^T) N

********************************************************************************/


import Jama.*;
import java.io.*;
import java.util.*;
import java.text.*;
import Star.*;
import Field.*;
import Survey.*;
import Kinematics.*;
import Constants.Disks;

class Decomposed{

    //+++ Parameters controlling measurement of LF +++//


    //+++ Desired sequence of lower v_{tan} limits +++//
    static double[] v_min = {30,50,80,120,200};

    //+++ Set whether discrete or overlapping tangential velocity bins are to be used +++//
    static boolean discrete = true;

    //+++ String array of labels for each kinematic population. Use   +++//
    //+++ this to set which populations are included in decomposition +++//
    static String[] population = {"ThinDisk","ThickDisk", "Halo"};

    static double[] totalDensity      = new double[population.length];
    static double[] totalDensitySigma = new double[population.length];

    //+++ Luminosity function resolution. Bin width must have corresponding mean absolute magnitudes +++//
    //+++ Per-magnitude densities are recovered by dividing by width of magnitude bin.               +++//
    static double bin_width = 0.5;
    static double Mbol_min = 1.5;
    static double Mbol_max = 19.0;

    public static void main(String arg[]) throws IOException, Exception{

        System.out.println("Started...");

	//+++ Get precise date and time of program run, for output file names and logging  +++//
        Date date = new Date();

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

        //+++ Get discovery fraction functions for each populations +++//
        DiscoveryFractions[] discoveryFractions  = {new DiscoveryFractions("ThinDisk"),
                                                    new DiscoveryFractions("ThickDisk"),
                                                    new DiscoveryFractions("Halo")};

        //+++ Array to store column vector N for each M_{bol} bin +++//
	double Ns[][] = new double[(int)((Mbol_max - Mbol_min)/bin_width)][v_min.length];

      	//+++ Array to store design matrix for each kinematic population +++//
        //+++ [magnitude][population][v_{tan} bin]
        double[][][] designMatrix = new double[Ns.length][3][v_min.length];

        //+++ Array to capture volumes calculated in method in Survey class +++..
        double[][] V = new double[3][v_min.length];

	//+++ Arrays to store mean absolute mags in given Mbol bin +++//
	double absmags[] = new double[8];

	//+++ loop over all survey fields and calculate max volumes as described in 3) above +++//
	System.out.println("Calculating design matrix elements");

        //+++ loop over all M_{bol} bins +++//
	for(int M=0; M<Ns.length; M++){

            System.out.println("magnitude "+xpxx.format((M*bin_width)+Mbol_min+(bin_width/2)));

            //+++ Get mean absolute magnitudes for stars in this bin, assuming H model +++//
            //+++ absmags[0,1,2,3,4->7] = [bj, r1, r2, i, sig b->i] +++//
	    absmags = PhotoPI.getMeanMags("0.5",((M*bin_width)+Mbol_min+(bin_width/2.0)),"DA","8.0");

            //+++ Now, loop over each survey and calculate marginalized vmax for a star with these magnitudes +++//
            //+++ and for each kinematic population.
            for(int s=0; s<survey.length; s++){

                //+++ Get marginalized vmax for survey, for each kinematic population and velocity range +++//
                V = survey[s].getMarginalizedVmax(absmags, discoveryFractions, v_min, discrete);

                //+++ Add to design matrix +++//
                for(int pop=0; pop<3; pop++)
                    for(int v=0; v<v_min.length; v++)
                        designMatrix[M][pop][v] += V[pop][v];
            }

        }

	//+++ Have finished calculating design matrix elements. Now calculate column vector N by
	//+++ counting stars in catalogue that pass each v_{min} threshold

	System.out.println("Constructing N from star catalogue");

	String stellarData;

        //+++ Track number of stars processed +++//
	int count = 0;

        //+++ Loop over each input catalogue +++//
        for (int s = 0; s < input.length; s++) {

            BufferedReader in = new BufferedReader(new FileReader(input[s]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (s == 0) ? 0 : 1;

            while ((stellarData = in.readLine()) != null) {

                WhiteDwarf star = new WhiteDwarf(stellarData, flag);

                //+++ Get synthetic model info from next six lines +++//
                String models[] = {in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine()};

                //+++ Check this star lies in footprint area set for surveys. It is possible that    +++//
                //+++ input WD candidates catalogues might be all-sky, and footprint region set here +++//
                if (FOOTPRINT.isStarInRegion(star)
                        && FOOTPRINT.getSolidAngle(star.f, star.hemi) != 0.0) {

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
                        double vt_upper = (v == (v_min.length - 1)) ? 1000 : v_min[v + 1];

                        //+++ Tangential velocity has to be greater than vt_lower for addition to bin  +++//
                        //+++ If discrete vt ranges are used, then vt must also be lower than vt_upper +++//
                        //+++ However, if discrete ranges are not used, first condition is sufficient. +++//
                        if (vtDA > vt_lower && ((vtDA < vt_upper && discrete) || !discrete)) { Ns[binDA][v] += star.fracH;}
                        if (vtDB > vt_lower && ((vtDB < vt_upper && discrete) || !discrete)) { Ns[binDB][v] += star.fracHe;}

                    }
                }
            }
        }



        //+++ Now have observations and design matrix. Solve via linear algebra techniques. +++//
        
	//+++ Output Luminosity function file +++//
	BufferedWriter out_LF = new BufferedWriter(new FileWriter(new File("/spare/SSS/LuminosityFunction/Decomposed/"+filename.format(date)+"_lf")));
        writeHeaders(out_LF, date);

	//+++ Write matrix algebra out to file  +++//
	BufferedWriter out_M = new BufferedWriter(new FileWriter(new File("/spare/SSS/LuminosityFunction/Decomposed/"+filename.format(date)+"_matrix")));

        //+++ Loop over magnitude bins +++//
        for (int m = 0; m < Ns.length; m++) {

            double mag = ((m * bin_width) + Mbol_min + (bin_width / 2));

            //+++ How many non-zero elements are there in observation vector N for this bin? +++//
            int nonZero = 0;
            for (int v = 0; v < v_min.length; v++) if (Ns[m][v] > 0) nonZero++;
            
            //+++ Insufficient data to solve least squares +++//
            if(nonZero < population.length)
            {
                writeFail(out_LF, out_M, mag, nonZero);
                continue;
            }

            //+++ There are enough data to solve equation set, providing pseudoinverse +++//
            //+++ of design matrix exists.                                             +++//

            //+++ Observation vector +++//
            Matrix N = new Matrix(nonZero, 1);

            //+++ Variance-covariance matrix for observations +++//
            Matrix M = new Matrix(nonZero, nonZero);

            //+++ Design matrix of survey volumes +++//
            Matrix A = new Matrix(nonZero, population.length);

            //+++ Decide which column of A to enter each desired population into. +++//
            //+++ Set by 'population' String array declared at start of code.     +++//
            int[] column = new int[population.length];
            for (int pop = 0; pop < population.length; pop++) {
                if (population[pop].equals("ThinDisk"))  column[pop] = 0;
                if (population[pop].equals("ThickDisk")) column[pop] = 1;
                if (population[pop].equals("Halo"))      column[pop] = 2;
            }

            //+++ Load values into new matrices. Skip rows with no stars +++//
            int row = 0;
            for (int v = 0; v < v_min.length; v++) {

                if(Ns[m][v]>0){

                    //+++ Load star counts into observation vector +++//
                    N.set(row, 0, Ns[m][v]);

                    //+++ Load variance to diagonal element of variance-covariance matrix +++//
                    M.set(row, row, Ns[m][v]);

                    //+++ If non-discrete v_tan ranges are used, then star counts are +++//
                    //+++ correlated between bins. Write values in covariance matrix. +++//
                    if(!discrete){
                        for(int i=0; i<=row; i++){
                            M.set(i, row, Ns[m][v]);
                            M.set(row, i, Ns[m][v]);
                        }
                    }

                    //+++ Load survey volumes to design matrix +++//
                    for(int pop = 0; pop < population.length; pop++) 
                        A.set(row, pop, designMatrix[m][column[pop]][v]);
                    
                    //+++ Increment row index +++//
                    row++;
                }
            }

            //+++ Write vector and matrix quantities out to file +++//
            writeMatrices(out_M, mag, nonZero, A, N, M);

            //+++ Get LSQ solution for density and covariance on fitted parameters +++//
            Matrix covariance, parameters;
            
            //+++ Get chi^2 sum of residuals +++//
            double chi2 = 0;
            int    dof  = 0;

            try {

                //+++ Invert variance-covariance matrix to get matrix of weights +++//
                Matrix W = M.inverse();

                Matrix ATWN = A.transpose().times(W.times(N));
                Matrix ATWA = A.transpose().times(W.times(A));

                covariance = ATWA.inverse();

                //+++ Solution via unconstrained least squares +++//
                //parameters = ATWA.solve(ATWN);

                //+++ Solution via non-negative least squares +++//
                parameters = NNLSSolver.solveNNLS(ATWA, ATWN);

                Matrix residuals = N.minus(A.times(parameters));

                chi2 = residuals.transpose().times(W).times(residuals).get(0, 0);

                //+++ Degrees of freedom - number of observations minus number of free parameters +++//
                dof = N.getRowDimension() - parameters.getRowDimension();
         



            }
            catch (RuntimeException re1) {
                out_LF.write("\n#" + xpxx.format(mag) + " - ATWA singular");
                out_M.write("Java indicates pseudoinverse of design matrix doesn't exist to within machine precision\n\n");
                continue;                    // skip to next magnitude bin
            }

            //+++ integrate LF to get total WD density +++//
            for (int pop = 0; pop < population.length; pop++) {

                //+++ Sum density in each magnitude bin +++//
                totalDensity[pop] += parameters.get(pop, 0);

                if (parameters.get(pop, 0) > 9E-9) {
                    //+++ Sum variances in each magnitude bin +++//
                    totalDensitySigma[pop] += covariance.get(pop, pop);
                }
            }

            //+++ Write luminosity function details out to file +++//
            writeLuminosityFunction(out_LF, mag, parameters, covariance, dof, chi2);

        }


        writeTotalDensity(out_LF);

        out_M.flush();
        out_LF.flush();

        out_M.close();
        out_LF.close();

        System.exit(0);
    }

    //+++ Put things here to keep main body of code tidy +++//
    static DecimalFormat xpxxxxxEx = new DecimalFormat("0.00000E0");
    static DecimalFormat xpxEx     = new DecimalFormat("0.0E0");
    static DecimalFormat xpxxxEx   = new DecimalFormat("0.000E0");
    static DecimalFormat xpxx      = new DecimalFormat("0.00");
    static DecimalFormat xpx       = new DecimalFormat("0.0");

    static DateFormat filename = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
    static DateFormat header   = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    static void writeHeaders(BufferedWriter out_LF, Date date) throws IOException{

        //+++ Write headers for output LF file +++//
        out_LF.write("# Date and time of run: " + header.format(date) + "\n");
        out_LF.write("# Sequence of v_{tan} thresholds  = " + v_min[0]);
        for (int v = 1; v < v_min.length; v++) { out_LF.write(", " + v_min[v]);}
        out_LF.write("\n# Scaleheight for thin disk = " + Disks.getThinDiskScaleheight() + "\n");
        out_LF.write("# Scaleheight for thick disk = " + Disks.getThickDiskScaleheight() + "\n");
        out_LF.write(discrete ? "# Discrete v_{tan} ranges\n": "# Overlapping v_{tan} ranges\n");
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

        out_LF.write("dof\treduced chi^2");

        out_LF.flush();

    }


    static void writeFail(BufferedWriter out_LF, BufferedWriter out_M, double mag, int nonZero) throws IOException{
    
        //+++ Only trivial solution is possible +++//
        if (nonZero == 0) {
            out_LF.write("\n#" + xpxx.format(mag) + " - No stars in magnitude bin. Only trivial solution possible.");
            out_M.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            out_M.write("\n\n" + xpxx.format(mag) + " bin has no stars and only trivial solution is possible.\n\n");
        }
        //+++ Equation set is underdetermined if there are more parameters than data points +++//
        else if (nonZero < population.length) {
            out_LF.write("\n#" + xpxx.format(mag) + " - Least squares under determined.");
            out_M.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            out_M.write("\n\n" + xpxx.format(mag) + " bin has insufficient data to constrain density.\n\n");
        }

    }

    public static void writeMatrices(BufferedWriter out_M, double mag, int nonZero, Matrix A, Matrix N, Matrix M) throws IOException{


        out_M.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        out_M.write("\n\n" + xpxx.format(mag) + " bin has " + nonZero + " data and can attempt solution.\n\n");
        out_M.write("A N M:\n\n\n");
        for (int pop = 0; pop < population.length; pop++) {
            out_M.write(population[pop] + "\t");
        }

        for (int r = 0; r < nonZero; r++) {
            out_M.newLine();
            for (int pop = 0; pop < A.getColumnDimension(); pop++) {
                out_M.write(xpxxxxxEx.format(A.get(r, pop)) + ",\t");
            }
            out_M.write("\t" + xpx.format(N.get(r, 0)) + "\t\t");
            for (int c = 0; c < M.getColumnDimension(); c++) {
                out_M.write(xpxxxEx.format(M.get(r, c)) + ",\t");
            }
        }
        out_M.write("\n\n");
        out_M.flush();

    }

    public static void writeLuminosityFunction(BufferedWriter out_LF, double mag, Matrix parameters, Matrix covariance, double dof, double chi2) throws IOException{

        out_LF.write("\n" + xpxx.format(mag));

        for (int pop = 0; pop < population.length; pop++) {
            out_LF.write("\t" + xpxxxEx.format((1.0/bin_width) * parameters.get(pop, 0))
                    + "\t" + xpxxxEx.format(Math.sqrt((1.0/bin_width) * covariance.get(pop, pop))));
        }

        //+++ Include covariance terms at this point. These can be negative, so need to be careful with sqrt +++//
        for (int pop = 0; pop < population.length; pop++) {
            for (int pop2 = pop + 1; pop2 < population.length; pop2++) {
                out_LF.write("\t" + xpxxxEx.format(Math.signum(covariance.get(pop, pop2)) * Math.sqrt(Math.abs((1.0/bin_width) * covariance.get(pop, pop2)))));
            }
        }

        //+++ Now write out dof and reduced chi-squared +++//
        if(dof==0)
            out_LF.write("\t" + dof + "\t" + (chi2));
        else
            out_LF.write("\t" + dof + "\t" + (chi2/(double)dof));
    }

    public static void writeTotalDensity(BufferedWriter out_LF) throws IOException{

        out_LF.write("\n# Total integrated density:");

        for(int p=0; p<population.length; p++){

            out_LF.write("\n# "+population[p] + ": "+totalDensity[p] + " +/- "+Math.sqrt(totalDensitySigma[p]));

        }


    }


}


