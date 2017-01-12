#!/bin/bash
#
# Script processes the Gaia_passbands.txt file to produce separate files for each filter,
# as required for processing in the Astronomy project.
#

echo "# Gaia nominal G band response curve in terms of photons (i.e. as quantum efficiency)" > G_photons.txt
echo "# Column 1: wavelength [Angstroms]" >> G_photons.txt
echo "# Column 2: response []" >> G_photons.txt
awk '!/^#/ {print $1*10 " " $2}' Gaia_passbands.txt >> G_photons.txt

echo "# Gaia nominal BP band response curve in terms of photons (i.e. as quantum efficiency)" > BP_photons.txt
echo "# Column 1: wavelength [Angstroms]" >> BP_photons.txt
echo "# Column 2: response []" >> BP_photons.txt
awk '!/^#/ {print $1*10 " " $3}' Gaia_passbands.txt >> BP_photons.txt

echo "# Gaia nominal RP band response curve in terms of photons (i.e. as quantum efficiency)" > RP_photons.txt
echo "# Column 1: wavelength [Angstroms]" >> RP_photons.txt
echo "# Column 2: response []" >> RP_photons.txt
awk '!/^#/ {print $1*10 " " $4}' Gaia_passbands.txt >> RP_photons.txt

echo "# Gaia nominal G band response curve in terms of energy" > G_energy.txt
echo "# Column 1: wavelength [Angstroms]" >> G_energy.txt
echo "# Column 2: response []" >> G_energy.txt
awk '!/^#/ {print $1*10 " " $5}' Gaia_passbands.txt >> G_energy.txt

echo "# Gaia nominal BP band response curve in terms of energy" > BP_energy.txt
echo "# Column 1: wavelength [Angstroms]" >> BP_energy.txt
echo "# Column 2: response []" >> BP_energy.txt
awk '!/^#/ {print $1*10 " " $6}' Gaia_passbands.txt >> BP_energy.txt

echo "# Gaia nominal RP band response curve in terms of energy" > RP_energy.txt
echo "# Column 1: wavelength [Angstroms]" >> RP_energy.txt
echo "# Column 2: response []" >> RP_energy.txt
awk '!/^#/ {print $1*10 " " $7}' Gaia_passbands.txt >> RP_energy.txt
