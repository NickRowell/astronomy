/**
 * The Pickles library of stellar spectra, published by A. Pickles (A.J. Pickles, PASP 110,863, 1998).
 * Data was downloaded from <a href="http://www.eso.org/sci/facilities/paranal/decommissioned/isaac/tools/lib.html">ESO</a>.
 * Relevant parts of the documentation:
 * <p>
 * <ul>
 *  <li> The spectral range is 1150-25000 Anstroms, sampling 5 Angstroms.
 *  <li> Note that most of the spectra have interpolated values in some part ofthe IR wavelength range when no data was available.
 *  <li> Fluxes are in F_lambda units.
 *  <li> The syntax for the filenames is as follows:
 *  <ul>
 *   <li><b>ukxxy.ext</b>: where xx is the spectral type in lower case letter (e.g. a0, g5), y  the luminosity class, in roman
 *           	lower case letters (i,ii, iii, iv, v), and ext the extension (dat for ascii files, fits for fitsfiles, gif for gif files).
 *   <li> <b>ukrxxy.ext</b>: same as above for metal rich stars.
 *   <li> <b>ukwxxy.ext</b>: same as above for metal weak stars.
 *  </ul>
 *  <li> Files with *_new* have been updated to contain actual H and/or K spectra from Ivanov et al. (2004, ApJS, 151, 387) instead of models as in the original Pickles (1998) paper. Only spectrathat used models were updated. A table with a description of the updatedfiles can be found here.
 *  <li> "The UVKLIB spectral library also consists of 131 text files arranged in 5--8 columns. Each file lists wavelength from 
 *        1150--25000 Angstroms, at 5 <span>&#8491;</span>/pixel steps in the first column, the corresponding F_{lambda} in column 2, the 
 *        standard deviation of the combination in column 3, the spectrum from UVILIB (above) in column 4, and the infrared source
 *        spectra in subsequent columns."
 * </ul>
 */
package resources.spectro.pickles;