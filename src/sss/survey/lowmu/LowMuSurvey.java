/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sss.survey.lowmu;

import Field.*;
import Star.*;
import sss.survey.dm.WhiteDwarf;
import Kinematics.DiscoveryFractions;

/**
 *
 * @author nickrowell
 */
public class LowMuSurvey extends Survey{

    //++++++++++++++++++++++++++++++++++++++++//
    //+++ Parameters unique to this survey +++//

    //+++ Reduced chi square limits and corrections +++//
    //double chi2LimitSouth = 1.7;
    //double chi2LimitNorth = 0.8;
    double chi2LimitSouth = 1.5;
    double chi2LimitNorth = 0.8;

    //+++ Obtained from cumulative distribution of synthetic proper motion counts, +++//
    //+++ with xi,eta errors overestimated by a factor of root 2.                  +++//
    double chi2DiscoveryFractionSouth = 0.98;
    double chi2DiscoveryFractionNorth = 0.83;

    //+++ List of fields to include in survey +++//
    boolean includeFields[][] = new boolean[900][2];

    //+++ Proper motion limits +++//
    LowMuLimits  lowMuLimits;
    double highMuLimit = 0.08;


    public LowMuSurvey(String FOOTPRINT) throws Exception{

        //+++ Load parameters constant between surveys via constructor for super class +++//
        super(FOOTPRINT);

        //++++++++++++++++++++++++++++++++++++++++++++++//
        //+++ Parameters that differ between surveys +++//


        //+++ Load list of rejected fields +++//
        for(int f = 1; f<includeFields.length; f++){
            for(int h=0; h<2; h++){

                //+++ Existing fields rejected based on contamination etc. +++//
                if(RejectField.contamination(f, ((h==0) ? "N":"S")) &&
                   RejectField.magellanicClouds(f, ((h==0) ? "N":"S")))
                    includeFields[f][h] = true;
                else
                    includeFields[f][h] = false;

                //+++ Fields that simply don't exist in SSS +++//
                if(!Misc.checkField(f, ((h==0) ? "N":"S")))
                    includeFields[f][h] = false;

            }
        }

        //+++ Load low proper motion limits +++//
        lowMuLimits = new LowMuLimits();

        //+++ Load high proper motion limits - NOW DEPRECATED by inclusion of intermediate proper motion catalogue +++//
        //highMuLimits = new HighMuLimits();
        
    }

    public boolean includeField(int field, String hemisphere){
        return includeFields[field][(hemisphere.equals("N") ? 0 : 1)];
    }


    @Override
    public double getChi2Limit(String hemi){ return ((hemi.equals("N") ? chi2LimitNorth : chi2LimitSouth));}
    @Override
    public double getChi2DiscoveryFraction(String hemi){
        return ((hemi.equals("N") ? chi2DiscoveryFractionNorth : chi2DiscoveryFractionSouth));
    }

    
    @Override
    public double getLowerProperMotionLimit(double mag, int field, String hemisphere){
        return lowMuLimits.getLowMuLimit(mag, field, hemisphere);
    }

    @Override
    public double getUpperProperMotionLimit(){
        //return highMuLimits.getHighMuLimit(field, hemisphere);
        return highMuLimit;
    }

    public double[][] getVMax(WhiteDwarf star, double distance){

        double[] vLower = this.getVLowerForAllKinematicPopulations(star, distance);
        double[] vHigher = this.getVHigherForAllKinematicPopulations(star, distance);

        return new double[][] {{vLower[0], vLower[0] + vHigher[0]},     // Thin disk V & V_{max}
                               {vLower[1], vLower[1] + vHigher[1]},     // Thick disk V & V_{max}
                               {vLower[2], vLower[2] + vHigher[2]}};    // Spheroid V & V_{max}
     }

    //+++ Method to calculate survey volume contained at distances smaller than that of the star +++//
    public double[] getVLowerForAllKinematicPopulations(WhiteDwarf star, double distance){

        double[] v   = {0.0,0.0,0.0};
        double[] v_i = {0.0,0.0,0.0};

        //+++ Get tangential velocity +++//
        double vt = 4.74 * distance * star.mu;

        //+++ Get absolute magnitudes +++//
        double B  = star.b  - 5.0 * Math.log10(distance) + 5;
        double R1 = star.r1 - 5.0 * Math.log10(distance) + 5;
        double R2 = star.r2 - 5.0 * Math.log10(distance) + 5;
        double I  = star.i  - 5.0 * Math.log10(distance) + 5;

        //+++ Max/min distances arising from magnitude limits +++//
	double dmin_m, dmax_m;

	//+++ Step size, volume element and apparent magnitude in numerical integration for vmax +++//
	double d_step, b, d_lower, d_upper, d_midStep;

	//+++ Loop over hemispheres and fields, and calculate total accessible volume +++//
	for(int h = 0; h<2; h++){

	    String HEMI = (h==0) ? "N" : "S";

	    for(int FIELD = 1; FIELD < 898; FIELD++){

		//+++ Check that current field both exists in SSS and is not on rejection lists +++//
		if(this.includeFields[FIELD][h]){

                    //+++ Reset volume for this field +++//
		    v_i[0]=v_i[1]=v_i[2]=0.0;

		    //+++ Calculate min and max distances based on magnitude +++//
		    dmax_m = Misc.min(Math.pow(10,(this.getFaintBLimit(FIELD, HEMI) - B  + 5)/5.0),
				       Math.pow(10,(this.getFaintR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
				       Math.pow(10,(this.getFaintR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
				       Math.pow(10,(this.getFaintILimit(FIELD, HEMI) - I  + 5)/5.0),
                                       distance);

		    dmin_m = Misc.max(Math.pow(10,(this.getBrightBLimit(FIELD, HEMI)  - B  + 5)/5.0),
				       Math.pow(10,(this.getBrightR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
				       Math.pow(10,(this.getBrightR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
				       Math.pow(10,(this.getBrightILimit(FIELD, HEMI)  - I  + 5)/5.0));


		    /*   Now, add up volume between these limits, and test whether each annulus is included
		     *   in survey by checking Vt and proper motion limit. Steps are such that change in apparent
		     *   magnitude is constant between steps.
		     *
		     */

		    d_step = dmin_m*(Math.pow(10,dm/5.0)-1.0);                      // initialise step size

		    for(double d_prime = dmin_m; d_prime < dmax_m; d_prime+=d_step){

			d_step  = d_prime*(Math.pow(10,dm/5.0)-1.0);                // Size of next step

			d_lower = d_prime;                                          // distance limits in this step
			d_upper = d_prime+d_step;

			if(d_upper>dmax_m) d_upper = dmax_m;                        // shift limits on final step

			d_midStep = Math.sqrt(d_lower*d_upper);	                    // mid-magnitude step

			b = B + 5*Math.log10(d_midStep) - 5;                        // apparent magnitude at midpoint


			//+++ Does star pass proper motion limits at mid-point of current step? +++//

  			if(((vt/(4.74*d_midStep))> this.getLowerProperMotionLimit(b, FIELD, HEMI))&&
  			   ((vt/(4.74*d_midStep))<=this.getUpperProperMotionLimit())){


                            //+++ Thin disk volume in this distance element +++//
                            v_i[0] += this.thinDisk.getVGen(d_upper, FIELD, HEMI) - this.thinDisk.getVGen(d_lower, FIELD, HEMI);

                            //+++ Thick disk volume in this distance element +++//
                            v_i[1] += this.thickDisk.getVGen(d_upper, FIELD, HEMI) - this.thickDisk.getVGen(d_lower, FIELD, HEMI);


                            v_i[2] = (this.footprint.getSolidAngle(FIELD, HEMI)/3.0) * (d_upper*d_upper*d_upper - d_lower*d_lower*d_lower);

			}
		    }

		    /*  Finished integration for generalized volume in this field.
		     *  Add generalized volume for this field to running total for entire survey.
		     *
		     */

		    v[0] += v_i[0];
		    v[1] += v_i[1];
		    v[2] += v_i[2];

		}

	    }
	}

	return v;

    }

    //+++ Method to calculate survey volume contained at distances smaller than that of the star +++//
    public double[] getVHigherForAllKinematicPopulations(WhiteDwarf star, double distance){

        double[] v   = {0.0,0.0,0.0};
        double[] v_i = {0.0,0.0,0.0};

        //+++ Get tangential velocity +++//
        double vt = 4.74 * distance * star.mu;

        //+++ Get absolute magnitudes +++//
        double B  = star.b  - 5.0 * Math.log10(distance) + 5;
        double R1 = star.r1 - 5.0 * Math.log10(distance) + 5;
        double R2 = star.r2 - 5.0 * Math.log10(distance) + 5;
        double I  = star.i  - 5.0 * Math.log10(distance) + 5;

        //+++ Max/min distances arising from magnitude limits +++//
	double dmin_m, dmax_m;

	//+++ Step size, volume element and apparent magnitude in numerical integration for vmax +++//
	double d_step, b, d_lower, d_upper, d_midStep;

	//+++ Loop over hemispheres and fields, and calculate total accessible volume +++//
	for(int h = 0; h<2; h++){

	    String HEMI = (h==0) ? "N" : "S";

	    for(int FIELD = 1; FIELD < 898; FIELD++){

		//+++ Check that current field both exists in SSS and is not on rejection lists +++//
		if(this.includeFields[FIELD][h]){

                    //+++ Reset volume for this field +++//
		    v_i[0]=v_i[1]=v_i[2]=0.0;

		    //+++ Calculate min and max distances based on magnitude +++//
		    dmax_m = Misc.min(Math.pow(10,(this.getFaintBLimit(FIELD, HEMI) - B  + 5)/5.0),
				       Math.pow(10,(this.getFaintR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
				       Math.pow(10,(this.getFaintR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
				       Math.pow(10,(this.getFaintILimit(FIELD, HEMI) - I  + 5)/5.0));

		    dmin_m = Misc.max(Math.pow(10,(this.getBrightBLimit(FIELD, HEMI)  - B  + 5)/5.0),
				       Math.pow(10,(this.getBrightR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
				       Math.pow(10,(this.getBrightR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
				       Math.pow(10,(this.getBrightILimit(FIELD, HEMI)  - I  + 5)/5.0),
                                       distance);


		    /*   Now, add up volume between these limits, and test whether each annulus is included
		     *   in survey by checking Vt and proper motion limit. Steps are such that change in apparent
		     *   magnitude is constant between steps.
		     *
		     */

		    d_step = dmin_m*(Math.pow(10,dm/5.0)-1.0);                      // initialise step size

		    for(double d_prime = dmin_m; d_prime < dmax_m; d_prime+=d_step){

			d_step  = d_prime*(Math.pow(10,dm/5.0)-1.0);                // Size of next step

			d_lower = d_prime;                                          // distance limits in this step
			d_upper = d_prime+d_step;

			if(d_upper>dmax_m) d_upper = dmax_m;                        // shift limits on final step

			d_midStep = Math.sqrt(d_lower*d_upper);	                    // mid-magnitude step

			b = B + 5*Math.log10(d_midStep) - 5;                        // apparent magnitude at midpoint


			//+++ Does star pass proper motion limits at mid-point of current step? +++//

  			if(((vt/(4.74*d_midStep))> this.getLowerProperMotionLimit(b, FIELD, HEMI))&&
  			   ((vt/(4.74*d_midStep))<=this.getUpperProperMotionLimit())){

                            //+++ Thin disk volume in this distance element +++//
                            v_i[0] += this.thinDisk.getVGen(d_upper, FIELD, HEMI) - this.thinDisk.getVGen(d_lower, FIELD, HEMI);

                            //+++ Thick disk volume in this distance element +++//
                            v_i[1] += this.thickDisk.getVGen(d_upper, FIELD, HEMI) - this.thickDisk.getVGen(d_lower, FIELD, HEMI);


                            v_i[2] = (this.footprint.getSolidAngle(FIELD, HEMI)/3.0) * (d_upper*d_upper*d_upper - d_lower*d_lower*d_lower);

			}
		    }

		    /*  Finished integration for generalized volume in this field.
		     *  Add generalized volume for this field to running total for entire survey.
		     *
		     */

		    v[0] += v_i[0];
		    v[1] += v_i[1];
		    v[2] += v_i[2];

		}

	    }
	}

	return v;

    }
    /**
     * Marginalized Vmax method calculates survey volume for e.g. thin disk stars marginalized over tangential
     * velocity. Volume returned is effective survey volume for thin disk stars of all tangential velocities.
     * Note that survey corrections (discovery fraction and reduced chi-square) can either be applied within
     * Vmax method or main body of code; in this case corrections are applied within the method.
     *
     * @param absmags             Absolute magnitudes for stars for which survey volume is to be calculated
     * @param discoveryFractions  Discovery fractions as a function of v_{tan} for each kinematic population
     * @param v_min               Tangential velocity ranges into which to divide survey volume
     * @return
     */

    @Override
    public double[][] getMarginalizedVmax(double[] absmags, DiscoveryFractions[] discoveryFractions, double[] v_min, boolean discrete){

        double[][] V   = new double[3][v_min.length];
        double[][] V_i = new double[3][v_min.length];

        //+++ Get absolute magnitudes +++//
        double B  = absmags[0];
        double R1 = absmags[1];
        double R2 = absmags[2];
        double I  = absmags[3];

	//+++ Max/min distances arising from magnitude limits +++//
	double dmin,dmax;

	//+++ Loop over hemispheres and fields, and calculate total accessible volume +++//
	for(int h = 0; h<2; h++){

	    String HEMI = (h==0) ? "N" : "S";

	    for(int FIELD = 1; FIELD < 898; FIELD++){

		//+++ Check that current field both exists in SSS and is not on rejection lists +++//
		if(this.includeFields[FIELD][h]){

                    //+++ Zero volumes for this field +++//
                    for(int pop=0; pop<3; pop++)
                        for(int v=0; v<v_min.length; v++)
                            V_i[pop][v]=0.0;

		    //+++ Calculate min and max distances based on magnitude +++//
		    dmax = Misc.min(Math.pow(10,(this.getFaintBLimit(FIELD, HEMI) - B  + 5)/5.0),
			            Math.pow(10,(this.getFaintR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
			            Math.pow(10,(this.getFaintR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
			            Math.pow(10,(this.getFaintILimit(FIELD, HEMI) - I  + 5)/5.0));

		    dmin = Misc.max(Math.pow(10,(this.getBrightBLimit(FIELD, HEMI)  - B  + 5)/5.0),
			            Math.pow(10,(this.getBrightR1Limit(FIELD, HEMI) - R1 + 5)/5.0),
			            Math.pow(10,(this.getBrightR2Limit(FIELD, HEMI) - R2 + 5)/5.0),
				    Math.pow(10,(this.getBrightILimit(FIELD, HEMI)  - I  + 5)/5.0));


                    //+++ Now integrate survey volume between magnitude distance limits, correcting +++//
                    //+++ each annulus by discovery fraction of objects passing survey proper       +++//
                    //+++ motion limits at the corresponding distance.                              +++//

                    double d_step = dmin*(Math.pow(10,dm/5.0)-1.0);
                    double d_lower, d_upper;
                    double d_midStep;

                    //+++ Volume elements for each population +++//
                    double d_thinDisk, d_thickDisk, d_halo;

                    //+++ Apparent b mag at distance step +++//
                    double b;

                    for(double d = dmin; d < dmax; d+=d_step){

                        //+++ Size of next constant magnitude step +++//
        		d_step = d*(Math.pow(10,dm/5.0)-1.0);

		        //+++ distance from d -> d+d_step to be integrated over in this step +++//
		        d_lower = d;
		        d_upper = d+d_step;

		        //+++ shift upper integration limit if new distance step is +++//
		        //+++ larger than dmax. Occurs on final step of integration +++//
			if(d_upper>dmax){ d_upper = dmax;}

                        //+++ Distance at which magnitude is halfway between that at lower +++//
                        //+++ and upper distance, where discovery fraction is evaluated.   +++//
                        d_midStep = Math.sqrt(d_lower*d_upper);

                        //+++ apparent magnitude required for proper motion limit +++//
                        b = B + 5*Math.log10(d_midStep) - 5;


                        //+++ Get generalized volume contained in this annulus, for each +++//
                        //+++ kinematic population. Copy to each v_min element.          +++//

                        //+++ Thin disk volume in this distance element +++//
                        d_thinDisk  = this.thinDisk.getVGen(d_upper, FIELD, HEMI) - this.thinDisk.getVGen(d_lower, FIELD, HEMI);

                        //+++ Thick disk volume in this distance element +++//
                        d_thickDisk = this.thickDisk.getVGen(d_upper, FIELD, HEMI) - this.thickDisk.getVGen(d_lower, FIELD, HEMI);

                        //+++ Spheroid +++//
                        d_halo      = (this.footprint.getSolidAngle(FIELD, HEMI)/3.0) * (d_upper*d_upper*d_upper - d_lower*d_lower*d_lower);

                        //+++ Correct by chi^2 discovery fraction for this hemisphere +++//
                        d_thinDisk  = d_thinDisk * this.getChi2DiscoveryFraction(HEMI);
                        d_thickDisk = d_thickDisk * this.getChi2DiscoveryFraction(HEMI);
                        d_halo      = d_halo * this.getChi2DiscoveryFraction(HEMI);

                        //+++ Now correct each volume element by corresponding V_{tan} discovery fraction +++//

                        //+++ Get tangential velocity sensitivity limits at this distance, from survey limits +++//
                        double vt_min = 4.74 * d_midStep * this.getLowerProperMotionLimit(b, FIELD, HEMI);
                        double vt_max = 4.74 * d_midStep * this.highMuLimit;

                        //+++ P(vt) only tabulated to 599km^{-1} - restrict vt to this range +++//
                        if(vt_max>599.0) vt_max = 599.0;
			if(vt_min>599.0) vt_min = 599.0;

                        //+++ Loop over all lower velocity thresholds and correct corresponding +++//
                        //+++ volume element by corresponding discovery fractions.              +++//
                        for(int v=0; v<v_min.length; v++){

                            //+++ Restrict v_tan lower limit. Alter this bit to do restricted ranges of Vtan +++//
                            double vt_lower = Math.max(v_min[v],vt_min);

                            //+++ If discrete tangential velocity ranges are used, upper v_{tan} limit is +++//
                            //+++ is set by upper edge of current bin. If not, then vt_max is used.       +++//
                            double vt_upper = (!discrete) ? vt_max : ((v==(v_min.length-1)) ? vt_max : Math.min(v_min[v+1],vt_max));

                            //+++ Discovery fraction for each population +++//
                            double[] corrections = new double[3];

                            //+++ Is survey sensitive to these stars at this velocity range? +++//
                            if(vt_upper > vt_lower){

                                //+++ Survey is sensitive to these stars - get fraction that are observed +++//
                                corrections[0] =   discoveryFractions[0].getDiscoveryFraction(FIELD, HEMI, vt_lower)
                                                 - discoveryFractions[0].getDiscoveryFraction(FIELD, HEMI, vt_upper);
                                corrections[1] =   discoveryFractions[1].getDiscoveryFraction(FIELD, HEMI, vt_lower)
                                                 - discoveryFractions[1].getDiscoveryFraction(FIELD, HEMI, vt_upper);
                                corrections[2] =   discoveryFractions[2].getDiscoveryFraction(FIELD, HEMI, vt_lower)
                                                 - discoveryFractions[2].getDiscoveryFraction(FIELD, HEMI, vt_upper);

                            }
                            else{
                                //+++ Survey not sensitive to these stars over this tangential velocity range +++//
                                corrections[0]=corrections[1]=corrections[2] = 0.0;
                            }

                            //+++ Correct volume elements by these discovery fractions and add tot toals +++//
                            V_i[0][v] += d_thinDisk* corrections[0];
                            V_i[1][v] += d_thickDisk* corrections[1];
                            V_i[2][v] += d_halo* corrections[2];

                        }

		    }

                    //+++ Finished calculating marginalized volumes for this field. Add to totals +++//
                    for(int pop=0; pop<3; pop++)
                        for(int v=0; v<v_min.length; v++)
                            V[pop][v] += V_i[pop][v];

		}

	    }

	}

	return V;
    }

}
