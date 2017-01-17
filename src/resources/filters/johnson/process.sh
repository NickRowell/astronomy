#!/bin/bash

echo "# Johnson-Cousins U band; obtained from http://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves" > Uj.dat
awk '!/^(#|<|$)/ {print $1*10 " " $2/100.0}' Bessel_U-1.txt |  sort -g -k1 >> Uj.dat

echo "# Johnson-Cousins B band; obtained from http://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves" > Bj.dat
awk '!/^(#|<|$)/ {print $1*10 " " $2/100.0}' Bessel_B-1.txt |  sort -g -k1 >> Bj.dat

echo "# Johnson-Cousins V band; obtained from http://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves" > Vj.dat
awk '!/^(#|<|$)/ {print $1*10 " " $2/100.0}' Bessel_V-1.txt |  sort -g -k1 >> Vj.dat

echo "# Johnson-Cousins R band; obtained from http://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves" > Rj.dat
awk '!/^(#|<|$)/ {print $1*10 " " $2/100.0}' Bessel_R-1.txt |  sort -g -k1 >> Rj.dat

echo "# Johnson-Cousins I band; obtained from http://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves" > Ij.dat
awk '!/^(#|<|$)/ {print $1*10 " " $2/100.0}' Bessel_I-1.txt |  sort -g -k1 >> Ij.dat
