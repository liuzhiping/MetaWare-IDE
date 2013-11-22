#/************************************************************************/
#/*                                                                      */
#/* Copyright (C) ARC International 2008.                                */
#/*                                                                      */
#/* All Rights Reserved.                                                 */
#/*                                                                      */
#/* This software is the property of ARC International.  It is           */
#/* furnished under a specific licensing agreement.  It may be used or   */
#/* copied only under terms of the licensing agreement.                  */
#/*                                                                      */
#/* For more information, contact support@arc.com                        */
#/*                                                                      */
#/************************************************************************/

ifeq ($(strip $(VDK_INSTALL_DIR)),)
  export VDK_INSTALL_DIR = C:/Vr/VrDev
endif

#$(warning VDK_INSTALL_DIR=$(VDK_INSTALL_DIR))
HOST = $(shell uname)
ifeq ($(HOST),Linux)
 export JAVASDK          = /usr/java/latest
 export CLPATH           = ".:$(VDK_INSTALL_DIR)/classes/spec_io.jar"
else
 export JAVASDK          = C:/progra~1/Java/latest
 export CLPATH           = ".;$(VDK_INSTALL_DIR)/classes/spec_io.jar"
endif

export JC               = $(JAVASDK)/bin/javac
export JAVA             = $(JAVASDK)/bin/java
export JAVAH            = $(JAVASDK)/bin/javah
export JAVAJAR          = $(JAVASDK)/bin/jar

JAVA_FLAGS = -version -sourcepath .
JAVA_FLAGS += -classpath "$(VDK_INSTALL_DIR)/classes/spec_io.jar"

HOST = $(shell uname)

ifeq ($(strip $(VDK_CATALOG_PATH)),)
  export VDK_CATALOG_PATH =
#
#   NOTE: ^^^^ This is intentionally empty so that an error will be thrown
#    if user doesn't set this.
#
endif


include src/defs.mk

MCSG_BIN     = $(LOCAL_VDK_INSTALL_DIR)/bin


#
# SWARCH XML File
#
SWARCH_XML = swarch.xml


C = \
    src/grmain.c \
    src/grapi.c \
    src/displaymain.c \
    src/rendermain.c \
    src/plot3d.c \
    src/plot.c \
    src/screen.c \
    src/config.c \
    src/init.c

H = \
    src/grapi.h \
    src/grapi_types.h \
    src/common.h \
    src/common_types.h \
    src/plot3d.h \
    src/plot.h \
    src/screen.h \
    src/config.h \
    src/track_state.h


all: $(SWARCH_XML) generated/software/Makefile app


app: generated/software/Makefile
	cd generated/software;make install


generated/software/Makefile: $(H) $(C) $(SWARCH_XML) Makefile
	env VDK_INSTALL_DIR=$(NATIVE_VDK_INSTALL_DIR) \
	  $(MCSG_BIN)/mcsg.sh \
          -O -__dont_validate_schema \
	  $(SWARCH_XML)


$(SWARCH_XML): swarch.class
	$(JAVA) -classpath $(CLPATH) swarch

swarch.class: swarch.java hwarch.java $(VDK_INSTALL_DIR)/classes/spec_io.jar
	$(JC) -classpath "$(VDK_INSTALL_DIR)/classes/spec_io.jar" \
                         swarch.java hwarch.java

libs: obj_clean

clean:
	rm -rf src/*.o
	rm -rf swarch.class
	rm -rf $(SWARCH_XML)

obj_clean:
	rm -rf src/*.o

