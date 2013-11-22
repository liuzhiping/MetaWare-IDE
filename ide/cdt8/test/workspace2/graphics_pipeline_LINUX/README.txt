
graphics_pipeline:


How to build:

) make clean

  -- clean the project

) make JAVASDK=C:/progra~1/Java/jdk1.6.0_03

  -- Builds the hardware and software architecture
     and then invokes mcsg to generate the project.
     Next the top-level generated makefile is invoked
     to compile the project for the target.

     NOTE: Pass the path to the JDK on your machine via
      the JAVASDK argument above.




How to run:

OS=LEGACY
OS=NO_OS_LEGACY 

    Image is written to the address 0x00E00000
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
