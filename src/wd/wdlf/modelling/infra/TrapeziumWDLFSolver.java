/**
 * 
 * Name:
 *  TrapeziumWDLFSolver.java
 * 
 * Purpose:
 *  This class is not yet implemented.
 * 
 * Instances of this class are used to calculate synthetic WDLF using Romberg
 * numerical integration method.
 * 
 * Language:
 * Java
 *
 * Author:
 * Nicholas Rowell
 * 
 */
package wd.wdlf.modelling.infra;

import javax.swing.SwingWorker;

public class TrapeziumWDLFSolver 
extends SwingWorker<Void,Void>
{
    
    /** ModellingState object. */
    ModellingState modellingState;
    
    /** Main constructor. */
    public TrapeziumWDLFSolver(ModellingState modellingState)
    {
        this.modellingState = modellingState;
    }
    
    /** Perform WDLF simulation in background thread. */
    @Override
    public Void doInBackground()
    {
        calculateWDLF();
        return null;
    }
    
    /** Trapezium LF integration code. */
    public final void calculateWDLF()
    {
     
        
//        // Construct new ModelWDLF objects
//        wdlf    = new ModelWDLF(magMin, magMax, dM);   // Total LF
//        wdlf_H  = new ModelWDLF(magMin, magMax, dM);   // H WDLF
//        wdlf_He = new ModelWDLF(magMin, magMax, dM);   // He WDLF
//        
//        // Loop over time since onset of star formation to present day. For
//        // each time element, find MS turnoff mass. This sets a lower limit
//        // on the mass of MS stars that have had time to contribute WDs to the
//        // present-day population.
//        //
//        // Then, loop over MS mass higher than this and transform each mass
//        // element to an element of the WDLF.
//        for (t = 0; t <= sfr.getAge(); t += deltaT) {
//
//            // Lookback time to present integration step
//            double T_LOOKBACK = sfr.getAge()-t;
//            
//            // Get total number of stars to form in this time element
//            double N_STARS_T = sfr.getSFR(t+deltaT/2) * deltaT;
//                        
//            // Get MS turn off mass at this age. All stars heavier than this
//            // that formed at T_LOOKBACK have since turned into WDs.
//            double M_l = ms.getMass(T_LOOKBACK);
//
//            // Sanity check. This tests that main sequence masses that
//            // contribute WDs to models lie within the range 0.6 -> 7M
//            // over which the IMF is normalised.
//            assert M_l > IMF.M_lower;
//
//            // Skip time element if no WDs have had time to form.
//            if (M_l >= IMF.M_upper) continue;
//
//            // Size of mass element. This gets trimmed down on final
//            // integration step, so refresh it here before starting next
//            // integration.
//            double dM2 = deltaM;
//
//            // Progenitor parameters used in LF calculation
//            double t_MS_mid, t_MS_low, t_MS_high;
//
//            // WD parameters used in LF calculation
//            
//            // Leading, trailing & midpoint of wd mass element
//            double m_wd_mid, m_wd_low, m_wd_high;
//            
//            // WD cooling times at leading, trailing & midpoint of mass element
//            double t_cool_mid, t_cool_low, t_cool_high;
//            
//            // Bolometric magnitude of each type of WD after cooling
//            double mbol_mid,  mbol_low,  mbol_high;
//            
//            // Bolometric magnitude sorted
//            double mbol_min, mbol_max, dmbol;
//
//            // Number of stars to form in current time AND mass element
//            double N_STARS_TM;
//
//            // Integer indicating if WD models were extrapolated to 
//            // calculate a given cooling age/mass point. (0=no; 1=yes)
//            int extrapolated;
//
//            // Loop over progenitor mass.
//            for (double Mp = M_l; Mp < IMF.M_upper; Mp += dM2) {
//
//                // Avoid overshoot on final integration step
//                if ((Mp + dM2) > IMF.M_upper) dM2 = IMF.M_upper - Mp;
//                
//
//                // Scale number of stars formed in this time element by the
//                // fraction that formed in the current mass element.
//                // 
//                // This is then split among the
//                // M_{bol} bins that it spans in order to get stars-per-magnitude,
//                // which is the desired quantity. The rest of the calculations
//                // in this loop simply find out the range of M_{bol} that these
//                // stars span and splits up the wdlf contribution accordingly.
//                //
//                // Note that 'd_wdlf_h' includes the dM_{bol} factor and gives
//                // stars-per-M_{bol} centred at M_bol_mid. This is not very
//                // useful because I really want models defined at the same
//                // magnitude points.
//
//                N_STARS_TM = N_STARS_T * imf.getIMF(Mp + dM2 / 2) * dM2;
//                
//                //double d_wdlf_h = getIMF(Mp + (dM / 2.0)) * dM/dM_bol;   // stars-per-M_{bol}
//
//
//                // Now figure out the range of mbol spanned by the current
//                // element of progenitor mass, for each WD atmosphere type.
//
//                // What is the MS lifetime for progenitors of mass M, 
//                // and at each end of dM bin?
//                t_MS_mid = ms.getLifetime(Mp + dM2 / 2);
//                t_MS_high = ms.getLifetime(Mp + dM2);
//                t_MS_low = ms.getLifetime(Mp);
//                // How long have WDs had to cool, at bin centre and each edge?
//                t_cool_mid  = T_LOOKBACK - t_MS_mid;
//                t_cool_high = T_LOOKBACK - t_MS_high;
//                t_cool_low  = T_LOOKBACK - t_MS_low;
//                // What mass WDs do these progenitors produce?
//                m_wd_mid = ifmr.getMf(Mp + dM2 / 2);
//                m_wd_high = ifmr.getMf(Mp + dM2);
//                m_wd_low = ifmr.getMf(Mp);
//
//                // Now calculate quantities specific to each WD atmosphere type.
//                // They cool at different rates, so will have reached different
//                // bolometric magnitudes in the same time.
//                //
//                // In this loop, 0 = H and 1 = He atmosphere types
//                for (int a = 0; a < 2; a++) {
//
//                    String atm = (a == 0) ? "H" : "He";
//                    // Get atmosphere type weight factor
//                    double atm_weight = (a == 0) ? AbstractModeller.getW_H() : 1.0 - AbstractModeller.getW_H();
//                    
//                    // What bolometric magnitude have WDs cooled to?
//                    mbol_mid = wd.mbol(t_cool_mid, m_wd_mid, atm);
//                    mbol_high = wd.mbol(t_cool_high, m_wd_high, atm);
//                    mbol_low = wd.mbol(t_cool_low, m_wd_low, atm);
//
//                    // Over what bolometric magnitude range is dM spread?
//                    mbol_min = Math.min(Math.min(mbol_mid, mbol_low), mbol_high);
//                    mbol_max = Math.max(Math.max(mbol_mid, mbol_low), mbol_high);
//
//                    // Absolute range in bolometric magnitude
//                    dmbol = Math.abs(mbol_max - mbol_min);
//
//                    // At central mass and cooling age, are WD models extrapolated?
//                    extrapolated = (wd.isExtrapolated(t_cool_mid, m_wd_mid, atm)) ? 1 : 0;
//
//                    // Total time since formation (WD cooling plus progenitor
//                    // lifetime).
//                    double totalAge = t_cool_mid + t_MS_mid;
//                    
//                    // Loop over each M_bol bin and check if any of this falls in it
//                    //
//                    double fraction, M_low, M_high;
//                    //
//                        
//                    // All ModelWDLFs have the same magnitude bins.
//                    for(int m=0; m < wdlf.number.getN(); m++){    
//
//                        // Get magnitude of each bin edge
//                        M_low  = wdlf.number.getBinLowerEdge(m);
//                        M_high = wdlf.number.getBinUpperEdge(m);
//
//                        // Now passed beyond range of WDLF point
//                        if (mbol_max < M_low) { break;} 
//                        // Not yet found magnitude bins that WDLF contributes to
//                        else if (mbol_min > M_high) { continue;} 
//                        // Both limits fall in bin
//                        else if ((mbol_min < M_high) && (mbol_max > M_low)
//                                && (mbol_min < M_high) && (mbol_min > M_low)) {
//                            
//                            // Entire bolometric magnitude element falls in 
//                            // this bin.
//                            fraction = 1.0*atm_weight;
//                            
//                            // Add contribution to total WDLF
//                            wdlf.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            // Add contribution to each atmosphere type separately
//                            if(atm.equals("H"))
//                                wdlf_H.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            else
//                                wdlf_He.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            
//                        } 
//                        // Only lower limit on bolometric magnitude range falls in LF bin, partial contribution
//                        else if ((mbol_max > M_high) && (mbol_min < M_high) && (mbol_min > M_low)) {
//                            
//                            // Part of bolometric magnitude element falls in 
//                            // this bin.
//                            fraction = (M_high - mbol_min)*atm_weight / dmbol;
//                                               
//                            // Add contribution to total WDLF
//                            wdlf.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            // Add contribution to each atmosphere type separately
//                            if(atm.equals("H"))
//                                wdlf_H.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            else
//                                wdlf_He.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);    
//                        } 
//                        // Only upper limit on bolometric magnitude range falls in LF bin
//                        else if ((mbol_max < M_high) && (mbol_max > M_low) && (mbol_min < M_low)) {
//                            
//                            // Part of bolometric magnitude element falls in 
//                            // this bin.
//                            fraction = (mbol_max - M_low)*atm_weight / dmbol;
//                                                        
//                            // Add contribution to total WDLF
//                            wdlf.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            // Add contribution to each atmosphere type separately
//                            if(atm.equals("H"))
//                                wdlf_H.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            else
//                                wdlf_He.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge); 
//                        } 
//                        // Limits completely straddle bin
//                        else if ((mbol_max > M_high) && (mbol_min < M_low)) {
//                            
//                            // Part of bolometric magnitude element falls in 
//                            // this bin.
//                            fraction = (M_high - M_low)*atm_weight / dmbol;
//                            
//                            // Add contribution to total WDLF
//                            wdlf.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            // Add contribution to each atmosphere type separately
//                            if(atm.equals("H"))
//                                wdlf_H.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                            else
//                                wdlf_He.add(m, N_STARS_TM*fraction, m_wd_mid, extrapolated, totalAge);
//                        } 
//                        // Shouldn't reach this code under any circumstances
//                        else { assert false;}
//
//                    }  // Close loop over LF bins
//                }      // Close loop over atmosphere type
//            }          // Close loop over progenitor mass element
//        }              // Close loop over age
//        
//        // Now convolve all WDLFs with Gaussian kernel to simulate
//        // measurement errors.
//        
//        // Get Gaussian convolution kernel for simulating observational errors
//        double[] kernel = Statistics.getSymGausKernel(sigM, dM);
//        
//        // Convolve wdlfs with Gaussian kernel to simulate measurement error.
//        wdlf.convolve(kernel);
//        wdlf_H.convolve(kernel);
//        wdlf_He.convolve(kernel);
        
    }
    
}
