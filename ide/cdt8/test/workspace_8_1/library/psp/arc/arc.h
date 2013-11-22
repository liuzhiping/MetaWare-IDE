#ifndef __arc_h__
#define __arc_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: arc.h
***                                                            
*** Comments: 
*** 
***    This file contains the type definitions for the ARC core processor.
***
***************************************************************************
*END***********************************************************************/

/* Compiler pragmas */

/* Turn off the emiting of load.di instructions
** for all volatile data structures
*/
#pragma Off(Volatile_cache_bypass)

/*==========================================================================*/
/*
**                   MQX REQUIRED DEFINITIONS
**
** Other MQX kernel and component functions require these definitions to exist.
*/

/*---------------------------------------------------------------------------*/

/* Indicate which endian this PSP is */
#ifdef _ARC_BE
#define PSP_ENDIAN                 MQX_BIG_ENDIAN
#else
#define PSP_ENDIAN                 MQX_LITTLE_ENDIAN
#endif

/* 
** Indicate the direction of the stack
*/
#define PSP_STACK_GROWS_TO_LOWER_MEM         (1)

/*
** Indicate addressing capability of the CPU
** This is the memory width. i.e., the number of bits addressed
** by each memory location.
*/
#define PSP_MEMORY_ADDRESSING_CAPABILITY (8)

/*
** Indicate alignment restrictions on memory accesses
** For an n-bit memory access,
**
** if n <  PSP_MEMORY_ACCESSING_CAPABILITY,
**         n-bit accesses must be n-bit-aligned
**
** if n >= PSP_MEMORY_ACCESSING_CAPABILITY,
**         n-bit accesses must be PSP_MEMORY_ACCESSING_CAPABILITY-bit-aligned
*/
#define PSP_MEMORY_ACCESSING_CAPABILITY (32)


/* 
**   MINIMUM STACK SIZE FOR A TASK
*/

/* This much extra stack is required for the logging of mqx functions */
#if MQX_KERNEL_LOGGING
#define PSP_STACK_KLOG_OVERHEAD      (128)
#else
#define PSP_STACK_KLOG_OVERHEAD      (0)
#endif

#ifdef __PROFILE__
#define PSP_STACK_PROFILE_OVERHEAD   (64)
#else
#define PSP_STACK_PROFILE_OVERHEAD   (0)
#endif

/* This much extra stack needed to handle a stack frame for the
** called task.
*/
#define PSP_STACK_FRAME_OVERHEAD (4*4)

/* Start CR 2396 */
#if MQX_USE_PMU
#define PSP_STACK_PMU_OVERHEAD   (256)
#else
#define PSP_STACK_PMU_OVERHEAD   (0)
#endif
/* End CR 2396 */

/* Minimum stack size for all tasks. */
/* Start CR 2396 */
/*
#define PSP_MINSTACKSIZE \
   (sizeof(PSP_STACK_START_STRUCT) + \
   PSP_STACK_PROFILE_OVERHEAD + \
   PSP_STACK_FRAME_OVERHEAD + \
   PSP_STACK_KLOG_OVERHEAD)
*/
#define PSP_MINSTACKSIZE \
   (sizeof(PSP_STACK_START_STRUCT) + \
   PSP_STACK_PROFILE_OVERHEAD + \
   PSP_STACK_FRAME_OVERHEAD + \
   PSP_STACK_KLOG_OVERHEAD + \
   PSP_STACK_PMU_OVERHEAD)
/* End CR 2396 */

/* 
** Minimum stack size for the Idle Task 
*/
/* Start CR 2334 */
/*#define PSP_IDLE_STACK_SIZE  (PSP_MINSTACKSIZE + 28 + 1024) */
#define PSP_IDLE_STACK_SIZE  (PSP_MINSTACKSIZE + 28)
/* End CR 2334 */

/* Addresses do not need to be normalized 
** (ie as for example the Intel chips in real mode do require this)
*/
#define _PSP_NORMALIZE_MEMORY(x) (x)

/*
** Special PSP flag indicating whether scratch registers are saved for
** a blocked task, stored in the TD_STRUCT FLAGS field
*/

/*==========================================================================*/
/*
**                    PSP SPECIFIC DEFINITIONS
**
** These definitions will change from PSP to PSP
*/

/* Processor family */
#define PSP_ARC5  1

/* Processor type definitions for this family of cpus */
#define PSP_CPU_TYPE_ARC_TANGENT_A5 (0x11A5)  /* ARCtangent-A5 */

#define PSP_CPU_TYPE_ARC_TANGENT_A52 (0x1A52)  /* ARCtangent-A5.2 */

#define PSP_CPU_TYPE_ARC_A600        (0x1A60)  /* ARC A600 */

#define PSP_CPU_TYPE_ARC_700         (0x1A70)  /* ARC 700 */

/*
** Bits found in the Status Register
*/
#define PSP_SR_ZERO               (0x80000000)
#define PSP_SR_NEGATIVE           (0x40000000)
#define PSP_SR_CARRY              (0x20000000)
#define PSP_SR_OVERFLOW           (0x10000000)
#define PSP_SR_E2                 (0x08000000)
#define PSP_SR_E1                 (0x04000000)
#define PSP_SR_HALT               (0x02000000)
#define PSP_SR_PC_MASK            (0x00FFFFFF)

/*
** Standard auxiliary registers
*/
#define PSP_AUX_STATUS            (0x00)
#define PSP_AUX_SEMAPHORE         (0x01)
#define PSP_AUX_LP_START          (0x02)
#define PSP_AUX_LP_END            (0x03)
#define PSP_AUX_IDENTITY          (0x04)
#define PSP_AUX_DEBUG             (0x05)

/* Code cache registers */
#define PSP_AUX_IVIC              (0x10)
#define PSP_AUX_CHE_MODE          (0x11)
#define PSP_AUX_LOCKLINE          (0x12)
#define PSP_AUX_UNLOCKLINE        (0x13)
#define PSP_AUX_TAG_A_MASK        (0x15)
#define PSP_AUX_TAG_D_MASK        (0x16)
#define PSP_AUX_LN_MASK           (0x17)

/* Do you want to verify kernel data can be read and written correctly */
#define PSP_KERNEL_DATA_VERIFY_ENABLE   \
        ((uint_32)__KERNEL_DATA_VERIFY_ENABLE) /* CR 1327 */


/*==========================================================================*/

/* Assembler macros */

#define _psp_get_aux(aux_reg) \
   (uint_32)_lr((unsigned)(aux_reg))

#define _psp_set_aux(aux_reg,value) \
   _sr((unsigned)(value),(unsigned)(aux_reg))

_Asm uint_8 _psp_get_periph_b(pointer addr) {
   %reg addr
      ldb.di %r0,[addr]
}

_Asm void _psp_set_periph_b(pointer addr,uint_8 val) {
   %reg addr,val
      stb.di val,[addr]
}

_Asm uint_16 _psp_get_periph_w(pointer addr) {
   %reg addr
      ldw.di %r0,[addr]
}

_Asm void _psp_set_periph_w(pointer addr,uint_16 val) {
   %reg addr,val
      stw.di val,[addr]
}

_Asm uint_32 _psp_get_periph_l(pointer addr) {
   %reg addr
      ld.di %r0,[addr]
}

_Asm void _psp_set_periph_l(pointer addr,uint_32 val) {
   %reg addr,val
      st.di val,[addr]
}
   
/*==========================================================================*/
/*                         PSP DATA TYPES                                   */

/*--------------------------------------------------------------------------*/
/*
** PSP BACK TRACE STRUCT
**
** This structure is pointed to by the frame pointer and is generated
** on each function call by the caller.
*/
typedef struct psp_back_trace_struct
{
   /* Pointer to previous back trace register */
   struct psp_back_trace_struct _PTR_ SAVED_FP;

   /* the return address (BLINK) register */
   pointer                            RETURN_ADDRESS;

   /* the static link frame pointer */
   pointer                            LINK;

   /* A reserved field */
   uint_32                            RESERVED;

} PSP_BACK_TRACE_STRUCT, _PTR_ PSP_BACK_TRACE_STRUCT_PTR;

/*==========================================================================*/
/*                         MQX DATA TYPES                                   */

/*--------------------------------------------------------------------------*/
/*
** PSP SUPPORT STRUCT
** This structure is used to maintain the PSP support information
*/
typedef struct psp_support_struct
{
   /* The number of milliseconds per tick */
   uint_32 MS_PER_TICK;

   /* 
   ** Accuracy of MS_PER_TICK field. Did the caculation produce a whole number 
   ** or did truncation occur
   */
   boolean MS_PER_TICK_IS_INT;

   /* How many extra exension registers are saved during a context switch */
   uint_32 NUM_EXTENSION_REGISTERS;
   
   /* Bit mask of the extension registers used
   ** bit 0, indicates that R32 is available,
   ** bit 1, indicates that R33 is available, etc
   */
   uint_32 EXTENSION_REGISTERS;
   
} PSP_SUPPORT_STRUCT, _PTR_ PSP_SUPPORT_STRUCT_PTR;
 

/*-----------------------------------------------------------------------*/
/*
** PSP BLOCKED STACK STRUCT
**
** This is what a stack looks like for a task that is NOT the active
** task
*/
typedef struct psp_blocked_stack_struct
{
   uint_32 R0;   /* Integer result, Argument 1 */
   uint_32 R1;   /* Argument 2 */
   uint_32 R2;   /* Argument 3 */
   uint_32 R3;   /* Argument 4 */
   uint_32 R12;  /* Scratch, temporary calculations */
   uint_32 R13;  /* Saved by Callee */
   uint_32 R14;  /* Saved by Callee */
   uint_32 R15;  /* Saved by Callee */
   uint_32 LP_COUNT;  /* R60 */
   uint_32 LP_START;
   uint_32 LP_END;
   uint_32 BLINK;   /* Branch link register */
   uint_32 FLAGS;   /* Condition Flags (and PC on ARC Tangent A4) */
   uint_32 RETURN_ADDRESS;  /* PC Return address only used on Tangent A5*/
   uint_32 ILINK1;  /* ILINK1 register */

   uint_32 R4;   /* Argument 5 */
   uint_32 R5;   /* Argument 6 */
   uint_32 R6;   /* Argument 7 */
   uint_32 R7;   /* Argument 8 */
   uint_32 R8;   /* Scratch */
   uint_32 R9;   /* Scratch */
   uint_32 R10;  /* Scratch */
   uint_32 R11;  /* Scratch */
   uint_32 R16;  /* Saved by Callee */
   uint_32 R17;  /* Saved by Callee */
   uint_32 R18;  /* Saved by Callee */
   uint_32 R19;  /* Saved by Callee */
   uint_32 R20;  /* Saved by Callee */
   uint_32 R21;  /* Saved by Callee */
   uint_32 R22;  /* Saved by Callee */
   uint_32 R23;  /* Saved by Callee */
   uint_32 R24;  /* Saved by Callee */
   uint_32 R25;  /* Saved by Callee */
   uint_32 R26;  /* (gp) [Never modified] may or may not be used */
   uint_32 R27;  /* Frame pointer (fp) always points to back-trace structure */

   /* uint_32 R28;  Stack top pointer (sp) [in Task descriptor] */
   /* uint_32 R29;  Maskable interrupt link register 1 */
   /* uint_32 R30;  Maskable interrupt link register 2 */
   /* uint_32 R31;  Branch link register               */
   /* uint_32 R61;  Short immediate data indicator setting flags */
   /* uint_32 R62;  Long immediate data indicator */
   /* uint_32 R63;  Short immediate data indicator not setting flags */
   
#if PSP_NUM_EXTENSION_REGISTERS
   uint_32 EXTENSION_REGISTERS[PSP_NUM_EXTENSION_REGISTERS];
#endif

   /* The DSP-C compiler will use the accumulators for normal int multiplies */
/* Start CR 2180 */
//#if (defined(__Xxmac_24) || defined(__Xxmac_d16))
#if (defined(__Xxmac_24) || defined(__Xxmac_d16) || defined(__Xmul32x16))
/* End CR 2180 */
   /* XMAC Control Reg - AUX_MACMODE */
   uint_32 AUX_MACMODE;
# ifdef __Xxmac_d16
   uint_32 CORE_ACC1_R56;
   uint_32 CORE_ACC2_R57;
   uint_32 AUX_XMAC0_16;
   uint_32 AUX_XMAC1_16;
   uint_32 AUX_XMAC2_16;
# endif
# ifdef __Xxmac_24
   uint_32 AUX_XMAC0_24;
   uint_32 AUX_XMAC1_24;
   uint_32 AUX_XMAC2_24;
# endif
/* Start CR 2180 */
# ifdef __Xmul32x16
   uint_32 AUX_XMACLW_H;
   uint_32 AUX_XMACLW_L; 
# endif
/* End CR 2180 */
#endif

#if MQX_CPU == 0xACA7
   uint_32 BTA; /* Delay slot branch target address */
#endif

   uint_32 PSP_BLOCKED_STACK_STRUCT_LAST_FIELD[0]; /* must be last field */
} PSP_BLOCKED_STACK_STRUCT, _PTR_ PSP_BLOCKED_STACK_STRUCT_PTR;


/*-----------------------------------------------------------------------*/
/*
** PSP INTERRUPT CONTEXT STRUCT
**
** This structure provides a "context" for mqx primitives to use while executing
** in an interrupt handler.  A link list of these contexts is built on the 
** interrupt stack.  The head of this list (the current interrupt context) is 
** pointed to by the INTERRUPT_CONTEXT_PTR field of the kernel data structure.
**
*/
typedef struct psp_int_context_struct
{

   /* The "task" error code for use by mqx functions while in the ISR */
   uint_32 ERROR_CODE;

   /* Used by the _int_enable function while in the ISR */
   uint_16 ENABLE_SR;
   
   /* The interrupt vector number for this context */
   uint_16 EXCEPTION_NUMBER;

   /* Address of previous context, NULL if none */
   struct psp_int_context_struct _PTR_ PREV_CONTEXT;

} PSP_INT_CONTEXT_STRUCT, _PTR_ PSP_INT_CONTEXT_STRUCT_PTR;


/*-----------------------------------------------------------------------*/
/*
** PSP STACK START STRUCT
**
** This structure is used during the initialization of a new task.
** It is overlaid on the bottom of the task's stack
**
*/
typedef struct psp_stack_start_struct
{ 

   /* The start up registers for the task */
   PSP_BLOCKED_STACK_STRUCT     INITIAL_CONTEXT;
   
   /* The standard stack back-trace data structure */
   PSP_BACK_TRACE_STRUCT        BACK_TRACE;

   /* The task's parameter */
   uint_32                      PARAMETER;

} PSP_STACK_START_STRUCT, _PTR_ PSP_STACK_START_STRUCT_PTR; 

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF PSP FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

extern uchar     __KERNEL_DATA_VERIFY_ENABLE[]; 

/* Generic PSP prototypes */
extern _mqx_uint _arc_initialize_support(void);
extern _mqx_uint _psp_int_init(uint_32, uint_32);

/* PSP Specific prototypes */
extern uint_32   _psp_get_sr(void);
extern uint_32   _psp_set_sr(uint_32);
extern void      _psp_profile_init(uint_32, uint_32, uint_32, uint_32,
   uint_32, uint_32, uint_32, uint_32);
extern void      _psp_set_int_level(uint_32, uint_32);

/* Metaware profiling support */
extern uint_32  _mwprofile_clock_freq(void);
extern void     _mwstart_profile_clock(void);   
extern void     _mwstop_profile_clock(void);   

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
