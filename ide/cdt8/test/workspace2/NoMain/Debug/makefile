################################################################################
# Automatically-generated file. Do not edit!
################################################################################

PROJECT := ..

WORKSPACE = ../..
#SHELL=C:\PROGRA~2\MKSTOO~1\mksnt\sh.exe

# CYGWIN=nodosfilewarning
# This setting is required for the Cygwin shell to recognize the DOS cr/lf
# line-termination convention. This definition is not emitted if SHELLOPTS
# is already defined in the environment.
export SHELLOPTS=igncr

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
MAPFILE += \


# All Target
all: NoMain.elf

# Tool invocations
NoMain.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: MetaWare Linker'
	mcc  -Hldopt=-q -e_start -arc600 -o "NoMain.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C++_DEPS)$(ASM_DEPS)$(C_DEPS)$(CC_DEPS)$(MAPFILE)$(CPP_DEPS)$(EXECUTABLES)$(CXX_DEPS)$(C_UPPER_DEPS)$(S_UPPER_DEPS) NoMain.elf
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
