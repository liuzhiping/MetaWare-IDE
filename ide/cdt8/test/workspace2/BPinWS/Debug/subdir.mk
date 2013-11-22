################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../bpinws.c 

OBJS += \
./bpinws.o 

C_DEPS += \
./depend/bpinws.u 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MetaWare ARC C/C++ Compiler'
	mcc -c -g -Hnocopyr -arc600 -core1 -Humake -Hdepend="depend" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


