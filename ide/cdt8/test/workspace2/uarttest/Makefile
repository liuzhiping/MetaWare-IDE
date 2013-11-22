all:	uarttest.elf
MQX=C:/ARC/mqx2.50_acaa3a6.met
USER_OBJS := $(MQX)/lib/acaa3a6.met/met.o $(MQX)/lib/ARCA6.met/mqx.a $(MQX)/lib/acaa3a6.met/acaa3a6.a $(MQX)/lib/acaa3a6.met/link.met

uarttest.elf:	uarttest.o
	@echo 'Building target: $@'
	@echo 'Invoking: MetaWare Linker'
	hcac  -Hldopt=-q -Bstatic -Bgrouplib -Heos=mqx -e_start -arc600 uarttest.o -o "uarttest.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '


uarttest.o:	uarttest.c
	@echo 'Building file: $<'
	@echo 'Invoking: MetaWare ARC C/C++ Compiler'
	hcac -c -O -g -DPSP_NUM_EXTENSION_REGISTERS=0 -DPSP_EXTENSION_REGISTERS=0 -DMQX_EXTENDED_INTS_EXIST -D__MET__ -DMQX_CPU=0xACA6 -I$(MQX)/source/include -I$(MQX)/source/io -I$(MQX)/lib/arca6.met -I$(MQX)/lib/acaa3a6.met -Hnocopyr -HL "-Hpragma=Offwarn(572)" "-Hpragma=Offwarn(827)" -Hon=long_enums -Hsdata0 -arc600 -Hoff=volatile_cache_bypass  -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '