# Set up terminal
set terminal pngcairo font "Times" enhanced
# Access Angstrom symbol
set encoding iso_8859_1

# Line styles
set style line 1 lt 1 pt 5 ps 0.5 lc rgb "red"   lw 1  # Line 1

# Image size
set size nosquare

# Set x axis
set xrange [3000:9000]
set xtics 1000 font "Times,12" out nomirror offset 0,0.4
set mxtics 2
set xlabel font "Times,16" "Wavelength ({\305})" offset 0,0.7

# Set y axis
#set yrange [0:10]
#set ytics 20 font "Times,12" out nomirror offset 0,0.4
set mytics 2
set ylabel font "Times,16" "F_{/Symbol {\154}} (erg s^{-1} cm^{-2} {\305}^{-1})" offset 0,0.7


set output "SSSJ0021+2640.txt.png"
set title "SSS J 00 21 +26 40"
plot 'SSSJ0021+2640.txt' u 1:($2*1e16) w l ls 1 notitle
