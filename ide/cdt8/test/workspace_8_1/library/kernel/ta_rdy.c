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
*** File: ta_rdy.c
***
*** Comments:      
***   This file contains the function for making a task ready to run.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_ready
* Returned Value   : void
* Comments         :
*   Adds a task to the appropriate ready queue, preparing it to run again.
*
*END*----------------------------------------------------------------------*/

void _task_ready
   (
      /* [IN] the address of the task descriptor to add */
      pointer td
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   TD_STRUCT_PTR           td_ptr = (TD_STRUCT_PTR)td;

   _GET_KERNEL_DATA(kernel_data);

   /* Perform a validity check on the td */
#if MQX_CHECK_ERRORS
   if (PROC_NUMBER_FROM_TASKID(td_ptr->TASK_ID) !=
      kernel_data->PROCESSOR_NUMBER)
   {
      _KLOGE2(KLOG_task_ready, td_ptr);
      _task_set_error(MQX_INVALID_TASK_ID);
      _KLOGX1(KLOG_task_ready);
      return;
   } /* Endif */
#endif

   _INT_DISABLE();

   if (td_ptr->STATE == READY) {
      /* The task is already on the ready to run Queue! */
      _int_enable();
      _KLOGE2(KLOG_task_ready, td_ptr);
      _task_set_error(MQX_INVALID_TASK_STATE);
      _KLOGX1(KLOG_task_ready);
      return;
   }/* Endif */

   _task_ready_internal(td_ptr);

   _INT_ENABLE();

   _CHECK_RUN_SCHEDULER(); /* Let higher priority task run */

} /* Endbody */

/* EOF */
