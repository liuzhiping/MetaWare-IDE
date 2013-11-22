#ifndef __arc_cnfg_h__
#define __arc_cnfg_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International.
***
*** File: arc_cnfg.h
***
*** Comments:
***   This include file is used to provide information needed by
***   the ARC International MQX RTOS for ARC processor.
***
***
***************************************************************************
*END***********************************************************************/

/*
** The following defines usually come from MQX makefiles, but
** Metaware IDE uses projects instead.  Fastest will disable
** many features that slow the kernel down.  Small disables
** many features that will result in a smaller kernel size.
** You can pick your own combination of features to keep what
** you want and strip out what you don't.
*/

#define MQX_CRIPPLED_EVALUATION  0
#define MQX_VERIFICATION_TESTING 0

// Normal No Debug 
#if MQX_NO_DEBUG
# if !MQX_VERIFICATION_TESTING
#define MQX_CHECK_VALIDITY   0
/* Note: The MQX_KERNEL_LOGGING setting MUST match the corresponding setting in psp_cnfg.met */
#define MQX_KERNEL_LOGGING   0
#define MQX_MONITOR_STACK    0
# endif
#endif /* MQX_NO_DEBUG */

// Normal Debug 
#if MQX_DEBUG
/* No optimization - see build\config.mk */
#endif /* MQX_DEBUG */

// Floating point IO
#if MQX_FLOAT
#define MQX_INCLUDE_FLOATING_POINT_IO   1
#else
# ifdef MQX_ADD_FLOATING_POINT_IO
#define MQX_INCLUDE_FLOATING_POINT_IO   1
# endif	/* MQX_ADD_FLOATING_POINT_IO */
#endif /* MQX_FLOAT */

// Normal Ticks
#if MQX_TICKS
# if !MQX_VERIFICATION_TESTING
#define MQX_CHECK_VALIDITY              0
/* Note: The MQX_KERNEL_LOGGING setting MUST match the corresponding setting in psp_cnfg.met */
#define MQX_KERNEL_LOGGING              0
#define MQX_MONITOR_STACK               0
# endif /* MQX_VERIFICATION_TESTING */
// incremental changes
#define MQX_DEFAULT_TIME_SLICE_IN_TICKS 1
#define MQX_TIMER_USES_TICKS_ONLY       1
#endif /* MQX_TICKS */

// Fastest
#if MQX_FAST
#define MQX_CHECK_VALIDITY                 0
/* Note: The MQX_KERNEL_LOGGING setting MUST match the corresponding setting in psp_cnfg.met */
#define MQX_KERNEL_LOGGING                 0
#define MQX_MONITOR_STACK                  0
#define MQX_DEFAULT_TIME_SLICE_IN_TICKS    1
#define MQX_TIMER_USES_TICKS_ONLY          1
#define MQX_HAS_TIME_SLICE                 0
#define MQX_IS_MULTI_PROCESSOR             0
#define MQX_MUTEX_HAS_POLLING              0
# if !MQX_VERIFICATION_TESTING
#define MQX_CHECK_ERRORS                   0
#define MQX_TASK_CREATION_BLOCKS           0
#define MQX_CHECK_MEMORY_ALLOCATION_ERRORS 0
#define MQX_COMPONENT_DESTRUCTION          0
#define MQX_TASK_DESTRUCTION               0
# endif /* MQX_VERIFICATION_TESTING */
#endif /* MQX_FAST */

// Small size
#if MQX_SMALL
#define MQX_DEFAULT_TIME_SLICE_IN_TICKS    1
#define MQX_LWLOG_TIME_STAMP_IN_TICKS      1
#define MQX_TIMER_USES_TICKS_ONLY          1
#define MQX_HAS_TIME_SLICE                 0
#define MQX_IS_MULTI_PROCESSOR             0
#define MQX_MUTEX_HAS_POLLING              0
# if !MQX_VERIFICATION_TESTING
#define MQX_CHECK_VALIDITY                 0
/* Note: The MQX_KERNEL_LOGGING setting MUST match the corresponding setting in psp_cnfg.met */
#define MQX_KERNEL_LOGGING                 0
#define MQX_MONITOR_STACK                  0
#define MQX_CHECK_ERRORS                   0
#define MQX_TASK_CREATION_BLOCKS           0
#define MQX_CHECK_MEMORY_ALLOCATION_ERRORS 0
#define MQX_COMPONENT_DESTRUCTION          0
#define MQX_TASK_DESTRUCTION               0
# endif /* MQX_VERIFICATION_TESTING */
// incremental changes
#define MQX_EXIT_ENABLED                   0
#define MQX_USE_IDLE_TASK                  0
#define MQX_USE_LWMEM_ALLOCATOR            1
#define MQX_COMPILE_FOR_SMALL_SIZE         1
#endif /* MQX_SMALL */

// Tiny
#if MQX_TINY
/* Disable heavy weight components */
#define MQX_USE_EVENTS                     0
#define MQX_USE_IPC	                       0
#define MQX_USE_LOGS                       0
#define MQX_USE_MESSAGES                   0
#define MQX_USE_MUTEXES                    0
#define MQX_USE_NAME                       0
#define MQX_USE_PARTITIONS                 0
#define MQX_USE_SEMAPHORES                 0
#define MQX_USE_SW_WATCHDOGS               0
#define MQX_USE_TIMER                      0
/* Note: The MQX_KERNEL_LOGGING setting MUST match the corresponding setting in psp_cnfg.met */
#define MQX_KERNEL_LOGGING                 0
/* Other configuration */
#define MQX_DEFAULT_TIME_SLICE_IN_TICKS    1
#define MQX_LWLOG_TIME_STAMP_IN_TICKS      1
#define MQX_TIMER_USES_TICKS_ONLY          1
#define MQX_HAS_TIME_SLICE                 0
#define MQX_IS_MULTI_PROCESSOR             0
#define MQX_MUTEX_HAS_POLLING              0
#define MQX_EXIT_ENABLED                   0
#define MQX_USE_IDLE_TASK                  0
#define MQX_USE_LWMEM_ALLOCATOR            1
#define MQX_COMPILE_FOR_SMALL_SIZE         1
/* Start CR 2396 */
#define MQX_USE_PMU                        0
/* End CR 2396 */
# if !MQX_VERIFICATION_TESTING
#define MQX_CHECK_VALIDITY                 0
#define MQX_MONITOR_STACK                  0
#define MQX_CHECK_ERRORS                   0
#define MQX_TASK_CREATION_BLOCKS           0
#define MQX_CHECK_MEMORY_ALLOCATION_ERRORS 0
#define MQX_COMPONENT_DESTRUCTION          0
#define MQX_TASK_DESTRUCTION               0
#define MQX_VERIFY_KERNEL_DATA             0
# endif /* MQX_VERIFICATION_TESTING */
/* Check for dependencies */
#if MQX_USE_EVENTS || MQX_USE_SEMAPHORES
 #if !MQX_USE_NAME
  #error enable MQX_USE_NAME for Semaphores and Events
 #endif
#endif
#if MQX_USE_IPC
 #if !MQX_USE_PARTITIONS
  #error enable MQX_USE_PARTITIONS for IPC
 #endif
#endif
#endif /* MQX_TINY */

/*
** These are used internally by ARC to build a crippled
** kernel for evaluation purposes.
*/
/* Check for dependencies */
#if MQX_CRIPPLED_EVALUATION
 #if MQX_KERNEL_LOGGING && !MQX_EXIT_ENABLED
  #error enable MQX_EXIT_ENABLED for MQX crippled evaluation
 #elif !MQX_KERNEL_LOGGING && MQX_EXIT_ENABLED
  #error disable MQX_EXIT_ENABLED for MQX crippled evaluation without MQX Kernel logging
 #endif
 
#endif

#endif
/* EOF */
