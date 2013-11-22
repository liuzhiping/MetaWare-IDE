/*
 * This file contains the fully-qualified names of processors and memory banks 
 * and busses defined in the 'hwarch' hardware architecture. These names are
 * used as follows : 
 * - processor names are required by the software architecture to create OSes
 * - memory banks names are required when mapping sections and
     some coordination API objects

****** PROCESSORS ******

hwarch.SoC.cpu0
hwarch.SoC.cpu1
hwarch.SoC.cpu2
hwarch.SoC.cpu3

****** MEMORY BANKS ******

hwarch.SoC.ssramInst_mmap_bank1_ram0
hwarch.SoC.ssramInst

****** BUSSES ******

hwarch.SoC.instruction_bus0
hwarch.SoC.data_bus0
hwarch.SoC.instruction_bus1
hwarch.SoC.data_bus1
hwarch.SoC.instruction_bus2
hwarch.SoC.data_bus2
hwarch.SoC.instruction_bus3
hwarch.SoC.data_bus3
hwarch.SoC.arbiter2bridge
hwarch.SoC.bridge2uart
hwarch.SoC.arbiter2ssramController
hwarch.SoC.ssramController2sram


*/

