#ifndef __mqx_prv_h__
#define __mqx_prv_h__
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
*** File: mqx_prv.h
***
*** Comments:
***   This file contains information private to the mqx kernel.
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                        KERNEL CONFIGURATION
**
** Each mqx kernel configuration item has an associated bit that is written
** into the configuration field of the kernel data structure at run time,
** so that the compiled configuration of the kernel is available to software
** and the debuggers.
*/

#define MQX_CNFG1_INCLUDE_FLOATING_POINT_IO            (0x0001)
#define MQX_CNFG1_USE_INLINE_MACROS                    (0x0002)
#define MQX_CNFG1_KERNEL_LOGGING                       (0x0004)
#define MQX_CNFG1_MONITOR_STACK                        (0x0008)
#define MQX_CNFG1_TASK_CREATION_BLOCKS                 (0x0010)
#define MQX_CNFG1_TASK_DESTRUCTION                     (0x0020)
#define MQX_CNFG1_COMPONENT_DESTRUCTION                (0x0040)
#define MQX_CNFG1_CHECK_ERRORS                         (0x0080)
#define MQX_CNFG1_CHECK_VALIDITY                       (0x0100)
#define MQX_CNFG1_CHECK_MEMORY_ALLOCATION_ERRORS       (0x0200)
#define MQX_CNFG1_USE_32BIT_MESSAGE_QIDS               (0x0400)
#define MQX_CNFG1_MEMORY_FREE_LIST_SORTED              (0x0800)
#define MQX_CNFG1_DEFAULT_TIME_SLICE_IN_TICKS          (0x1000)
#define MQX_CNFG1_LWLOG_TIME_STAMP_IN_TICKS            (0x2000)
#define MQX_CNFG1_PROFILING_ENABLE                     (0x4000)
#define MQX_CNFG1_RUN_TIME_ERR_CHECK_ENABLE            (0x8000)

#define MQX_CNFG2_USE_32BIT_TYPES                      (0x0001)
#define MQX_CNFG2_TIMER_USES_TICKS_ONLY                (0x0002)
#define MQX_CNFG2_EXIT_ENABLED                         (0x0004)
#define MQX_CNFG2_USE_IDLE_TASK                        (0x0008)
#define MQX_CNFG2_IS_MULTI_PROCESSOR                   (0x0010)
#define MQX_CNFG2_HAS_TIME_SLICE                       (0x0020)
#define MQX_CNFG2_MUTEX_HAS_POLLING                    (0x0040)
#define MQX_CNFG2_USE_LWMEM_ALLOCATOR                  (0x0080)
/* Start CR 2396 */
#define MQX_CNFG2_USE_PMU_ENABLED                      (0x0100)
/* End CR 2396 */


/* These bits will be set by dispatch.<comp> */
#define MQX_CNFG2_PSP_ASM_KERNEL_LOGGING               (0x8000)
#define MQX_CNFG2_PSP_ASM_FP_REGISTERS_EXIST           (0x4000)
#define MQX_CNFG2_PSP_ASM_MMU_CONTEXT_EXIST            (0x2000)
#define MQX_CNFG2_PSP_ASM_PROFILING_ENABLED            (0x1000)
#define MQX_CNFG2_PSP_ASM_RUN_TIME_ERR_CHECK_ENABLED   (0x0800)
#define MQX_CNFG2_PSP_ASM_DSP_REGISTERS_EXIST          (0x0400)

/* Compute value for kernel to write to CONFIG fields */
#define MQX_CNFG1 ( \
   (MQX_INCLUDE_FLOATING_POINT_IO ? MQX_CNFG1_INCLUDE_FLOATING_POINT_IO : 0) | \
   (MQX_USE_INLINE_MACROS ? MQX_CNFG1_USE_INLINE_MACROS : 0) | \
   (MQX_KERNEL_LOGGING ? MQX_CNFG1_KERNEL_LOGGING : 0) | \
   (MQX_MONITOR_STACK ? MQX_CNFG1_MONITOR_STACK : 0) | \
   (MQX_TASK_CREATION_BLOCKS ? MQX_CNFG1_TASK_CREATION_BLOCKS : 0) | \
   (MQX_TASK_DESTRUCTION ? MQX_CNFG1_TASK_DESTRUCTION : 0) | \
   (MQX_COMPONENT_DESTRUCTION ? MQX_CNFG1_COMPONENT_DESTRUCTION : 0) | \
   (MQX_CHECK_ERRORS ? MQX_CNFG1_CHECK_ERRORS : 0) | \
   (MQX_CHECK_VALIDITY ? MQX_CNFG1_CHECK_VALIDITY : 0) | \
   (MQX_CHECK_MEMORY_ALLOCATION_ERRORS ? MQX_CNFG1_CHECK_MEMORY_ALLOCATION_ERRORS : 0) | \
   (MQX_USE_32BIT_MESSAGE_QIDS ? MQX_CNFG1_USE_32BIT_MESSAGE_QIDS : 0) | \
   (MQX_MEMORY_FREE_LIST_SORTED ? MQX_CNFG1_MEMORY_FREE_LIST_SORTED : 0) | \
   (MQX_DEFAULT_TIME_SLICE_IN_TICKS ? MQX_CNFG1_DEFAULT_TIME_SLICE_IN_TICKS : 0) | \
   (MQX_LWLOG_TIME_STAMP_IN_TICKS ? MQX_CNFG1_LWLOG_TIME_STAMP_IN_TICKS : 0) | \
   (MQX_PROFILING_ENABLE ? MQX_CNFG1_PROFILING_ENABLE : 0) | \
   (MQX_RUN_TIME_ERR_CHECK_ENABLE ? MQX_CNFG1_RUN_TIME_ERR_CHECK_ENABLE : 0) \
   )

/* Start CR 2396 */
#if MQX_USE_PMU
#define DVFS_MAX_TIMER_FREQUENCY   4

#define MQX_CNFG2 ( \
   (MQX_USE_32BIT_TYPES ? MQX_CNFG2_USE_32BIT_TYPES : 0) | \
   (MQX_TIMER_USES_TICKS_ONLY ? MQX_CNFG2_TIMER_USES_TICKS_ONLY : 0) | \
   (MQX_EXIT_ENABLED ? MQX_CNFG2_EXIT_ENABLED : 0) | \
   (MQX_USE_IDLE_TASK ? MQX_CNFG2_USE_IDLE_TASK : 0) | \
   (MQX_IS_MULTI_PROCESSOR ? MQX_CNFG2_IS_MULTI_PROCESSOR : 0) | \
   (MQX_HAS_TIME_SLICE ? MQX_CNFG2_HAS_TIME_SLICE : 0) | \
   (MQX_MUTEX_HAS_POLLING ? MQX_CNFG2_MUTEX_HAS_POLLING : 0) | \
   (MQX_USE_LWMEM_ALLOCATOR ? MQX_CNFG2_USE_LWMEM_ALLOCATOR : 0) | \
   (MQX_USE_PMU ? MQX_CNFG2_USE_PMU_ENABLED : 0) \
   )
#else
#define MQX_CNFG2 ( \
   (MQX_USE_32BIT_TYPES ? MQX_CNFG2_USE_32BIT_TYPES : 0) | \
   (MQX_TIMER_USES_TICKS_ONLY ? MQX_CNFG2_TIMER_USES_TICKS_ONLY : 0) | \
   (MQX_EXIT_ENABLED ? MQX_CNFG2_EXIT_ENABLED : 0) | \
   (MQX_USE_IDLE_TASK ? MQX_CNFG2_USE_IDLE_TASK : 0) | \
   (MQX_IS_MULTI_PROCESSOR ? MQX_CNFG2_IS_MULTI_PROCESSOR : 0) | \
   (MQX_HAS_TIME_SLICE ? MQX_CNFG2_HAS_TIME_SLICE : 0) | \
   (MQX_MUTEX_HAS_POLLING ? MQX_CNFG2_MUTEX_HAS_POLLING : 0) | \
   (MQX_USE_LWMEM_ALLOCATOR ? MQX_CNFG2_USE_LWMEM_ALLOCATOR : 0) \
   )
#endif
/* End CR 2396 */

/*--------------------------------------------------------------------------*/
/*
**                    STRUCTURE OFFSET MACRO
**
**  This macro was used to calculate the offset of a structure field from
**  the start of that structure.
*/
#define FIELD_OFFSET(item,field) (_mqx_uint)&(((item _PTR_)0)->field)


/*--------------------------------------------------------------------------*/
/*
**                       MQX INITIALIZATION DEFINITIONS
*/

/*
** The maximum number of task templates for a MQX application.
*/
#define MQX_MAXIMUM_NUMBER_OF_TASK_TEMPLATES (0xFFFF)

/*
** The scaler to use to initialize the default time slice value. It is used
** in conjunction with MQX's tick rate (ticks per second). The default
** time slice frequency = ticks per second / MQX_DEFAULT_TIME_SLICE
*/
#define MQX_DEFAULT_TIME_SLICE  ((_mqx_uint)10)

#ifndef MQX_SETJMP
#define MQX_SETJMP(b)    setjmp(b)
#endif
#ifndef MQX_LONGJMP
#define MQX_LONGJMP(b,n) longjmp(b,n)
#endif


/*--------------------------------------------------------------------------*/
/*
**                        KERNEL LOGGING CONTROL
**
** If MQX_KERNEL_LOGGING is configured to non-zero, code will be compiled in
** that logs kernel activity to the MQX kernel log.
*/
#if MQX_KERNEL_LOGGING
# if MQX_CRIPPLED_EVALUATION
#  define MQX_KLOG_KILL_COUNT    (10000000)				/*C*/
#  include <setjmp.h>                                   /*C*/
   extern jmp_buf _mqx_exit_jump_buffer_internal;       /*C*/
#  define _KLOGM(x) x                                   /*C*/
#  if (PSP_ENDIAN == MQX_BIG_ENDIAN)                    /*C*/
#    define _KLOG(x) \
      kernel_data->END_OF_KERNEL_DATA_STRUCT[0]++;      /*C*/\
      if ((kernel_data->TIME.TICKS[1] > 240000) ||      /*C*/\
         (kernel_data->END_OF_KERNEL_DATA_STRUCT[0] > MQX_KLOG_KILL_COUNT)) \
      {  _INT_DISABLE();                                /*C*/\
         MQX_LONGJMP(_mqx_exit_jump_buffer_internal,1); /*C*/\
      } else if (kernel_data->LOG_CONTROL & 1) x        /*C*/
#  else /*PSP is LITTLE ENDIAN */
#    define _KLOG(x)                                    /*C*/\
      kernel_data->END_OF_KERNEL_DATA_STRUCT[0]++;      /*C*/\
      if ((kernel_data->TIME.TICKS[0] > 240000) ||      /*C*/\
         (kernel_data->END_OF_KERNEL_DATA_STRUCT[0] > MQX_KLOG_KILL_COUNT)) \
      {  _INT_DISABLE();                                /*C*/\
         MQX_LONGJMP(_mqx_exit_jump_buffer_internal,1); /*C*/\
      } else if (kernel_data->LOG_CONTROL & 1) x        /*C*/
#  endif /*PSP is LITTLE ENDIAN */
# else /* MQX_CRIPPLED_EVALUATION */
#  define _KLOGM(x) x
#  define _KLOG(x) if (kernel_data->LOG_CONTROL & 1) x
# endif /* MQX_CRIPPLED_EVALUATION */
#else /* MQX_KERNEL_LOGGING */
# if MQX_CRIPPLED_EVALUATION
#  define MQX_KLOG_KILL_COUNT    (10000000)				/*C*/
#  include <setjmp.h>                                   /*C*/
   extern jmp_buf _mqx_exit_jump_buffer_internal;  	    /*C*/
#  define _KLOGM(x) x
#  if (PSP_ENDIAN == MQX_BIG_ENDIAN)                    /*C*/
#    define _KLOG(x) \
      kernel_data->END_OF_KERNEL_DATA_STRUCT[0]++;      /*C*/\
      if ((kernel_data->TIME.TICKS[1] > 240000) ||      /*C*/\
         (kernel_data->END_OF_KERNEL_DATA_STRUCT[0] > MQX_KLOG_KILL_COUNT)) \
      {  _INT_DISABLE();                                /*C*/\
         MQX_LONGJMP(_mqx_exit_jump_buffer_internal,1); /*C*/\
      }                                                 /*C*/
#  else /*PSP is LITTLE ENDIAN */
#    define _KLOG(x)                                    /*C*/\
      kernel_data->END_OF_KERNEL_DATA_STRUCT[0]++;      /*C*/\
      if ((kernel_data->TIME.TICKS[0] > 240000) ||      /*C*/\
         (kernel_data->END_OF_KERNEL_DATA_STRUCT[0] > MQX_KLOG_KILL_COUNT)) \
      {  _INT_DISABLE();                                /*C*/\
         MQX_LONGJMP(_mqx_exit_jump_buffer_internal,1); /*C*/\
      }                                                 /*C*/
#  endif /*PSP is LITTLE ENDIAN */
# else /* MQX_CRIPPLED_EVALUATION */
#define _KLOGM(x)
#define _KLOG(x)
# endif /* MQX_CRIPPLED_EVALUATION */
#endif /* MQX_KERNEL_LOGGING */

/* NOTE: _klog_log now only accepts 6 parameters */
/* Function entry logging macros, */
#define _KLOGE1(fn) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGE2(fn,p1) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGE3(fn,p1,p2) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGE4(fn,p1,p2,p3) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)(p3), (_mqx_max_type)0);)
#define _KLOGE5(fn,p1,p2,p3,p4) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)(p3), (_mqx_max_type)(p4));)
#define _KLOGE6(fn,p1,p2,p3,p4,p5) _KLOG(_klog_log(KLOG_FUNCTION_ENTRY, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)(p3), (_mqx_max_type)(p4));)

/* Function exit logging macros */
#define _KLOGX1(fn) _KLOG(_klog_log(KLOG_FUNCTION_EXIT, \
   (_mqx_max_type)(fn), (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGX2(fn,p1) _KLOG(_klog_log(KLOG_FUNCTION_EXIT, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)0, (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGX3(fn,p1,p2) _KLOG(_klog_log(KLOG_FUNCTION_EXIT, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)0, (_mqx_max_type)0);)
#define _KLOGX4(fn,p1,p2,p3) _KLOG(_klog_log(KLOG_FUNCTION_EXIT, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)(p3), (_mqx_max_type)0);)
#define _KLOGX5(fn,p1,p2,p3,p4) _KLOG(_klog_log(KLOG_FUNCTION_EXIT, \
   (_mqx_max_type)(fn), (_mqx_max_type)(p1), (_mqx_max_type)(p2), (_mqx_max_type)(p3), (_mqx_max_type)(p4));)

/*--------------------------------------------------------------------------*/
/*
**                             MEMORY ALIGNMENTS
**
** It is important to maintain proper memory alignment of the kernel
** data structures in order to avoid the penalty of mis-aligned memory
** accesses.
*/

#if PSP_MEMORY_ALIGNMENT   /* We must align memory */

/* Make sure that the address is a multiple of the memory alignment */
#define _MEMORY_ALIGN_VAL_LARGER(val) \
   val = (((_mem_size)(val) + PSP_MEMORY_ALIGNMENT) & PSP_MEMORY_ALIGNMENT_MASK)
#define _MEMORY_ALIGN_VAL_SMALLER(val) \
   val = ((_mem_size)(val) & PSP_MEMORY_ALIGNMENT_MASK)

#define _ALIGN_ADDR_TO_HIGHER_MEM(mem_ptr) \
   (((_mem_size)(mem_ptr) + PSP_MEMORY_ALIGNMENT) & PSP_MEMORY_ALIGNMENT_MASK)
#define _ALIGN_ADDR_TO_LOWER_MEM(mem_ptr) \
   ((_mem_size)(mem_ptr) & PSP_MEMORY_ALIGNMENT_MASK)

/* Make sure that the stack address is a multiple of the stack alignment */
#define _STACK_ALIGN_VAL_LARGER(val) \
   val = (((_mem_size)(val) + PSP_STACK_ALIGNMENT) & PSP_STACK_ALIGNMENT_MASK)
#define _STACK_ALIGN_VAL_SMALLER(val) \
   val = ((_mem_size)(val) & PSP_STACK_ALIGNMENT_MASK)

#define _ALIGN_STACK_TO_HIGHER_MEM(mem_ptr) \
   (((_mem_size)(mem_ptr) + PSP_STACK_ALIGNMENT) & PSP_STACK_ALIGNMENT_MASK)
#define _ALIGN_STACK_TO_LOWER_MEM(mem_ptr) \
   ((_mem_size)(mem_ptr) & PSP_STACK_ALIGNMENT_MASK)


/* Is the memory address a multiple of the memory alignment */
#define _MEMORY_ALIGNED(mem_ptr) \
   (((_mem_size)(mem_ptr) & PSP_MEMORY_ALIGNMENT) ? FALSE : TRUE)

#define _STACK_ALIGNED(mem_ptr) \
   (((_mem_size)(mem_ptr) & PSP_STACK_ALIGNMENT) ? FALSE : TRUE)

#else
#define _ALIGN_ADDR_TO_HIGHER_MEM(val) val
#define _ALIGN_ADDR_TO_LOWER_MEM(val) val
#define _MEMORY_ALIGNED(mem_ptr) TRUE
#define _ALIGN_STACK_TO_HIGHER_MEM(val) val
#define _ALIGN_STACK_TO_LOWER_MEM(val) val
#define _STACK_ALIGNED(mem_ptr) TRUE
#endif

/*--------------------------------------------------------------------------*/
/*
**                        KERNEL STACK MONITORING
**
** If MQX_MONITOR_STACK is defined as non-zero, then all stacks
** are filled with the following value at stack initialization time.
*/
#define MQX_STACK_MONITOR_VALUE          (_mqx_uint)(0x7374616B)  /* "stak" */

/*--------------------------------------------------------------------------*/
/*
**                          STACK MACROS
**
** These macros determine the BASE and LIMIT of the stack, depending upon
** which direction the stack grow
*/
#if PSP_STACK_GROWS_TO_LOWER_MEM
#define _GET_STACK_BASE(mem_ptr, size) (pointer) \
   _ALIGN_STACK_TO_LOWER_MEM(((uchar_ptr)_PSP_NORMALIZE_MEMORY(mem_ptr) + size - 1))
#define _GET_STACK_LIMIT(mem_ptr, size)(pointer) \
   _ALIGN_STACK_TO_HIGHER_MEM(_PSP_NORMALIZE_MEMORY(mem_ptr))
#else
#define _GET_STACK_BASE(mem_ptr, size) (pointer) \
   _ALIGN_STACK_TO_HIGHER_MEM(_PSP_NORMALIZE_MEMORY(mem_ptr))
#define _GET_STACK_LIMIT(mem_ptr, size)(pointer) \
   _ALIGN_STACK_TO_LOWER_MEM(((uchar_ptr)_PSP_NORMALIZE_MEMORY(mem_ptr) + size - 1))
#endif

 /*--------------------------------------------------------------------------*/
/*
**                             TASK STATES
**
** The following define all of the states that a task have.
*/

/* All blocked states have this bit set */
#define IS_BLOCKED                      (0x001)

/* Some states may also have the task on a timeout queue when blocked */
#define IS_ON_TIMEOUT_Q                 (0x100)

/* Some states may also have their task descriptors on a queue when blocked */
#define TD_IS_ON_QUEUE                  (0x200)

/*
** Some states may also have their task descriptors on a queue using their
** AUX pointer fields in the TD
*/
#define TD_IS_ON_AUX_QUEUE              (0x400)

/* Mask to remove queueing and timeout bits */
#define STATE_MASK                      (0x0ff)

#define READY                           (0x02)
#define BLOCKED                         (0x02 | IS_BLOCKED)
#define RCV_SPECIFIC_BLOCKED            (0x04 | IS_BLOCKED)
#define RCV_ANY_BLOCKED                 (0x06 | IS_BLOCKED)
#define DYING                           (0x08 | IS_BLOCKED)
#define UNHANDLED_INT_BLOCKED           (0x0A | IS_BLOCKED)
#define SEND_BLOCKED                    (0x0C | IS_BLOCKED)
#define BREAKPOINT_BLOCKED              (0x0E | IS_BLOCKED)
#define IO_BLOCKED                      (0x10 | IS_BLOCKED | TD_IS_ON_QUEUE)

#define SEM_BLOCKED                     (0x20 | IS_BLOCKED)
#define MUTEX_BLOCKED                   (0x22 | IS_BLOCKED | TD_IS_ON_QUEUE)
#define EVENT_BLOCKED                   (0x24 | IS_BLOCKED)
#define TASK_QUEUE_BLOCKED              (0x28 | IS_BLOCKED | TD_IS_ON_QUEUE)
#define LWSEM_BLOCKED                   (0x2A | IS_BLOCKED | TD_IS_ON_AUX_QUEUE)
#define LWEVENT_BLOCKED                 (0x2C | IS_BLOCKED | TD_IS_ON_AUX_QUEUE)

/*
** The task ID of a task that is in the DYING state
*/
#define TASK_ID_DYING                   (0x80000000)

/*--------------------------------------------------------------------------*/
/*
**                            INTERNAL TASK FLAGS
**
** These bits are set in the task descriptor FLAGS field.
** Note that the bits from the task template ATTRIBUTES field are also copied
** to the task descriptor FLAGS field.
*/

/* This bit indicates that the task is running at a boosted priority level */
#define TASK_PRIORITY_BOOSTED             (0x8000)

/* This bit indicates that the task has kernel logging enabled */
#define TASK_LOGGING_ENABLED              (0x4000)

/* This bit indicates that the watchdog is active for this task */
#define TASK_WATCHDOG_STARTED             (0x2000)

/* This bit indicates that preemption has been disabled for this task */
#define TASK_PREEMPTION_DISABLED          (0x1000)

/* This bit indicates that the watchdog is currently running for this task */
#define TASK_WATCHDOG_RUNNING             (0x800)

/* This bit indicates that the task has a MMU context associated with it */
#define TASK_MMU_CONTEXT_EXISTS           (0x0400)

/*
** This bit indicates that the floating point registers have been saved
** on the stack of the blocked task
*/
#define TASK_FLOATING_POINT_CONTEXT_SAVED (0x0200)

/*
** This bit indicates that the DSP registers have been saved
** on the stack of the blocked task
*/
#define TASK_DSP_CONTEXT_SAVED            (0x0100)

/*
** This bit indicates that the task is waiting on a LWEvent and wants
** all bits to be set
*/
#define TASK_LWEVENT_ALL_BITS_WANTED      (0x0080)

/*
** This bit indicates that the PSP context switch/ISR code has saved the
** scratch registers on the task's stack
*/

#define PSP_SCRATCH_REGISTERS_SAVED       (0x040)

/* 
** This bit indicates that the tasks TD and stack were provided by the
** application
*/
#define TASK_STACK_PREALLOCATED           (0x020)

/*--------------------------------------------------------------------------*/
/*
**                             TASK ID MACROS
**
** These macros manipulate the internals of a TASKID
*/

/* Create a new task id from processor number and task number */
#define BUILD_TASKID(procnum, tasknum) (_task_id)\
   (((uint_32)(procnum) << 16) | ((uint_32)(tasknum) & 0xFFFF))

/* Obtain the processor number from the task ID */
#define PROC_NUMBER_FROM_TASKID(taskid) ((uint_16)((taskid) >> 16))

/* Obtain the task number from the task ID */
#define TASK_NUMBER_FROM_TASKID(taskid) ((uint_16)((taskid) & 0xFFFF))

/* Increment task number being careful not to wrap to 0 */
#if (PSP_MEMORY_ADDRESSING_CAPABILITY > 16)
#define INC_TASK_NUMBER(n)                      \
   n++;                                         \
   n &= 0xFFFF;                                 \
   if (n == 0) {                                \
      n = 1;                                    \
   }
#else
#define INC_TASK_NUMBER(n)                      \
   n++;                                         \
   if (n == 0) {                                \
      n = 1;                                    \
   }
#endif


/*--------------------------------------------------------------------------*/
/*
**                            MQX INTERNAL TASK FLAGS
**
** These bits are set in the kernel data FLAGS field.
*/


/*
** This flag indicates to _int_kernel_isr that the _int_exception_isr has
** been installed.  Thus _int_kernel_isr will save and restore all
** registers upon entry/exit.
*/
#define MQX_FLAGS_EXCEPTION_HANDLER_INSTALLED   (0x0001)

/*--------------------------------------------------------------------------*/
/*
**                            TIME CONSTANTS
*/
#define LEAP_YEAR                (1)
#define NON_LEAP_YEAR            (0)

/*
** Time normalization macro.
** Ensures that output milliseconds are in the range of 0-999,
** and that out_secs are adjusted if required
*/
#define MQX_TIME_NORMALIZE(in_secs, in_msecs, out_secs, out_msecs) \
   (out_secs)  = 0;                                                \
   (out_msecs) = (in_msecs);                                       \
   if ((out_msecs) >= MILLISECS_IN_SECOND) {                       \
      (out_secs)   = (out_msecs) / MILLISECS_IN_SECOND;            \
      (out_msecs) -= ((out_secs) * MILLISECS_IN_SECOND);           \
   } /* Endif */                                                   \
   (out_secs) += (in_secs)

/*
** This macro performs the same function as above but uses a single
** TIME_STRUCT
*/
#define MQX_NORMALIZE_TIME_STRUCT(t_ptr)                     \
   if ( ((TIME_STRUCT_PTR)(t_ptr))->MILLISECONDS >= 1000 ) { \
      ((TIME_STRUCT_PTR)(t_ptr))->SECONDS      +=            \
         ((TIME_STRUCT_PTR)(t_ptr))->MILLISECONDS / 1000;    \
      ((TIME_STRUCT_PTR)(t_ptr))->MILLISECONDS  =            \
         ((TIME_STRUCT_PTR)(t_ptr))->MILLISECONDS % 1000;    \
   }

/* This macro initializes a TICK structure to zero */
#if MQX_NUM_TICK_FIELDS == 1
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 2
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 3
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 4
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->TICKS[3] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 5
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->TICKS[3] = 0; \
   (tick_ptr)->TICKS[4] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 6
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->TICKS[3] = 0; \
   (tick_ptr)->TICKS[4] = 0; \
   (tick_ptr)->TICKS[5] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 7
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->TICKS[3] = 0; \
   (tick_ptr)->TICKS[4] = 0; \
   (tick_ptr)->TICKS[5] = 0; \
   (tick_ptr)->TICKS[6] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#elif MQX_NUM_TICK_FIELDS == 8
#define MQX_ZERO_TICK_STRUCT(tick_ptr) \
   (tick_ptr)->TICKS[0] = 0; \
   (tick_ptr)->TICKS[1] = 0; \
   (tick_ptr)->TICKS[2] = 0; \
   (tick_ptr)->TICKS[3] = 0; \
   (tick_ptr)->TICKS[4] = 0; \
   (tick_ptr)->TICKS[5] = 0; \
   (tick_ptr)->TICKS[6] = 0; \
   (tick_ptr)->TICKS[7] = 0; \
   (tick_ptr)->HW_TICKS = 0;
#else
#error missing option for zeroing mqx tick structure
#endif

#ifndef PSP_MILLISECONDS_TO_TICKS_QUICK
#define PSP_MILLISECONDS_TO_TICKS_QUICK PSP_MILLISECONDS_TO_TICKS
#endif

/*--------------------------------------------------------------------------*/
/*
**                      INLINE MACROS FOR SPEED
*/

/*
** This macro checks if the current task should invoke the scheduler
** to allow a higher priority task to run (it may just have been added).
*/
#define _CHECK_RUN_SCHEDULER()  _sched_check_scheduler_internal()

/*
** This macro simulates the _task_ready function call. It is in the form of a
** macro to optimize for speed.
** NOTE: it MUST BE USED DISABLED
*/
#if MQX_HAS_TIME_SLICE
#define ZERO_TICK_STRUCT_INTERNAL(x) MQX_ZERO_TICK_STRUCT(x)
#else
#define ZERO_TICK_STRUCT_INTERNAL(x)
#endif

#if MQX_USE_INLINE_MACROS
#define _TASK_READY(the_td,the_kernel_data)                \
 _KLOGE2(KLOG_task_ready, the_td);                         \
 if ( ((uchar _PTR_) (the_td)->MY_QUEUE) >                 \
 (uchar _PTR_) (the_kernel_data->CURRENT_READY_Q) ) {      \
    the_kernel_data->CURRENT_READY_Q = (the_td)->MY_QUEUE; \
 } /* Endif */                                             \
 (the_td)->STATE = READY;                                  \
 (the_td)->TD_PREV = ((the_td)->MY_QUEUE)->TAIL_READY_Q;   \
 (the_td)->TD_NEXT = ((the_td)->TD_PREV)->TD_NEXT;         \
 ((the_td)->TD_PREV)->TD_NEXT = (the_td);                  \
 ((the_td)->MY_QUEUE)->TAIL_READY_Q = (the_td);            \
 /*(the_td)->CURRENT_TIME_SLICE = _mqx_zero_tick_struct;*/ \
  ZERO_TICK_STRUCT_INTERNAL(&(the_td)->CURRENT_TIME_SLICE) \
 _KLOGX1(KLOG_task_ready);
#else
#define _TASK_READY(the_td,the_kernel_data) _task_ready_internal(the_td)
#endif

#define _INT_DISABLE_CODE()                             \
   if (kernel_data->ACTIVE_PTR->DISABLED_LEVEL == 0)  { \
      _PSP_SET_DISABLE_SR(kernel_data->DISABLE_SR);     \
   } /* Endif */                                        \
   ++kernel_data->ACTIVE_PTR->DISABLED_LEVEL;

#define _INT_ENABLE_CODE()                                                     \
   if (kernel_data->ACTIVE_PTR->DISABLED_LEVEL) {                              \
      if (--kernel_data->ACTIVE_PTR->DISABLED_LEVEL == 0) {                    \
         if (kernel_data->IN_ISR) {                                            \
            _PSP_SET_ENABLE_SR(kernel_data->INTERRUPT_CONTEXT_PTR->ENABLE_SR); \
         } else {                                                              \
            _PSP_SET_ENABLE_SR(kernel_data->ACTIVE_SR);                        \
         } /* Endif */                                                         \
      } /* Endif */                                                            \
   } /* Endif */

#if MQX_USE_INLINE_MACROS
#define _INT_DISABLE() _INT_DISABLE_CODE()
#define _INT_ENABLE()  _INT_ENABLE_CODE()
#else
#define _INT_DISABLE() _int_disable()
#define _INT_ENABLE()  _int_enable()
#endif

/* This macro dequeues a td from the timeout queue */
#define _TIME_DEQUEUE(td,kd)                             \
   if ( ((TD_STRUCT_PTR)td)->STATE & IS_ON_TIMEOUT_Q ) { \
      _QUEUE_REMOVE(&kd->TIMEOUT_QUEUE,td);              \
      ((TD_STRUCT_PTR)td)->STATE &= ~IS_ON_TIMEOUT_Q;    \
      ((TD_STRUCT_PTR)td)->STATE |= BLOCKED;             \
   } /* Endif */

/*
** Macro to help with de-queueing, it subtracts the offset of
** field f of type t from pointer p, so that p points to the
** start of the structure
*/
#define _BACKUP_POINTER(p,t,f) \
   p = (t _PTR_)((uchar_ptr)p - FIELD_OFFSET(t, f))

/*--------------------------------------------------------------------------*/
/*
**                      KERNEL TASKING CONSTANTS
*/

/* This bit signifies a special task template index. */
#define SYSTEM_TASK_FLAG       ((_mqx_uint)(0x1 << (MQX_INT_SIZE_IN_BITS - 1)))

/* The task descriptor for the system task */
#define SYSTEM_TD_PTR(kd)      ((TD_STRUCT_PTR)(&(kd)->SYSTEM_TD))

/* The task id of the system task */
#define SYSTEM_TASK_ID(kd)     (SYSTEM_TD_PTR(kd)->TASK_ID)

/*
** The following are task indexes for the system tasks: idle task
** and the ipc task.
*/
#define IPC_TASK               (SYSTEM_TASK_FLAG | 0x2)
#define IDLE_TASK              (SYSTEM_TASK_FLAG | 0x3)

/*--------------------------------------------------------------------------*/
/*
**                          IPC CONSTANTS
**
** Inter-Processor Communication requests are made by calling the IPC
** function. This function is installed into the kernel data 'IPC' field
** by the IPC initialization code.
** An IPC request is made by calling this function.
** The first parameter indicates which processor this request is for.
** The second parameter indicates which component is sending the request.
** The third parameter indicates which function of the component is requesting
** service.
** The fourth parameter is the number of further parameters for the function call.
** And following this parameter are the parameters required for the remote
** function call.
**
*/

/*
** The following definitions apply to the inter-processor communications.
** These constants are used as the third parameter of the IPC_FUNCTION
** (installed in the kernel data structure).  This parameter indicates what
** kernel operation is required on a remote processor.
*/
#define IPC_ACTIVATE                  (0x1000)
#define IPC_TASK_CREATE               (0x1001)
#define IPC_TASK_CREATE_WITH_TEMPLATE (0x1002)
#define IPC_TASK_DESTROY              (0x1003)
#define IPC_TASK_RESTART              (0x1004)
#define IPC_TASK_ABORT                (0x1005)

/*--------------------------------------------------------------------------*/
/*
**                    KERNEL COMPONENT CONSTANTS
*/

/*
** The MQX component indexes, used to index into the component
** arrays to access component specific data,
** task destruction handlers and IPC handlers
*/
#define KERNEL_NAME_MANAGEMENT        (0)
#define KERNEL_SEMAPHORES             (1)
#define KERNEL_EVENTS                 (2)
#define KERNEL_MUTEXES                (3)
#define KERNEL_TIMER                  (4)
#define KERNEL_LOG                    (5)
#define KERNEL_WATCHDOG               (6)
#define KERNEL_MESSAGES               (7)
#define KERNEL_PARTITIONS             (8)
#define KERNEL_IPC                    (9)
#define KERNEL_IPC_MSG_ROUTING        (10)
#define KERNEL_LWLOG                  (11)

/* The maximum number of components */
#define MAX_KERNEL_COMPONENTS         (16)


/*--------------------------------------------------------------------------*/
/*
**                       KERNEL SCHEDULER CONSTANTS
*/

/* Scheduling policy constants */
#define SCHED_MAX_POLICY  MQX_SCHED_RR

/*--------------------------------------------------------------------------*/
/*
**                      QUEUE MANIPULATION MACROS
*/

/*
** These macros assume a data structure that looks like
** a double linked list.
**     element.NEXT is first field and points to next item in q
**     element.PREV is second field and points to prev item in q
** The queue head actually looks like a queue element!, but is initialized
** with values such that a queue with 1 element on it can have that element
** removed, and a queue with 0 elements on it can have an element added to
** the queue without any special checking.
*/

/* Initialize a queue of doubly linked elements */
#if MQX_USE_INLINE_MACROS
#define _QUEUE_INIT(queue,max)                                      \
   ((QUEUE_STRUCT_PTR)(queue))->NEXT =                              \
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)&((QUEUE_STRUCT_PTR)(queue))->NEXT); \
   ((QUEUE_STRUCT_PTR)(queue))->PREV =                              \
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)&((QUEUE_STRUCT_PTR)(queue))->NEXT); \
   ((QUEUE_STRUCT_PTR)(queue))->SIZE = (uint_16)0;                  \
   ((QUEUE_STRUCT_PTR)(queue))->MAX  = (uint_16)max
#else
#define _QUEUE_INIT(queue, max) _queue_init((QUEUE_STRUCT_PTR)(queue), max)
#endif

/* Is the queue empty */
#define _QUEUE_IS_EMPTY(queue) ((QUEUE_STRUCT_PTR)(queue)->SIZE == 0)

/* How big is the queue */
#define _QUEUE_GET_SIZE(queue)  ((QUEUE_STRUCT_PTR)(queue))->SIZE

/* Is the queue full */
#define _QUEUE_IS_FULL(queue)  \
   (((QUEUE_STRUCT_PTR)(queue))->SIZE == (((QUEUE_STRUCT_PTR)(queue))->MAX))

/* links a queue element into a doubly linked list, after queue_member */
#define _QUEUE_LINK(queue_member,element)               \
{                                                       \
   QUEUE_ELEMENT_STRUCT_PTR nxt = (queue_member)->NEXT; \
      (element)->NEXT = nxt;                            \
      (queue_member)->NEXT = element;                   \
      (element)->PREV = queue_member;                   \
      nxt->PREV = element;                              \
}

/*
** Inserts a queue element, after queue_member
** NOTE: this does not _ENQUEUE at the end of the queue.
*/
#if MQX_USE_INLINE_MACROS
#define _QUEUE_INSERT(queue,queue_member,element)           \
      _QUEUE_LINK((QUEUE_ELEMENT_STRUCT_PTR)((pointer)(queue_member)), \
         (QUEUE_ELEMENT_STRUCT_PTR)((pointer)(element)));              \
      ++((QUEUE_STRUCT_PTR)(queue))->SIZE;
#else
#define _QUEUE_INSERT(queue,queue_member,element) \
   _queue_insert((QUEUE_STRUCT_PTR)(queue), \
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)(queue_member)), \
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)(element)))
#endif

/* Enqueue an element at the end of the queue */
#if MQX_USE_INLINE_MACROS
#define _QUEUE_ENQUEUE(queue,element) \
   _QUEUE_INSERT((queue),((QUEUE_STRUCT_PTR)((pointer)(queue)))->PREV, \
   (element))
#else
#define _QUEUE_ENQUEUE(queue,element) \
   _queue_enqueue((QUEUE_STRUCT_PTR)(queue), \
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)(element)))
#endif


/* Unlink a queue member from any doubly linked queue */
#define _QUEUE_UNLINK(queue_member) \
{ \
   QUEUE_ELEMENT_STRUCT_PTR prev_ptr = \
      ((QUEUE_ELEMENT_STRUCT_PTR)((pointer)(queue_member)))->PREV; \
   QUEUE_ELEMENT_STRUCT_PTR next_ptr = \
      ((QUEUE_ELEMENT_STRUCT_PTR)((pointer)(queue_member)))->NEXT; \
   prev_ptr->NEXT = next_ptr; \
   next_ptr->PREV = prev_ptr; \
}


/*
** Remove an element from the queue
** NOTE: this does not dequeue an element from the front of the list
** the element must be known
*/
#define _QUEUE_REMOVE(queue,element)        \
      _QUEUE_UNLINK(element);               \
      --((QUEUE_STRUCT_PTR)(queue))->SIZE;

/* Dequeue an element from the front of the queue, the element is not known */
#if MQX_USE_INLINE_MACROS
#define _QUEUE_DEQUEUE(queue,element)                    \
   element = (pointer)((QUEUE_STRUCT_PTR)(queue))->NEXT; \
   _QUEUE_REMOVE((queue),(element))
#else
#define _QUEUE_DEQUEUE(queue,element) element = (pointer)_queue_dequeue(queue)
#endif


/*--------------------------------------------------------------------------*/
/*                     TASK QUEUE CONSTANTS                                 */

/* The correct value for the task queue VALID field */
#define TASK_QUEUE_VALID           (_mqx_uint)(0x74736b71)  /* "tskq" */


/*--------------------------------------------------------------------------*/
/*                LIGHT WEIGHT SEMAPHORE CONSTANTS                          */

/* The correct value for the lwsem VALID field */
#define LWSEM_VALID                (_mqx_uint)(0x6C77736D)    /* "lwsm" */


/*--------------------------------------------------------------------------*/
/*                     MISC. CONSTANTS                                      */

/* A macros to convert a pre-processor constant to a string */

#define NUM_TO_STR(x) #x
#define REAL_NUM_TO_STR(x) NUM_TO_STR(x)

/*--------------------------------------------------------------------------*/
/*                SPECIAL MEMORY ALLOCATOR OPTION                           */

#if MQX_USE_LWMEM_ALLOCATOR
#ifndef __MEMORY_MANAGER_COMPILE__
#define _mem_get_next_block_internal _lwmem_get_next_block_internal
#define _mem_init_internal _lwmem_init_internal
#define _mem_transfer_td_internal _lwmem_transfer_td_internal
#define _mem_transfer_internal _lwmem_transfer_internal
#endif
#endif

/*==========================================================================*/
/*                        DATA STRUCTURES                                   */

/*--------------------------------------------------------------------------*/
/*
**                      INTERRUPT TABLE STRUCTURE
**
** An array of these context structures is created at initialization time.
** The array is bounded by the FIRST_USER_ISR_VECTOR and
** LAST_USER_ISR_VECTOR fields in the kernel data structure.
**
** When an interrupt occurs, the interrupt number is checked to be within
** these stated bounds.  If it is, the interrupt table structure is
** indexed with the interrupt number to obtain the 'C' function to call.
**
** The interrupt table also contains a parameter to pass to the users
** isr, and an exception handler for the users isr.
*/
typedef struct interrupt_table_struct
{

   /* The application ISR to call for this interrupt */
   void    (_CODE_PTR_ APP_ISR)(pointer);

   /*
   ** The exception handler for this ISR.  If the exception handling
   ** has been installed as the default ISR,
   ** then when an exception occurs during an ISR,
   ** the ISR (and the exception) are aborted, and this function is called.
   ** This function is passed the ISR vector number, the exception
   ** vector number and the parameter for the application ISR,
   ** and the an exception frame pointer
   */
   void   (_CODE_PTR_  APP_ISR_EXCEPTION_HANDLER)(_mqx_uint, _mqx_uint, pointer,
      pointer);

   /* The parameter to pass to this ISR */
   pointer             APP_ISR_DATA;

} INTERRUPT_TABLE_STRUCT, _PTR_ INTERRUPT_TABLE_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                         TASK QUEUE STRUCTURE
**
** This structure defines a task queue.
** These task queues are elements on the KERNEL_TASK_QUEUE queue, a field
** of the kernel data structure.
*/
typedef struct task_queue_struct
{
   /* The next task queue */
   struct task_queue_struct _PTR_  NEXT;

   /* The previous task queue */
   struct task_queue_struct _PTR_  PREV;

   /* A validation stamp */
   _mqx_uint                       VALID;

   /* The policy of the queue (fifo or priority) */
   _mqx_uint                       POLICY;

   /* The queue of task descriptors */
   QUEUE_STRUCT                    TD_QUEUE;

} TASK_QUEUE_STRUCT, _PTR_ TASK_QUEUE_STRUCT_PTR;


/* Start CR 2396 */
#if MQX_USE_PMU
/* PMU structure */
typedef volatile struct pmu_struct
{
   /* 
   ** Number of execution time for idle task which after
   ** we enter power down mode 
   */
   uint_32   SLEEP_THRSHLD;
   
   /* Maximum slow down number of time clock interval */
   uint_32   MAX_SLOW_DOWN_NUM;

   /* Current slow down number of time clock interval */
   uint_32   CURRENT_NUM_SLOW_DOWN;

   /* Power down mode (0 | 1 | 2) */
   uint_8    PMU_MODE;

   /* Flag to slow down time clock interval */
   uint_8    SLOW_CLOCK_INTERVAL_FLAG;

   /* DVFS mode : 0 -> 3 */
   uint_8    GLOBAL_DVFS_MODE;

   /* DVFS mode when auto DVFS is enabled */
   uint_8    SAVED_GLOBAL_DVFS_MODE;
   
   /* Flag to enable automatic DVFS */
   uint_8    AUTO_DVFS_ENABLE; 

   /* Automatic DVFS thresholds */
   uint_8    AUTO_THRESHOLD_0; 
   uint_8    AUTO_THRESHOLD_1; 
   uint_8    AUTO_THRESHOLD_2; 

   /* DVFS period */
   uint_32   AUTO_DVFS_PERIOD;
 
   /* Tick count for auto DVFS mode */
   uint_32   AUTO_DVFS_TICKS;

   /* Idle count for auto DVFS mode */
   uint_32   AUTO_DVFS_IDLE_CNT;

   /* Timer frequency for different DVFS mode */
   uint_32   TIMER_FREQUNCY[DVFS_MAX_TIMER_FREQUENCY];

   /* HW ticks representative of timer frequency for different DVFS mode */
   uint_32   HW_TICKS_PER_INTERRUPT[DVFS_MAX_TIMER_FREQUENCY];

   /* Power consumption for different DVFS mode */
   uint_32   POWER_CONSUMPTION[DVFS_MAX_TIMER_FREQUENCY];

   /* Power consumption for power down */
   uint_32   PMU_POWER_CONSUMPTION;

   /* Idle loop count */
   uint_32   IDLE_LOOP_COUNT;

   /* Ticks count */
   uint_32   TICKS_COUNT;

} PMU_STRUCT, _PTR_ PMU_STRUCT_PTR;
#endif
/* End CR 2396 */


/*--------------------------------------------------------------------------*/
/*
**                      TASK DESCRIPTOR STRUCTURE
**
** This structure defines the Task Descriptor (TD). There is one
** Task Descriptor for each task in the system, and the TD contains
** various information about the state of the task.
*/

typedef struct   td_struct
{
   /*
   ** A pointer to the next TD in the queue (for whatever queue this
   ** task is currently in). This field MUST be the first field in the
   ** TD.
   */
   struct td_struct            _PTR_ TD_NEXT;

   /*
   ** A pointer to the previous TD in the queue (for whatever queue this
   ** task is currently in).
   */
   struct td_struct            _PTR_ TD_PREV;

   /* The current state that this task is in. */
   _mqx_uint                         STATE;

   /* The Task id of the task that this task descriptor represents. */
   _task_id                          TASK_ID;

   /* The Start of the Stack */
   pointer                           STACK_BASE;

   /*
   ** If the task is blocked, then this is a pointer to the task's current
   ** stack value, otherwise it is a pointer to the task's stack at the
   ** time of the last block.
   */
   pointer                           STACK_PTR;

   /* The other end of the Stack  */
   void                        _PTR_ STACK_LIMIT;

   /* The Ready Queue upon which to place the task, when it is ready to run */
   struct ready_q_struct    _PTR_    MY_QUEUE;

   /*
   ** pointer to the first entry in the linked list of message queues owned
   ** by this task.
   */
   pointer                           MSG_QUEUE_HEAD;

   /*
   ** pointer to the last entry in the linked list of message queues owned
   ** by this task.
   */
   pointer                           MSG_QUEUE_TAIL;

   /* Number of messages currently available on all queues. */
   _mqx_uint                         MESSAGES_AVAILABLE;

   /* The address of the message just received */
   pointer                           MESSAGE;

   /*
   ** A field used by components to store information.
   ** Also, when a task is put onto a queue,
   ** (The state of the task has the TD_IS_ON_QUEUE set)
   ** then this field contains the address of the queue it is on.
   */
   _mqx_uint                         INFO;

   /*
   ** This field points to a linked list of memory pool blocks obtained
   ** via calls to _mem_alloc()
   */
   pointer                           MEMORY_RESOURCE_LIST;

   /* The errno for this task */
   _mqx_uint                         TASK_ERROR_CODE;

   /* The Task ID of the task that created this task */
   _task_id                          PARENT;

   /* The task template index from which this task was created. */
   _mqx_uint                         TEMPLATE_INDEX;

   /* The default console input state information */
   pointer                           STDIN_STREAM;

   /* The default console output state information */
   pointer                           STDOUT_STREAM;

   /* The default console error output state information */
   pointer                           STDERR_STREAM;

   /*
   ** The Hardware Flags of this task.  This is set equivalent to the ENABLE_SR
   ** field of the current ready queue that this task resides on.
   ** This field is copied to the kernel data ACTIVE_SR field when this task
   ** becomes the ACTIVE task.
   ** The ACTIVE_SR field in the kernel data is used by _int_enable,
   ** to set the correct hardware interrupt level for this task.
   */
   uint_16                           TASK_SR;

   uint_16                           RESERVED1;

   /* A pointer to the task template used to create this task. */
   TASK_TEMPLATE_STRUCT_PTR          TASK_TEMPLATE_PTR;

   pointer                           TAD_RESERVED;

   /* Timeout information used when putting task on a timeout queue. */
   MQX_TICK_STRUCT                   TIMEOUT;

   /* The number of times _int_disable has been called */
   _mqx_uint                         DISABLED_LEVEL;

   /*
   ** Bit flags for controlling special features
   ** See INTERNAL TASK FLAGS, and TASK TEMPLATE ATTRIBUTES FLAGS
   */
   _mqx_uint                         FLAGS;

   /*
   ** The amount of time (ticks) the task should be allowed to run before
   ** being put at the end of it's ready queue.
   ** This is only used when time slicing has been enabled
   */
   MQX_TICK_STRUCT                   TIME_SLICE;
   MQX_TICK_STRUCT                   CURRENT_TIME_SLICE;

   /* The number of times that this task has had it priority level boosted */
   _mqx_uint                         BOOSTED;

   /*
   ** The home priority Queue of the task where the task is returned to
   ** when it's boosted period has expired.
   */
   struct ready_q_struct     _PTR_   HOME_QUEUE;

   /* The time left before the watchdog expires */
   MQX_TICK_STRUCT                   WATCHDOG_TIME;

   /*
   ** These fields are use for external profiling and runtime error checking
   ** components
   */
   pointer                           PROFILER_CONTEXT_PTR;
   pointer                           RUNTIME_ERROR_CHECK_PTR;

   /* The address of the virtual context for this task (if it exists) */
   pointer                           MMU_VIRTUAL_CONTEXT_PTR;

   /* The address of the environment for the task */
   pointer                           ENVIRONMENT_PTR;

   /* The exit handler for the task */
   void                  (_CODE_PTR_ EXIT_HANDLER_PTR)(void);

   /* The exception handler for the task */
   void                  (_CODE_PTR_ EXCEPTION_HANDLER_PTR)(_mqx_uint, pointer);

   /* used to link this td with all the other tds in the system */
   QUEUE_ELEMENT_STRUCT              TD_LIST_INFO;

   /* Extra link list pointer for future development */
   QUEUE_ELEMENT_STRUCT              AUX_QUEUE;

   /* The light weight event bits this task is waiting on */
   _mqx_uint                         LWEVENT_BITS;

   /* Pointer to where the task's DSP registers will be saved */
   pointer                           DSP_CONTEXT_PTR;
   /* Pointer to where the task's floating point registers will be saved */
   pointer                           FLOAT_CONTEXT_PTR;

   /* C runtime thread local storage */
   pointer                           CRT_TLS;
   pointer                           TOS_RESERVED;

/* Start CR 2396 */
#if MQX_USE_PMU
   /* DVFS mode for the task */
   _mqx_uint                         TASK_DVFS_MODE;
#endif
/* End CR 2396 */

} TD_STRUCT, _PTR_ TD_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** READY QUEUE STRUCTURE
**
** This structure defines the structure of an ready queue element.
**
** The list is allocated as one large array of elements.
** ALSO the list is ordered so that higher priority ready
** queue elements are located at higher addresses in memory.
** The CURRENT_READY_Q field of the kernel data structure points to the
** highest priority ready queue that contains a task descriptor.
**
** These elements are linked together via the NEXT_Q field.
** This linked list is pointed to by the READY_Q_LIST field of the kernel
** data structure.  The list is terminated by a NULL field.
** The list is ordered, with the highest priority ready queue element
** first.
**
*/

typedef struct ready_q_struct
{

   /*
   ** The head of the ready queue for this priority level, pointing
   ** to itself if empty.  Otherwise points to first task descriptor
   ** ready to run.  The queue is circular, so the first task descriptor
   ** points to the next task descriptor ready to run.
   */
   TD_STRUCT_PTR                HEAD_READY_Q;

   /* The tail of the ready queue, pointing to itself if empty.
   ** Otherwise, it points to the last task descriptor on the queue,
   ** to which new tasks are added as they become ready.
   */
   TD_STRUCT_PTR                TAIL_READY_Q;

   /* The address of the next priority queue, of lower priority */
   struct ready_q_struct  _PTR_ NEXT_Q;

   /*
   ** The hardware priority of this priority queue
   ** This field is copied to the tasks TASK_SR field when this task
   ** is assigned to the ready queue. (ie when the task is created,
   ** or when the task changes priority levels)
   ** The task TASK_SR field is assigned to the ACTIVE_SR field in
   ** the kernel data structure when the task becomes the ACTIVE(running) task.
   ** The ACTIVE_SR field in the kernel data is used by _int_enable,
   ** to set the correct hardware interrupt level for this task.
   */
   uint_16                      ENABLE_SR;

   /* The software priority of this queue, 0 being the highest priority */
   uint_16                      PRIORITY;

} READY_Q_STRUCT, _PTR_ READY_Q_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** THE KERNEL DATA STRUCTURE
**
** This structure defines the kernel data structure used by MQX
** for all of it's state and dynamic variables.
**
** This structure is created starting at the address provided by the
** START_OF_KERNEL_MEMORY field of the MQX_INITIALIZATION_STRUCT
** provided by the user as a parameter to _mqx()
**
** The address of this structure is kept in the global variable _mqx_kernel_data
*/

typedef struct  kernel_data_struct
{
   /*
   ** ----------------------------------------------------------
   ** Configuration information. Used by MQX tools and debuggers
   ** ----------------------------------------------------------
   */

   /* The addressing capability of the processor */
   uint_32                         ADDRESSING_CAPABILITY;

   /* The endianess of the processor */
   uint_32                         ENDIANESS;

   /* The type of CPU that this kernel is running upon */
   uint_16                         CPU_TYPE;

   /* PSP Configuration data */
   /* How a PSP aligns a memory block */
   uint_16                         PSP_CFG_MEMORY_ALIGNMENT;

   /* How a PSP aligns a task's stack */
   uint_16                         PSP_CFG_STACK_ALIGNMENT;

   /* How a PSP pads the STOREBLOCK_STRUCT. Number of reserved fields */
   uint_16                         PSP_CFG_MEM_STOREBLOCK_ALIGNMENT;

   /*
   ** The configuration used to compile this kernel
   ** Written to at mqx initialization time
   */
   uint_16                         CONFIG1;
   uint_16                         CONFIG2;

   /*
   ** ----------------------------------------------------------
   */

   /* This field is used to store bit flags for use by MQX primitives. */
   uint_16                         FLAGS;

   /*
   ** This field is used to by _int_disable to program the hardware correctly
   ** when interrupts are to be disabled.
   ** The value of this field is set by the psp upon system initialization.
   ** This field is equivalent to the ENABLE_SR field for the highest
   ** priority MQX task (ie tasks at priority 0) which run with interrupts
   ** disabled.
   */
   uint_16                         DISABLE_SR;

   /*
   ** A count incremented when an interrupt service routine is entered.
   ** It thus indicates the interrupt nesting level for the current
   ** interrupt being serviced.
   */
   uint_16                         IN_ISR;

   /*
   ** This field is used by _int_enable to program the hardware correctly
   ** when interrupts are to be re-enabled.
   ** When a READY task becomes the ACTIVE task the TASK_SR field from the
   ** task descriptor is copied to this field.
   */
   uint_16                         ACTIVE_SR;

   /*
   ** The address of the task descriptor of the currently running task.
   ** Note that the currently running task (the ACTIVE task) is the
   ** first task on the highest priority ready queue and will be in the
   ** READY state.
   */
   TD_STRUCT_PTR                   ACTIVE_PTR;

   /*
   ** The address of the task descriptor of the currently running task
   ** that uses the floating point co-processor.
   ** Note this may be different from the ACTIVE_PTR, as the floating
   ** point register context switches are only performed between
   ** FLOATING_POINT_TASKS (where applicable)
   */
   TD_STRUCT_PTR                   FP_ACTIVE_PTR;

   /*
   ** The address of the task descriptor of the currently running task
   ** that uses the DSP co-processor.
   ** Note this may be different from the ACTIVE_PTR, as the DSP
   ** register context switches are only performed between
   ** DSP_TASKS (where applicable).
   */
   TD_STRUCT_PTR                   DSP_ACTIVE_PTR;

   /* The address of the highest priority ready q */
   READY_Q_STRUCT_PTR              READY_Q_LIST;

   /* The address of the highest priority occupied ready queue */
   READY_Q_STRUCT_PTR              CURRENT_READY_Q;

   /* The processor number of this processor */
   uint_32                         PROCESSOR_NUMBER;

   /* The current internal time, from start of processor */
   MQX_TICK_STRUCT                 TIME;

   /* The current time offset, generated by _time_set */
   MQX_TICK_STRUCT                 TIME_OFFSET;

   /*
   ** The MQX timeout queue. Tasks waiting for a timeout are placed onto this
   ** queue
   */
   QUEUE_STRUCT                    TIMEOUT_QUEUE;

   /* The IPC task ID */
   _task_id                        MY_IPC_ID;

   /* The IPC task descriptor */
   TD_STRUCT_PTR                   MY_IPC_TD_PTR;

   /* The address of the base of the interrupt stack */
   pointer                         INTERRUPT_STACK_PTR;

   /* Context information for kernel memory */
   MEMPOOL_STRUCT                  KD_POOL;

   /*
   ** The default I/O FILE pointers constructed at initialization time
   ** from the IO_FUNCTION fields of the MQX_INITIALIZATION_STRUCT
   */
   pointer                         PROCESSOR_STDIN;
   pointer                         PROCESSOR_STDOUT;
   pointer                         PROCESSOR_STDERR;

   /* The number of task templates on this processor */
   _mqx_uint                       NUM_TASK_TEMPLATES;

   /* The address of the local task template list */
   TASK_TEMPLATE_STRUCT_PTR        TASK_TEMPLATE_LIST_PTR;

   /* The kernel counter (used to provide unique numbers) */
   _mqx_uint                       COUNTER;

   /*
   ** Idle loop counters, incremented by the idle task as it executes.
   ** IDLE_LOOP1 in incremented until it reaches 0.
   ** When IDLE_LOOP1 wraps to 0, IDLE_LOOP2 is incremented etc.
   */
   _mqx_uint                       IDLE_LOOP1;
   _mqx_uint                       IDLE_LOOP2;
   _mqx_uint                       IDLE_LOOP3;
   _mqx_uint                       IDLE_LOOP4;

   /* System Task Templates */
   TASK_TEMPLATE_STRUCT            IDLE_TASK_TEMPLATE;

   /* A copy of the MQX initialization structure */
   MQX_INITIALIZATION_STRUCT       INIT;

   /* An address used by _mqx_exit */
   void                (_CODE_PTR_ EXIT_HANDLER)(void);

   /* A queue of all allocated TDs */
   QUEUE_STRUCT                    TD_LIST;

   /* A counter used to build a unique task id */
   /* _mqx_uint                       TD_COUNTER; */
   _mqx_uint                       TASK_NUMBER;

   /* Pointer in TD list where to insert the next TD after */
   TD_STRUCT_PTR                   INSERT_TD_PTR;

   /* Used to keep track of all memory pools */
   MEMORY_COMPONENT_STRUCT         MEM_COMP;

   /* The following is used by MQX exit */
   pointer                         USERS_STACK;
   _mqx_max_type                   USERS_VBR;
   _mqx_uint                       USERS_ERROR;

   /* message pool_id of the named pool, for IPC requests*/
   pointer                         IPC_NAMED_POOL;

   /* Kernel Scheduler constants */
   _mqx_uint                       LOWEST_TASK_PRIORITY;

   /* Kernel Scheduler default task creation attributes */
   _mqx_uint                       SCHED_POLICY;
   MQX_TICK_STRUCT                 SCHED_TIME_SLICE;

   /*
   ** Kernel component data pointers.  When a kernel component is
   ** installed, it allocates memory and stores a pointer to this memory
   ** in this array.
   */
   pointer                         KERNEL_COMPONENTS[MAX_KERNEL_COMPONENTS];

   /* Light wait semaphore to protect component creation */
   LWSEM_STRUCT                    COMPONENT_CREATE_LWSEM;

   /* This is the period in milliseconds of the clock interrupt */
   _mqx_uint                       KERNEL_ALARM_RESOLUTION;

   /* The system clock interrupt vector number */
   _mqx_uint                       SYSTEM_CLOCK_INT_NUMBER;

   /* The system clock hardware register reference value */
   _mqx_uint                       TIMER_HW_REFERENCE;

   /* The number of times the timer interrupts or ticks in a second */
   _mqx_uint                       TICKS_PER_SECOND;

   /* The number of hardware ticks in a ticks */
   uint_32                         HW_TICKS_PER_TICK;

   /*
   ** This is the function that is called when the number of hw ticks
   ** is needed
   */
   uint_32             (_CODE_PTR_ GET_HWTICKS)(pointer);

   /* This is the parameter that is passed to the get hw ticks function */
   pointer                         GET_HWTICKS_PARAM;

   /* The list of task queue structures for explicit scheduling */
   QUEUE_STRUCT                    KERNEL_TASK_QUEUES;

   /* The kernel log control variable for KLOG entry control */
   uint_32                         LOG_CONTROL;

   /* The address of the last task descriptor being logged */
   pointer                         LOG_OLD_TD;

   /* This field contains the address of _int_kernel_isr */
   void                (_CODE_PTR_ INT_KERNEL_ISR_ADDR)(void);

   /*
   ** This is the default ISR that is called whenever a users isr is
   ** not available
   */
   void                (_CODE_PTR_ DEFAULT_ISR)(pointer);

   /*
   ** The interrupt vector for the first interrupt that the
   ** application wishes to have a 'C' isr for
   */
   _mqx_uint                       FIRST_USER_ISR_VECTOR;

   /* The last interrupt vector that the application wishes to handle */
   _mqx_uint                       LAST_USER_ISR_VECTOR;

   /*
   ** A pointer to the CURRENT interrupt handler context this is a link list
   ** of context information kept on the interrupt stack which keeps an
   ** error code and a status register for each nested interrupt.
   */
   PSP_INT_CONTEXT_STRUCT_PTR      INTERRUPT_CONTEXT_PTR;

   /* A pointer to a table of 'C' handlers for interrupts */
   INTERRUPT_TABLE_STRUCT_PTR      INTERRUPT_TABLE_PTR;

   /* A queue of light weight semaphores that have been created */
   QUEUE_STRUCT                    LWSEM;

   /* sem for serializing create/destroy */
   LWSEM_STRUCT                    TASK_CREATE_LWSEM;

   /*
   ** Kernel component task destruction cleanup functions.
   ** When a task is destroyed, each component's cleanup function
   ** is called to free any resources that the task may have
   ** acquired
   */
   void                (_CODE_PTR_ COMPONENT_CLEANUP[MAX_KERNEL_COMPONENTS])
                                    (TD_STRUCT_PTR);

   /*
   ** IO component data pointers.  When an IO component is
   ** installed, it allocates memory and stores a pointer to this memory
   ** in this array.
   */
   pointer                         IO_COMPONENTS[MAX_IO_COMPONENTS];

   /*
   ** IO component task destruction cleanup functions
   ** When a task is destroyed, each component's cleanup function
   ** is called to free any resources that the task may have
   ** acquired
   */
   void                (_CODE_PTR_ IO_COMPONENT_CLEANUP[MAX_IO_COMPONENTS])
                                    (TD_STRUCT_PTR);

   /*
   ** When this field is not NULL, the kernel timer ISR will call it
   ** at each timer interrupt.  It is set by the timer component.
   */
   void                (_CODE_PTR_ TIMER_COMPONENT_ISR)(void);

   /*
   ** When I/O device drivers are added, their initialization
   ** tables are linked onto this queue by the I/O driver installation
   ** function
   */
   QUEUE_STRUCT                    IO_DEVICES;

   /* sem for serializing open/close */
   LWSEM_STRUCT                    IO_LWSEM;

   /*
   ** This function is called to handle IPC functionality
   ** When a component determines that a request is for a different processor,
   ** it calls this function.
   ** The first parameter indicates which processor number to send
   **    the message to,
   ** the second parameter indicates which component is to handle the
   **    IPC message on the remote CPU,
   ** The third parameter indicates which function in the component to perform,
   ** The fourth parameter indicates the number of additional parameters
   **    (All _mqx_uint)
   ** Following are the additional parameters
   */
   _mqx_uint            (_CODE_PTR_ IPC)(boolean, _processor_number,
      _mqx_uint, _mqx_uint, _mqx_uint, ...);

   /* A pointer to the context information required by the IPC */
   pointer                         IPC_COMPONENT_PTR;

   /* A pointer for use by any PSP Support functions */
   pointer                         PSP_SUPPORT_PTR;

   /*
   ** This field holds a function pointer for a secondary timer,
   ** if installed.
   */
   void                (_CODE_PTR_ TIMER2)(pointer);

   /* TD for the system task */
   TD_STRUCT                       SYSTEM_TD;

   /* Queue for storing PCB pools */
   QUEUE_STRUCT                    IO_PCB_POOLS;

   /* Queue for storing Light weight memory pools */
   QUEUE_STRUCT                    LWMEM_POOLS;

   /* The light weight memory pool for default memory allocation */
   pointer                         KERNEL_LWMEM_POOL;

   /* Function to call to reclaim light weight memory */
   void               (_CODE_PTR_  LWMEM_CLEANUP)(TD_STRUCT_PTR);

   /* Queue for storing Light weight events */
   QUEUE_STRUCT                    LWEVENTS;

   /* Queue for storing Light weight timers */
   QUEUE_STRUCT                    LWTIMERS;

   /* The lwtimer ISR called from the kernel timer isr */
   void               (_CODE_PTR_ LWTIMER_ISR)(void);

   /* User wants to reserve extra memory at the top of stack in every task */
   _mqx_uint                      TOS_RESERVED_SIZE;
   uint_8                         TOS_RESERVED_ALIGN_MASK;

   /* Queue for storing Light weight message queues */
   QUEUE_STRUCT                    LWMSGQS;

/* Start CR 2396 */
#if MQX_USE_PMU
   /* pointer to PMU structure */
   PMU_STRUCT_PTR                  PMU_STRUCT_PTR;

   /* Current DVFS performance level */
   uint_32                         CURRENT_KERNEL_DVFS_MODE;
#endif
/* End CR 2396 */

   /* Spare fields */
   uint_8                          RESERVED_FOR_ALIGNMENT_FILL[3];
   _mqx_uint                       END_OF_KERNEL_DATA_STRUCT[2];

} KERNEL_DATA_STRUCT, _PTR_ KERNEL_DATA_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** KERNEL INTERNAL FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern struct kernel_data_struct _PTR_ _mqx_kernel_data;

extern void          _int_kernel_isr_return_internal(void);

extern void          _klog_block_internal(void);
extern void          _klog_context_switch_internal(void);
extern void          _klog_execute_scheduler_internal(void);
extern char _PTR_    _klog_get_function_name_internal(uint_32);
extern _mqx_uint     _klog_get_task_stack_usage_internal(TD_STRUCT_PTR,
   _mem_size_ptr, _mem_size_ptr);
extern void          _klog_isr_end_internal(_mqx_uint);
extern void          _klog_isr_start_internal(_mqx_uint);
extern void          _klog_log(_mqx_uint, _mqx_max_type, _mqx_max_type, _mqx_max_type,
   _mqx_max_type, _mqx_max_type);
extern void          _klog_write_log_internal(_mqx_max_type, _mqx_max_type, _mqx_max_type,
  _mqx_max_type, _mqx_max_type, _mqx_max_type);
extern void          _klog_yield_internal(void);

extern _mqx_uint     _lwsem_wait_timed_internal(LWSEM_STRUCT_PTR, TD_STRUCT_PTR);
extern void          _mqx_init_kernel_data_internal(void);


extern TD_STRUCT_PTR _task_alloc_td_internal(_mem_size, _mem_size_ptr, pointer);
extern TD_STRUCT_PTR _task_build_internal(_mqx_uint, uint_32, pointer, _mem_size);
extern void          _task_fill_stack_internal(_mqx_uint_ptr, _mqx_uint);
extern TD_STRUCT_PTR _task_init_internal(TASK_TEMPLATE_STRUCT_PTR,
   _task_id, uint_32, boolean, pointer, _mem_size);
extern void          _task_ready_internal(TD_STRUCT_PTR);
extern _mqx_uint     _task_set_error_td_internal(TD_STRUCT_PTR, _mqx_uint);

extern void          _sched_boost_priority_internal(TD_STRUCT_PTR, _mqx_uint);
extern _mqx_uint     _sched_get_max_priority_on_q_internal(QUEUE_STRUCT_PTR);
extern void          _sched_insert_priorityq_internal(QUEUE_STRUCT_PTR,
   TD_STRUCT_PTR);
extern void          _sched_set_priority_internal(TD_STRUCT_PTR,
   _mqx_uint);
extern void          _sched_unboost_priority_internal(TD_STRUCT_PTR, _mqx_uint);

/*
** Prototypes from psp specific directories
*/

extern pointer       _mem_alloc_internal(_mem_size, TD_STRUCT_PTR,
   MEMPOOL_STRUCT_PTR, _mqx_uint_ptr);
extern _mqx_uint     _mem_coalesce_internal(STOREBLOCK_STRUCT_PTR);
extern pointer       _mem_get_next_block_internal(TD_STRUCT_PTR, pointer);
extern _mqx_uint     _mem_init_internal(void);
extern _mqx_uint     _mem_transfer_td_internal(pointer, TD_STRUCT_PTR,
   TD_STRUCT_PTR);
extern void          _mem_transfer_internal(pointer, TD_STRUCT_PTR);

extern void          _mqx_init_exit_internal(_mqx_uint);

extern void          _sched_check_scheduler_internal(void);
extern void          _sched_execute_scheduler_internal(void);
extern void          _sched_run_internal(void);
extern _mqx_uint     _sched_set_rr_interval_internal(_task_id,
   MQX_TICK_STRUCT_PTR, MQX_TICK_STRUCT_PTR);
extern void          _sched_start_internal(void);

extern void          _task_execute_exit_handler_internal(TD_STRUCT_PTR);
extern void          _task_exit_function_internal(void);
extern uint_32       _task_get_parameter_internal(TD_STRUCT_PTR);
extern uint_32       _task_set_parameter_internal(uint_32, TD_STRUCT_PTR);
extern void          _task_sync_priority_internal(TD_STRUCT_PTR);

extern       boolean _time_check_if_leap(uint_16);
extern const uchar   _time_days_in_month_internal[2][13];
extern const uint_32 _time_secs_before_month_internal[2][13];
extern const uint_32 _time_secs_before_year_internal[];

extern const uint_16 _time_days_before_month_internal[2][13];
extern const uint_32 _time_days_before_year_internal[];

extern void          _time_delay_internal(TD_STRUCT_PTR);

extern void          _psp_build_stack_frame(TD_STRUCT_PTR, pointer,
   _mem_size, TASK_TEMPLATE_STRUCT_PTR, _mqx_uint, uint_32);
extern void          _psp_destroy_stack_frame(TD_STRUCT_PTR);
extern PSP_STACK_START_STRUCT_PTR _psp_get_stack_start(TD_STRUCT_PTR);
extern _mqx_uint     _psp_init_readyqs(void);
extern void          _psp_set_kernel_disable_level(void);
extern void          _psp_save_fp_context_internal(void);

/*
** General prototypes from bsp specific directories, called by MQX
*/
extern _mqx_uint     _bsp_enable_card(void);

/*
** General prototypes from bsp specific directories, internal to BSP
*/
extern void          _bsp_exit_handler(void);
extern void          _bsp_timer_isr(pointer);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */
