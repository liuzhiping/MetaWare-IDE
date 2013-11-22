How to update the version number of the Splash screen:

1) Check out the following directory into your svn workspace if it isn't already checked out:

    http://sjvm-subversion02.arc.com/svn/Tools/MetaWare/Toolset/trunk/Eclipse/media

2) From Windows: cd to .../Eclipse/media/splash/gen

3) To set the version in the splash screen, invoke: "sh set_version.sh <version-number>"
   The resulting splash screen will be in the file "splash.bmp".

4) Modify the script in that directory named "update.sh" to copy the splash.bmp file to
   the correct location of your IDE workspace. Then invoke it: "sh update.sh"


