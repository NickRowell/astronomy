reset

set terminal pngcairo dashed enhanced color size 512,512 font "Helvetica"

# Common greek letters:
# Δ δ α π λ μ η ε β χ Ω ω Θ θ σ π ν → ∂· ν
# Astronomical symbols:
# ☉

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

# Style for filledcurves
set style fill solid 1.0 border -1


# Faint lines good for drawing grid
set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5
set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5

# Boxes style
set style fill transparent solid 0.5 noborder

# Colour palettes

# Jet colour scheme
set palette defined (0 '#000090', 1 '#000fff', 2 '#0090ff', 3 '#0fffee', 4 '#90ff70', 5 '#ffee00', 6 '#ff7000', 7 '#ee0000', 8 '#7f0000')

# Inverted Jet colour scheme
set palette defined (0 '#7f0000', 1 '#ee0000', 2 '#ff7000', 3 '#ffee00', 4 '#90ff70', 5 '#0fffee', 6 '#0090ff', 7 '#000fff', 8 '#000090')

# Plot setup
#
#

set xlabel "Z" font "Helvetica,14"
set xtics out nomirror offset 0.0,0.0 rotate by 0.0 scale 1.0
set mxtics 2
set xrange [*:*]
#set format x '%1.1g'
# Best for decimal places
#set format x '%1.1f'

set ylabel "Y" font "Helvetica,14"
set ytics out nomirror offset 0.0,0.0 rotate by 0.0 scale 1.0
set mytics 2
set yrange [*:*]
#set format y '%1.1g'
# Best for decimal places
#set format y '%1.1f'
#set format y '%.1s×10^{%T}'
#set format y '%1.0l×10^{%T}'

# Legend
# 'autotitle columnhead' will take titles for each plot from the first row.
set key top left

# Background grid lines
set grid xtics mxtics ytics mytics back ls 20, ls 21
unset grid

set title 'Metallicity of available tracks'

set output "metallicity.png"

f(x) = 0.2485 + 1.78*x

plot 'zy.txt' w p pt 5 ps 0.5 notitle, f(x) t '0.2485 + 1.78 Z'

