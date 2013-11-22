## *MAKEFILE***********************************************************
## ********************************************************************
##
## Copyright (c) 1989-2003 ARC International.
## All rights reserved
##
## This software embodies materials and concepts which are
## confidential to ARC International and is made
## available solely pursuant to the terms of a written license
## agreement with ARC International
##
## File: makefile
##
## Comments:
##    This is a MQX example makefile.
##
## Usage:
##
##    'make'		   builds the project.
##
##    'make clean'	   deletes the output directory
##
##    'make mostlyclean'   deletes intermediary files from the
##			   output directory.  Header files,
##			   the dependency file, and the library
##			   or executable are untouched.
##
##    'make depend'	   generates a dependency file
##
## ********************************************************************
## *END****************************************************************

#
# This makefile has two phases.	 In the first phase, we perform
# some error checking, create the output directory, and spawn
# the second phase.  The second phase is run from the output
# directory, and actually builds the project.
#
# This makefile is used for both phases.  We use the variable
# MAKELEVEL to check which phase we're in.
#

MQX_PROJ=taskq

.SUFFIXES:

# Compute path separator for Windows or Unix
nullstring=
PS=/
ifneq ($(ComSpec)$(COMSPEC),)
    PS=\$(nullstring)
endif

ifeq ($(MAKELEVEL),0)

###########################################################
#
# Phase 1.  Perform some error checking.  Check that we
# can find config.mk, and that config.mk sets all the
# variables we need.
#
###########################################################

all: $(MQX_PROJ)

# If MQX_CONFIG is not defined, try config.mk in the
# current directory
ifeq ($(MQX_CONFIG),)
    export MQX_CONFIG=$(CURDIR)$(PS)config.mk
endif
ifeq ($(wildcard $(MQX_CONFIG)),)
    ALL?=err_nocfg
endif

-include $(MQX_CONFIG)

ifeq ($(MQX_MAKEFILE_DIR),)
    ALL?=err_nomakefiledir
endif
ifeq ($(wildcard $(MQX_MAKEFILE_DIR)$(PS)mqx.mk),)
    ALL?=err_badmakefiledir
endif

-include $(MQX_MAKEFILE_DIR)$(PS)mqx.mk

ifeq ($(MQX_COMPILER),)
    ALL?=err_nocompiler
endif

ifeq ($(wildcard $(MQX_MAKEFILE_DIR)$(PS)$(MQX_COMPILER).mk),)
    ALL?=err_badcompiler
endif

ifeq ($(MQX_COMPILER_ROOT),)
    ALL?=err_nocompilerdir
endif

ifeq ($(MQX_PSP),)
    ALL?=err_nopsp
endif

ifeq ($(MQX_BSP),)
    ALL?=err_nobsp
endif

# If either MQX_PSP_LIB_DIR or MQX_BSP_LIB_DIR are not set, then
# it's probably because MQX_ROOT is not set
ifeq ($(MQX_PSP_LIB_DIR),)
    ALL?=err_nomqx
endif
ifeq ($(MQX_BSP_LIB_DIR),)
    ALL?=err_nomqx
endif

# Decide where to build the library.  We pick (in order of preference)
#     1) PROJECT_DIR
#     2) $(CURDIR)$(PS)$(MQX_BSPLIB)
ifeq ($(PROJECT_DIR),)
    export PROJECT_DIR=$(CURDIR)$(PS)$(MQX_PSPLIB)
endif

export PROJECT_SRC_DIR=$(CURDIR)

ifdef ALL

#
# If we did find errors, then 'all' will "build" one of
# the err_* rules, which just prints a message and exits.
#

$(MQX_PROJ) $(MAKECMDGOALS): $(ALL)

err_nocfg:
	@echo Could not find $(MQX_CONFIG)

err_nomakefiledir:
	@echo The variable MQX_MAKEFILE_DIR is not set in $(MQX_CONFIG)

err_badmakefiledir:
	@echo Could not find mqx.mk -- the variable MQX_MAKEFILE_DIR
	@echo ($(MQX_MAKEFILE_DIR)) is set incorrectly in $(MQX_CONFIG)

err_nocompiler:
	@echo The variable MQX_COMPILER is not set in $(MQX_CONFIG)

err_badcompiler:
	@echo The compiler $(MQX_COMPILER) is not supported -- one (or more) of the
	@echo following variables is (are) set incorrectly:
	@echo	  MQX_MAKEFILE_DIR ($(MQX_MAKEFILE_DIR))
	@echo	  MQX_COMPILER ($(MQX_COMPILER))

err_nocompilerdir:
	@echo The variable MQX_COMPILER_ROOT is not set in $(MQX_CONFIG)

err_nopsp:
	@echo The variable MQX_PSP is not set in $(MQX_CONFIG)

err_nobsp:
	@echo The variable MQX_BSP is not set in $(MQX_CONFIG)

err_nomqx:
	@echo The variable MQX_ROOT is not set in $(MQX_CONFIG)

else

#
# If there were no errors, ALL is empty.  We recursively
# invoke MAKE in the output directory.
#

$(MQX_PROJ) mostlyclean depend:
	@$(IFNOTEXISTDIR)$(PROJECT_DIR)$(ENDIFNOTEXISTDIR) $(MKDIR) $(PROJECT_DIR)
	-$(MAKE) -C $(PROJECT_DIR) -f $(CURDIR)$(PS)makefile $(MAKECMDGOALS)
	@$(IFEXISTFILE)$(PROJECT_DIR)$(PS)*.err$(ENDIFEXISTFILE) $(CAT) $(PROJECT_DIR)$(PS)errors $(CATWITH) $(PROJECT_DIR)$(PS)*.err $(CATTO) $(PROJECT_DIR)$(PS)errors $(CATEND) > $(NULL)
	@$(IFEXISTFILE)$(PROJECT_DIR)$(PS)*.err$(ENDIFEXISTFILE) $(RM) $(PROJECT_DIR)$(PS)*.err

clean:
	@$(IFNOTEXISTDIR)$(PROJECT_DIR)$(ENDIFNOTEXISTDIR) $(MKDIR) $(PROJECT_DIR)
	@$(RMALL)$(PROJECT_DIR)$(ENDRMALL)
	@$(RMDIR) $(PROJECT_DIR)

endif

else # $(MAKELEVEL) == 1

###########################################################
#
# Phase 2.  Build the project
#
###########################################################

all: $(MQX_PROJ)

MQX_INCDIRS=$(PROJECT_CFG_DIR) $(PROJECT_SRC_DIR)

include $(MQX_CONFIG)
include $(MQX_MAKEFILE_DIR)$(PS)mqx.mk
include $(MQX_MAKEFILE_DIR)$(PS)$(MQX_COMPILER).mk

#
# If MQX_CPU is empty, then either mqx.mk didn't recognize
# MQX_PSP, or <COMPILER>.mk doesn't support MQX_PSP.
# Either way, it's an error
#

ifeq ($(MQX_CPU),)

$(MQX_PROJ) $(MAKECMDGOALS):
	@echo The $(MQX_COMPILER) compiler does not support the $(MQX_PSP) PSP.

else

#
# Define the following variables:
#
#     MQX_INCDIRS   additional directories to search for header files
#     srcdirs	    additional directories to search for source files
#     srcs	    list of source files without extensions
#

srcs = taskq


#
# The vpath directive tells make where to look for dependencies
#

objs := $(addsuffix .$(MQX_OEXT),$(srcs))
deps := $(addsuffix .d,$(srcs))
# Start CR 803
libs := $(addsuffix .$(MQX_LEXT),$(MQX_LIBS))
# End	CR 803

vpath %.c $(srcdirs) $(PROJECT_SRC_DIR)

#
# Now the rules to build the project
#
# The following file extensions are used:
#
#     .c		C files
#     .d		makefile dependency files
#     .$(MQX_OEXT)	object files
#     .$(MQX_XEXT)	executable files
#

%.$(MQX_OEXT): %.c
	$(MQX_CC)

%.d: %.c
	$(MQX_DEPEND)

$(MQX_PROJ).$(MQX_XEXT): $(objs) $(libs)
	$(MQX_LD)

$(MQX_PROJ): $(MQX_PSP) $(MQX_PROJ).$(MQX_XEXT)

mostlyclean:
	$(MQX_MOSTLYCLEAN)

depend: $(deps)

#
# Include the dependencies generated by 'make depend'
#

ifeq ($(findstring $(MAKECMDGOALS),mostlyclean depend),)
-include $(deps)
endif

endif
endif

# EOF
