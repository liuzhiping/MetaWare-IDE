To build an IDE distribution: 

cd to this directory and invoke:

sh make_distribution.sh


The Windows and Linux zip files will be generated for Eclipse portions only.

Unfortunately, the config.ini files are missing the following that needs to be manually appended:

osgi.instance.area.default=@user.home/mide/workspace

You'll need to unzip each zip file in its location under "ide", append the above line to 
eclipse/configuration/config.ini, then zip up everything from ide directory.
