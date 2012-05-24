JAVA=C:/arc/java/jre6/bin/java
ECLIPSE=C:/Eclipse-3.6/eclipse
VERSION=1.1.1.R36x_v20101122_1400

rm -rf buildDirectory


$JAVA -jar $ECLIPSE/plugins/org.eclipse.equinox.launcher_${VERSION}.jar \
		-application org.eclipse.ant.core.antRunner \
		-buildfile "buildProduct.xml" 
