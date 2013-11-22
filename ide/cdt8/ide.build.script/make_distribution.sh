JAVA="C:/ARC/java/jre6/bin/java"
ECLIPSE=C:/tools/eclipseKEPLER431.32

rm -rf buildDirectory


"$JAVA" -DbaseLocation=$ECLIPSE -jar $ECLIPSE/plugins/org.eclipse.equinox.launcher_*.jar \
		-application org.eclipse.ant.core.antRunner \
		-buildfile "buildProduct.xml"  \
		-Dbuilder=.
