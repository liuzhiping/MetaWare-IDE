################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../bad.c 

OBJS += \
./bad.o 

C_DEPS += \
./depend/bad.u 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MetaWare ARC C/C++ Compiler'
	hcac -c -O1 -g -Hnocopyr -Hsdata0 -arc600 -Humake -Hdepend="depend" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


