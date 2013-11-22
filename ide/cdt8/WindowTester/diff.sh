diff -b -w baselineSnapshots/$1.xml testSnapshots/$1.xml
if [ -f baselineSnapshots/${1}_alt1.xml ]; then
    echo =================================================
    diff -b -w baselineSnapshots/${1}_alt1.xml testSnapshots/$1.xml
    fi
if [ -f baselineSnapshots/${1}_alt2.xml ]; then
    echo =================================================
    diff -b -w baselineSnapshots/${1}_alt2.xml testSnapshots/$1.xml
    fi
if [ -f baselineSnapshots/${1}_alt3.xml ]; then
    echo =================================================
    diff -b -w baselineSnapshots/${1}_alt3.xml testSnapshots/$1.xml
    fi
