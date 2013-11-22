install:
	cd grmain && make && cd ../
	cd displaymain && make && cd ../
	cd rendermain0 && make && cd ../
	cd rendermain1 && make && cd ../
	/home/chuckj/vdkinstalldir/bin/linkpatcher.sh 0 grmain/grmain displaymain/displaymain rendermain0/rendermain0 rendermain1/rendermain1 
	cd grmain && make && cd ../
	cd displaymain && make && cd ../
	cd rendermain0 && make && cd ../
	cd rendermain1 && make && cd ../
	cp -f dbgconfig.xml.new dbgconfig.xml
	cp -f run.sh.new run.sh
	echo "*** Compilation is Done! ***"

clean:
	cd grmain && make clean && cd ../
	cd displaymain && make clean && cd ../
	cd rendermain0 && make clean && cd ../
	cd rendermain1 && make clean && cd ../
