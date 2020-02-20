#!/bin/bash

# NOTE: it is necessary to comment out some rows in the thin H & He files over the discontinuity in the cooling age,
#       otherwise the interpolation code will freak out as the non-monotonic function cannot be uniquely inverted.

# Untar the files
tar -xzvf Tables.tar.gz

# Remove unused model files
rm Table_DA_thin
rm Table_DA_thick
rm Table_DB

# Make subdirectories to store the thin & thick hydrogen models separately, so that the same filenames can be used
mkdir thin
mkdir thick

# Script assumes that all files contain the models in the order thick H, thin H, He

for MASS in 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 1.2; do

  # Extract the thick H, thin H and He models to separate files, comment out the first two header lines
  sed '/thick/,/thin/!d;/thin/d' Table_Mass_${MASS} | sed '1,2 s/^/#/' > thick/H_${MASS}
  sed '/thin/,/helium/!d;/helium/d' Table_Mass_${MASS} | sed '1,2 s/^/#/' > thin/H_${MASS}
  sed -n '/helium/,$p' Table_Mass_${MASS} | sed '1,2 s/^/#/' > He_${MASS}

  # Delete the original file
  rm Table_Mass_${MASS}

done
