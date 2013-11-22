elfdump -t $1  | awk -v VAR="$2" '/GLOB FUNC/ {print "<interval data-source=\"" VAR "\"",  "left=\"" strtonum($2) "\"", "right=\"" strtonum($2)+$3 "\"", "label=\"" $7 "\"/>"}' 
