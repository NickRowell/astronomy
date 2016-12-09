reset

set terminal pngcairo dashed enhanced color size 640,480 font "Helvetica"

# Plot styles config
#
# 5 distinct solid/dashed line styles, 3 distinct point types
set style line 1  lt 1 lw 2 pt 5  ps 1
set style line 2  lt 2 lw 2 pt 5  ps 1
set style line 3  lt 3 lw 2 pt 5  ps 1
set style line 4  lt 4 lw 2 pt 5  ps 1
set style line 5  lt 5 lw 2 pt 5  ps 1
set style line 6  lt 1 lw 2 pt 7  ps 1
set style line 7  lt 2 lw 2 pt 7  ps 1
set style line 8  lt 3 lw 2 pt 7  ps 1
set style line 9  lt 4 lw 2 pt 7  ps 1
set style line 10 lt 5 lw 2 pt 7  ps 1
set style line 11 lt 1 lw 2 pt 11 ps 1
set style line 12 lt 2 lw 2 pt 11 ps 1
set style line 13 lt 3 lw 2 pt 11 ps 1 
set style line 14 lt 4 lw 2 pt 11 ps 1 
set style line 15 lt 5 lw 2 pt 11 ps 1

# Faint lines good for drawing grid
set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5
set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5

# Boxes style
set style fill transparent solid 0.5 noborder
set boxwidth 0.95 relative


# Plot setup

set xlabel "Distance [pc]" font "Helvetica,14"
set xtics 200 out nomirror offset 0.0,0.0 rotate by 0.0 scale 1.0
set mxtics 2
set xrange [0:*]
set format x '%g'

set ylabel "Differential survey volume [pc^{3}/pc]" font "Helvetica,14"
set ytics 200 out nomirror offset 0.0,0.0 rotate by 0.0 scale 1.0
set mytics 2
set yrange [0:*]
set format y '%g'

# Legend
set key top right Left box opaque font "Helvetica,14" noreverse noinvert

# Background grid lines
set grid xtics mxtics ytics mytics back ls 20, ls 21
unset grid


set output "plot.png"

plot 'test_generalised_survey_volume.txt' i 0 u 1:2 w l title 'Model',\
     'test_generalised_survey_volume.txt' i 1 u 1:2 w l title 'Monte Carlo'

