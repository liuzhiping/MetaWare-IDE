#
# Note this makefile assume that libpng.a is in the local directory
# and that it is a cygwin compile.
#
png_diff: png_diff.c makefile
	#gcc -lpng -Os -s -W -Wall png_diff.c -o png_diff
	#gcc /lib/libpng.a -Os -s -W -Wall png_diff.c -o png_diff
	#gcc /usr/lib/libpng12.a -Os -s -W -Wall png_diff.c -o png_diff
	gcc -W -Wall png_diff.c libpng.a /lib/libz.a -o png_diff

clean:
	rm png_diff.exe
