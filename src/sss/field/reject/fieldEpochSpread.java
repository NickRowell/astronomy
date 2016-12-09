package sss.field.reject;

import java.io.*;

import sss.survey.dm.HighMuStar;
import sss.util.Misc;

/**
 * This class runs on the high proper motion data file (newpm_everything) and uses the star records
 * to check the epoch spread in each field. It prints out a list of fields whose epoch spread is
 * narrower than some threshold. These can be selected for exclusion from high proper motion surveys.
 *
 * @author nickrowell
 */
public class fieldEpochSpread {

    static double limit = 1.5;

    public static void main(String args[]) throws IOException, Exception {

        FileReader catalogue = new FileReader("src/Data/newpm_everything.dat");
        BufferedReader in = new BufferedReader(catalogue);

        String data;
        int prevField = -1;

        while ((data = in.readLine()) != null) {

            HighMuStar star = new HighMuStar(data);

            if (star.f != prevField) {

            	double spread = Misc.max(star.epochR2, star.epochB, star.epochI) - Misc.min(star.epochR2, star.epochB, star.epochI);
            	
                if (spread < limit) {
                    //+++ Found a field with epoch spread below the limit. Print out details +++//
                    System.out.println("Field " + star.f + star.hemi + ":\tepoch spread " + spread + " years");
                }

                prevField = star.f;

            }
        }
        
        in.close();

    }
}