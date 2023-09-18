#!/bin/bash

# Untar the files
tar -xzvf AllTables.tar.gz

# Remove unused model files
rm Table_DA
rm Table_DB

# Script assumes that all files contain the models in the order thick H, thin H, He

for MASS in 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 1.1 1.2 1.3; do

  # Extract the H and He models to separate files, comment out the first two header lines
  sed '/hydrogen/,/helium/!d;/helium/d' Table_Mass_${MASS} | sed '1,2 s/^/#/' > H_${MASS}
  sed -n '/helium/,$p' Table_Mass_${MASS} | sed '1,2 s/^/#/' > He_${MASS}

  # Delete the original file
  rm Table_Mass_${MASS}

done
