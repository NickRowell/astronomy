/**
 * Package contains cooling tracks corresponding to DA WD models, as described in:
 * 
 * "New cooling sequences for old white dwarfs"
 *  Renedo I., Althaus L. G., Miller Bertolami M. M., Romero, A. D., Córsico, A. H., Rohrmann, R. D., García-Berro, E.
 *  2010, The Astrophysical Journal, 717, 183
 * 
 * They were downloaded from the website:
 * 
 * http://evolgroup.fcaglp.unlp.edu.ar/TRACKS/tracks_cocore.html
 * 
 * 
 * The models available on the website contain only the total luminosity, not the colours. I emailed L. Althaus on 02/12/15 to ask about that and he
 * sent me back another set of Z=0.01 models that included the colours in various standard filters. These are contained in the Z=0.01 folder along
 * with the default models. This is what L. Althaus said:
 * 
 * "thanks for your mail and your interest in our work. In the attachment you will find a tar file containing the colours in some filter systems.
 * 
 * Lyman alpha wing opacity induced by collisions H-H and H-H2 for cool white dwarf atmospheres are included from Rohrmann, Althaus & Kepler, 2011,
 * MNRAS, 411, 781. Calculations are based on the quasi-static theory and includes Doppler broadening. The broad-band colours located in the UV and
 * blue spectral regions are shown to differ substantially from those published in previous studies, which did not include collision-induced Lyman
 * wing absorptions.
 * 
 * I hope this information is OK. Tracks correspond to metallicity Z=0.01 "
 * 
 */
package resources.wd.cooling.LPCODE.da;