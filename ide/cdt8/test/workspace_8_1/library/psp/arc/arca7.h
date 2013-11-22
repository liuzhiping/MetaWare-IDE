#ifndef __arca7_h__
#define __arca7_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: arca7.h
***                                                            
*** Comments: 
*** 
***    This file contains the type definitions for the ARC 700 core.
***
***************************************************************************
*END***********************************************************************/

/* Define the PSP variant */
#define PSP_ARC_A7        	   1

/* Defines the number of chipselects for BVCI controller */
#define AC_BVCI_CS_NUM             (4)

/* CONFIGURATION INFORMATION */
/* Define PSP_EXTENDED_INTS_EXIST to 1 for the full 32 bits of interrupts */
/* BB@Nov. 1/05 : This macro must be set to 1 for ARC 700. Also macro */
/* PSP_ARC700_INTS_EXIST must be defined and set to 1 */
#ifndef PSP_EXTENDED_INTS_EXIST
#define PSP_EXTENDED_INTS_EXIST    (1)
#endif

/* Start CR 1946 */
/* CONFIGURATION INFORMATION */
/* Define PSP_ARC700_INTS_EXIST to 1 for ARC700 exceptions support */
#ifndef PSP_ARC700_INTS_EXIST
#define PSP_ARC700_INTS_EXIST      (1)
#endif
/* End CR 1946 */

/*
** Cache and MMU definition values
*/
#ifndef PSP_HAS_MMU
#define PSP_HAS_MMU                (0)
#endif
#ifndef PSP_HAS_CODE_CACHE
#define PSP_HAS_CODE_CACHE         (1)
#endif

#ifndef PSP_HAS_DATA_CACHE
#define PSP_HAS_DATA_CACHE         (1)
#endif
#ifndef PSP_CACHE_HAS_BUS_SNOOPING
#define PSP_CACHE_HAS_BUS_SNOOPING (0)
#endif
// Cache line size in bytes - configurable in ARChitect
#ifndef PSP_CACHE_LINE_SIZE
/* Start CR 2269 */
//#define PSP_CACHE_LINE_SIZE        (4)
#define PSP_CACHE_LINE_SIZE        (32)
/* End CR 2269 */
#endif

/*===========================================================================*/

/*
** Memory alignment requirements.
** The alignment indicates how memory is to be aligned for all memory
** accesses.   This is to avoid mis-aligned transfers of data, thus
** optimizing memory accesses.
*/
#if PSP_CACHE_LINE_SIZE
#define PSP_MEMORY_ALIGNMENT       (PSP_CACHE_LINE_SIZE-1)
#else
#define PSP_MEMORY_ALIGNMENT       (3)
#endif
#define PSP_MEMORY_ALIGNMENT_MASK  (~PSP_MEMORY_ALIGNMENT)

/*
** Stack alignment requirements.
** The alignment indicates how the stack is to be initially aligned.
** This is to avoid mis-aligned types on the stack
*/
#define PSP_STACK_ALIGNMENT       PSP_MEMORY_ALIGNMENT
#define PSP_STACK_ALIGNMENT_MASK  PSP_MEMORY_ALIGNMENT_MASK

/*
** Define padding need to make the STOREBLOCK_STRUCT aligned properly
*/
#ifdef PSP_HAS_DATA_CACHE
#if (PSP_CACHE_LINE_SIZE > 32)
#define PSP_MEM_STOREBLOCK_ALIGNMENT   ((PSP_CACHE_LINE_SIZE/4)-6)
#else
#define PSP_MEM_STOREBLOCK_ALIGNMENT               (2)
#endif
#else
#define PSP_MEM_STOREBLOCK_ALIGNMENT               (2)
#endif

/*
** Standard cache macros
*/
#if PSP_HAS_DATA_CACHE
#define _DCACHE_ENABLE(n)                _dcache_enable(n)
#define _DCACHE_DISABLE()                _dcache_disable()
#define _DCACHE_FLUSH()                  _dcache_flush()
#define _DCACHE_FLUSH_LINE(p)            _dcache_flush_line(p)
#define _DCACHE_FLUSH_MLINES(p, n)       _dcache_flush_mlines(p, n)
#define _DCACHE_INVALIDATE()             _dcache_invalidate()
#define _DCACHE_INVALIDATE_LINE(p)       _dcache_invalidate_line(p)
#define _DCACHE_INVALIDATE_MLINES(p, n)  _dcache_invalidate_mlines(p, n)
#define _DCACHE_LOCK_LINE(p)             _dcache_lock_line(p)
#else
#define _DCACHE_ENABLE(n)                
#define _DCACHE_DISABLE()                
#define _DCACHE_FLUSH()                  
#define _DCACHE_FLUSH_LINE(p)            
#define _DCACHE_FLUSH_MLINES(p, n)       
#define _DCACHE_INVALIDATE()             
#define _DCACHE_INVALIDATE_LINE(p)       
#define _DCACHE_INVALIDATE_MLINES(p, n)  
#define _DCACHE_LOCK_LINE(p)                
#endif

#if PSP_HAS_CODE_CACHE                 
#define _ICACHE_ENABLE(n)                 _psp_set_aux(PSP_AUX_IC_ICTRL, n)
#define _ICACHE_INVALIDATE()              _icache_invalidate()
#define _ICACHE_DISABLE()                 _psp_set_aux(PSP_AUX_IC_ICTRL, _psp_get_aux(PSP_AUX_IC_ICTRL) | 0x1)
#else
#define _ICACHE_ENABLE(n)                 
#define _ICACHE_DISABLE()
#define _ICACHE_INVALIDATE()
#endif
#define _ICACHE_INVALIDATE_LINE(p)        
#define _ICACHE_INVALIDATE_MLINES(p, n)   


/*
** The maximum number of hardware interrupt vectors
*/
#define PSP_MAXIMUM_INTERRUPT_VECTORS (39)

/*
** Exception vector numbers
*/
#define PSP_EXCPT_RESET_VECTOR             (0)
#define PSP_EXCPT_MEMORY_EXCEPTION_VECTOR  (1)
#define PSP_EXCPT_INSTRUCTION_ERROR_VECTOR (2)
/* Legacy interrupts */
#define PSP_EXCPT_3_VECTOR                 (3)
#define PSP_EXCPT_4_VECTOR                 (4)
#define PSP_EXCPT_5_VECTOR                 (5)
#define PSP_EXCPT_6_VECTOR                 (6)
#define PSP_EXCPT_7_VECTOR                 (7)
/* New base case interrupts */
#define PSP_EXCPT_8_VECTOR                 (8)
#define PSP_EXCPT_9_VECTOR                 (9)
#define PSP_EXCPT_10_VECTOR                (10)
#define PSP_EXCPT_11_VECTOR                (11)
#define PSP_EXCPT_12_VECTOR                (12)
#define PSP_EXCPT_13_VECTOR                (13)
#define PSP_EXCPT_14_VECTOR                (14)
#define PSP_EXCPT_15_VECTOR                (15)

/* Extension interrupts */
#define PSP_EXCPT_16_VECTOR                (16)
#define PSP_EXCPT_17_VECTOR                (17)
#define PSP_EXCPT_18_VECTOR                (18)
#define PSP_EXCPT_19_VECTOR                (19)
#define PSP_EXCPT_20_VECTOR                (20)
#define PSP_EXCPT_21_VECTOR                (21)
#define PSP_EXCPT_22_VECTOR                (22)
#define PSP_EXCPT_23_VECTOR                (23)
#define PSP_EXCPT_24_VECTOR                (24)
#define PSP_EXCPT_25_VECTOR                (25)
#define PSP_EXCPT_26_VECTOR                (26)
#define PSP_EXCPT_27_VECTOR                (27)
#define PSP_EXCPT_28_VECTOR                (28)
#define PSP_EXCPT_29_VECTOR                (29)
#define PSP_EXCPT_30_VECTOR                (30)
#define PSP_EXCPT_31_VECTOR                (31)

#define PSP_EXCPT_MACHINE_CHECK_EXCEPTION_VECTOR              (32)
#define PSP_EXCPT_TLB_INTRUCTION_MISS_EXCEPTION_VECTOR        (33)
#define PSP_EXCPT_TLB_DATA_MISS_EXCEPTION_VECTOR              (34)
#define PSP_EXCPT_TLB_PROTECTION_VIOLATION_EXCEPTION_VECTOR   (35)
#define PSP_EXCPT_PRIVILEGE_VIOLATION_EXCEPTION_VECTOR        (36)
#define PSP_EXCPT_TRAP_EXCEPTION_VECTOR                       (37)
#define PSP_EXCPT_EXTENSION_INTRUCTION_EXCEPTION_VECTOR       (38)


/* ARC 700 status32 register bits */
#define PSP_SR32_L                         (0x00001000) /* Loop inhibit (1 = disabled, 0 = enabled) */
#define PSP_SR32_ZERO                      (0x00000800)
#define PSP_SR32_NEGATIVE                  (0x00000400)
#define PSP_SR32_CARRY                     (0x00000200)
#define PSP_SR32_OVERFLOW                  (0x00000100)
#define PSP_SR32_U                         (0x00000080) /* User mode (1 = user, 0 = kernel) */
#define PSP_SR32_DE                        (0x00000040) /* Delay slot - Only readable by an external debugger (LR returns zero) */
#define PSP_SR32_AE                        (0x00000020) /* Exception active (Exception Return Address live) */
#define PSP_SR32_A2                        (0x00000010) /* Interrupt level 2 ISR active (ILINK2 live) */
#define PSP_SR32_A1                        (0x00000008) /* Interrupt level 1 ISR active (ILINK1 live) */
#define PSP_SR32_E2                        (0x00000004)
#define PSP_SR32_E1                        (0x00000002)
#define PSP_SR32_H                         (0x00000001)

/* ARC 700 Aux registers */
#define PSP_AUX_PC32                       (0x006)
#define PSP_AUX_STATUS32                   (0x00A)
#define PSP_AUX_STATUS32_L1                (0x00B)
#define PSP_AUX_STATUS32_L2                (0x00C)

#define PSP_AUX_IC_IVIC                    (0x010)
#define PSP_AUX_IC_ICTRL                   (0x011)
#define PSP_AUX_CODE_RAM                   (0x014)
#define PSP_AUX_LOCAL_RAM                  (0x018)
#define PSP_AUX_TCOUNT0                    (0x021)
#define PSP_AUX_TCONTROL0                  (0x022)
#define PSP_AUX_TLIMIT0                    (0x023)
#define PSP_AUX_BCR_VECBASE                (0x025)
#define PSP_AUX_IRQ_LV12                   (0x043)
#define PSP_AUX_DC_IVDC                    (0x047)
#define PSP_AUX_DC_CTRL                    (0x048)
#define PSP_AUX_DC_LDL                     (0x049)
#define PSP_AUX_DC_IVDL                    (0x04A)
#define PSP_AUX_DC_FLSH                    (0x04B)
#define PSP_AUX_DC_FLDL                    (0x04C)
#define PSP_AUX_DC_RAM_ADDR                (0x058)
#define PSP_AUX_DC_TAG                     (0x059)
#define PSP_AUX_DC_WP                      (0x05A)
#define PSP_AUX_DC_DATA                    (0x05B)
#define PSP_AUX_D_CACHE_BUILD              (0x072)
#define PSP_AUX_LDSTRAM_BUILD              (0x074)
#define PSP_AUX_I_CACHE_BUILD              (0x077)
#define PSP_AUX_TCOUNT1                    (0x100)
#define PSP_AUX_TCONTROL1                  (0x101)
#define PSP_AUX_TLIMIT1                    (0x102)
#define PSP_AUX_IRQ_LEV                    (0x200)
#define PSP_AUX_IRQ_HINT                   (0x201)
#define PSP_AUX_ERET                       (0x400)      /* Exception return address */
#define PSP_AUX_ERBTA                      (0x401)      /* Exception return branch target address */
#define PSP_AUX_ERSTATUS                   (0x402)      /* Exception return status */
#define PSP_AUX_ECR                        (0x403)      /* Exception cause register */
#define PSP_AUX_EFA                        (0x404)      /* Exception fault address */
#define PSP_AUX_ICAUSE1                    (0x40A)      /* L1 Interrupt cause */
#define PSP_AUX_ICAUSE2                    (0x40B)      /* L2 Interrupt cause */
#define PSP_AUX_IENABLE                    (0x40C)      /* Interrupt mask programming */
#define PSP_AUX_ITRIGGER                   (0x40D)      /* Interrupt sensitivity programming */
#define PSP_AUX_BTA                        (0x412)	/* Branch Target Address register */
#define PSP_AUX_BTA_L1                     (0x413)	/* Level 1 return branch target */
#define PSP_AUX_BTA_L2                     (0x414)	/* Level 2 return branch target */
#define PSP_AUX_IRQ_PULSE_CANCEL           (0x415)      /* Interrupt pulse cancel */
#define PSP_AUX_IRQ_PENDING                (0x416)      /* Interrupt pending */

/* 
** Data cache
*/
/* Data cache control register bits */
#define PSP_AUX_DC_CTRL_DC_ENABLE                (0x00000000)
#define PSP_AUX_DC_CTRL_DC_DISABLE               (0x00000001)
#define PSP_AUX_DC_CTRL_ENABLE_BYPASS            (0x00000002)
#define PSP_AUX_DC_CTRL_SUCCESS                  (0x00000004)
#define PSP_AUX_DC_CTRL_RANDOM_REPLACEMENT       (0x00000000)
#define PSP_AUX_DC_CTRL_ROUND_ROBIN_REPLACEMENT  (0x00000008)
#define PSP_AUX_DC_CTRL_CACHE_CONTROL_RAM_ACCESS (0x00000020)
#define PSP_AUX_DC_CTRL_INVALIDATE_FLUSHES       (0x00000040)
#define PSP_AUX_DC_CTRL_FLUSH_LOCKED_ENTRIES     (0x00000080)
#define PSP_AUX_DC_CTRL_FLUSH_STATUS             (0x00000100)

/* General BCR register bits */
#define PSP_AUX_GENERAL_BUILD_VERSION_MASK       (0x000000FF)

/* Data cache BCR register bits */
#define PSP_AUX_D_CACHE_BUILD_VERSION_MASK       (0x000000FF)

#define PSP_AUX_D_CACHE_BUILD_CONFIG_MASK        (0x00000F00)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_1_WAY       (0x00000000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_2_WAY       (0x00000100)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_4_WAY       (0x00000200)

#define PSP_AUX_D_CACHE_BUILD_CONFIG_RAM_SZ_MASK (0x0000F000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_0_5K        (0x00000000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_1K          (0x00001000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_2K          (0x00002000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_4K          (0x00003000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_8K          (0x00004000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_16K         (0x00005000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_32K         (0x00006000)

#define PSP_AUX_D_CACHE_BUILD_CONFIG_LINE_L_MASK (0x000F0000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_8_BYTES     (0x00000000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_16_BYTES    (0x00010000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_32_BYTES    (0x00020000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_64_BYTES    (0x00030000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_128_BYTES   (0x00040000)
#define PSP_AUX_D_CACHE_BUILD_CONFIG_256_BYTES   (0x00050000)

/* 
** Instruction cache
*/
/* control register bits */
#define PSP_AUX_IC_CTRL_IC_ENABLE                (0x00000000)
#define PSP_AUX_IC_CTRL_IC_DISABLE               (0x00000001)
#define PSP_AUX_IC_CTRL_ENABLE_BYPASS            (0x00000002)
#define PSP_AUX_IC_CTRL_ENABLE_CODERAM           (0x00000004)
#define PSP_AUX_IC_CTRL_SUCCESS                  (0x00000008)
#define PSP_AUX_IC_CTRL_RANDOM_REPLACEMENT       (0x00000000)
#define PSP_AUX_IC_CTRL_ROUND_ROBIN_REPLACEMENT  (0x00000010)
#define PSP_AUX_IC_CTRL_CACHE_CONTROL_RAM_ACCESS (0x00000020)

/* BCR register bites */
#define PSP_AUX_I_CACHE_BUILD_VERSION_MASK       (0x000000FF)

#define PSP_AUX_I_CACHE_BUILD_CONFIG_MASK        (0x00000F00)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_1_WAY       (0x00000000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_1_WAY_CR    (0x00000100)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_2_WAY       (0x00000300)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_4_WAY       (0x00000400)

#define PSP_AUX_I_CACHE_BUILD_CONFIG_RAM_SZ_MASK (0x0000F000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_0_5K        (0x00000000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_1K          (0x00001000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_2K          (0x00002000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_4K          (0x00003000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_8K          (0x00004000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_16K         (0x00005000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_32K         (0x00006000)

#define PSP_AUX_I_CACHE_BUILD_CONFIG_LINE_L_MASK (0x000F0000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_8_BYTES     (0x00000000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_16_BYTES    (0x00010000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_32_BYTES    (0x00020000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_64_BYTES    (0x00030000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_128_BYTES   (0x00040000)
#define PSP_AUX_I_CACHE_BUILD_CONFIG_256_BYTES   (0x00050000)


/*==========================================================================*/
/*                         MQX DATA TYPES                                   */

// ARC DSP version 3.1 extensions
#include <ac_dsp31.h>

// ARC Compact BVCI memory controller
#include <ac_bvci.h>

// ARC Compact memory management unit
#include <ac_tlb.h>

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF PSP FUNCTIONS
*/
#ifdef __cplusplus
extern "C" {
#endif

extern void _dcache_enable(uint_32);
extern void _dcache_disable(void);
extern void _dcache_flush(void);
extern void _dcache_flush_line(pointer);
extern void _dcache_flush_mlines(pointer,uint_32);
extern void _dcache_invalidate(void);
extern void _dcache_invalidate_line(pointer);
extern void _dcache_invalidate_mlines(pointer,uint_32);
extern void _dcache_lock_line(pointer);
extern void _icache_invalidate(void);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
