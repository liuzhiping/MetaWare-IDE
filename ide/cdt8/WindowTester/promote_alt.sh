doit(){
    cp -p $1 $2
    svn add $2
}

for f in "$@"; do
    if [ ! -f baselineSnapshots/${f}_alt1.xml ]; then
	echo alt1
	doit testSnapshots/$f.xml baselineSnapshots/${f}_alt1.xml
    elif [  ! -f baselineSnapshots/${f}_alt2.xml ]; then
	echo alt2
	doit testSnapshots/$f.xml baselineSnapshots/${f}_alt2.xml
    elif [  ! -f baselineSnapshots/${f}_alt3.xml ]; then
	echo alt3
	doit testSnapshots/$f.xml baselineSnapshots/${f}_alt3.xml
    elif [  ! -f baselineSnapshots/${f}_alt4.xml ]; then
	echo alt4
	doit testSnapshots/$f.xml baselineSnapshots/${f}_alt4.xml
    elif [  ! -f baselineSnapshots/${f}_alt5.xml ]; then
	echo alt5
	doit testSnapshots/$f.xml baselineSnapshots/${f}_alt5.xml
    else
	echo Failed
	fi
done
