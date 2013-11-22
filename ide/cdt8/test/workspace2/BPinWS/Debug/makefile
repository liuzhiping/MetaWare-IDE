################################################################################
# Automatically-generated file. Do not edit!
################################################################################

PROJECT := ..

WORKSPACE = C:/Users/lyou/mide/runtime-New_configuration
# "sh.exe" was not found in search path; using Windows cmd.exe

# Could not find "rm.exe" on the search path;
# the command "C:\ARC\MetaWare\ide\utils\rm.exe" will be used instead.

-include ../makefile.init

RM := C:\ARC\MetaWare\ide\utils\rm.exe -f

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
ifneq ($(strip $(MET_DEPS)),)
-include $(MET_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(S_DEPS)),)
-include $(S_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
MAPFILE += \


# All Target
all: BPinWS.elf

# Tool invocations
BPinWS.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: MetaWare Linker'
	mcc  -Hldopt=-q -e_start -arc600 -o "BPinWS.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C++_DEPS)$(ASM_DEPS)$(C_DEPS)$(CC_DEPS)$(MAPFILE)$(MET_DEPS)$(CPP_DEPS)$(S_DEPS)$(EXECUTABLES)$(CXX_DEPS)$(C_UPPER_DEPS) BPinWS.elf
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
