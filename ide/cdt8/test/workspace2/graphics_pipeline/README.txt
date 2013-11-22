
graphics_pipeline:


How to build:

) make clean

  -- clean the project

) make JAVA_SDK=C:/progra~1/Java/jdk1.6.0_03

  -- Builds the hardware and software architecture
     and then invokes mcsg to generate the project.
     Next the top-level generated makefile is invoked
     to compile the project for the target.

     NOTE: Pass the path to the JDK on your machine via
      the JAVA_SDK argument above.




How to run:

OS=LEGACY
OS=NO_OS_LEGACY 

    In the MetaWare debugger, create one "image" display window.
    To find the address of the primary_raster, enter the following
    in a bash window from the directory generated/software:

    ) objdump -t displaymain/displaymain | grep primary
    03c00000 g     O .aframe 00006300 primary_raster

    Image is written to this primary_raster address
    and its dimensions are 176x144, 8-bit grayscale
    So in the MetaWare Debugger or IDE, configure the
    one image view to these.

OS=MQX

    Image is written to a termwin window and dimensions
    are 80x40


You can also create a soft link to your latest JDK
like this:

) cd C:/progra~1/Java/
) ln -s jdk1.6.0_03 latest

and then just invoke "make" with no arguments.
The jdk will found by following C:/progra~1/Java/latest

On Linux machines, /usr/java/latest is used to find the JDK.

