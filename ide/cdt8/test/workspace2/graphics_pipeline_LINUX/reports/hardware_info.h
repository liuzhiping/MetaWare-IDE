/*
 * This file contains the fully-qualified names of processors and memory
 * components defined in the 'hwarch' hardware architecture.
 * These names are used as follows : 
 * - processor names are required by the software architecture to create OSes
 * - memory components and the bank paths are required when mapping
 *   sections and some coordination API objects

****** PROCESSORS ******

cpu0.core		cpu_type: arc700
cpu1.core		cpu_type: arc700
cpu2.core		cpu_type: arc700
cpu3.core		cpu_type: arc700

****** MEMORY COMPONENTS and BANK PATHS ******

ssramInst		component type: SSRAM
	mmap.bank1.ram0 0x00000000 0x02000000 32
	mmap.bank2.ram1 0x02000000 0x00001000 32


*/

