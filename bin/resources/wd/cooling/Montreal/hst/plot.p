set terminal pngcairo enhanced

set xlabel "M_{bol}"
set ylabel "Cooling time [yr]"
set xrange [6:8]
set yrange [0:3.5e7]

set output "He_0.7.png"

plot 'He_0.7' u 3:4 w lp pt 5 ps 0.5 title "He 0.7M_{Sol}"
