/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ta_prem.c
***
*** Comments:      
***   This file contains the functions for stopping and re-starting
*** task pre-emption.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_stop_preemption
* Comments         :
*    This function disables preemption of the currently running task
*    unless it blocks, or calls _task_start_preemption.
*    Note that interrupts will still be handled
*
*END*----------------------------------------------------------------------*/

void _task_stop_preemption
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
            TD_STRUCT_PTR td_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE1(KLOG_task_stop_preemption);

   td_ptr      = kernel_data->ACTIVE_PTR;
   _int_disable();
   td_ptr->FLAGS |= TASK_PREEMPTION_DISABLED;
   _int_enable();
   _KLOGX1(KLOG_task_stop_preemption);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_start_preemption
* Comments         :
*    This function restores the ability of this task to be preempted.
*
*END*----------------------------------------------------------------------*/

void _task_start_preemption
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
            TD_STRUCT_PTR td_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE1(KLOG_task_start_preemption);

   td_ptr      = kernel_data->ACTIVE_PTR;
   _int_disable();
   td_ptr->FLAGS &= ~TASK_PREEMPTION_DISABLED;
   _CHECK_RUN_SCHEDULER(); /* Allow higher priority tasks to run */
   _int_enable();

   _KLOGX1(KLOG_task_start_preemption);

} /* Endbody */

/* EOF */
