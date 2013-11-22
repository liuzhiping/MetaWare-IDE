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
*** File: sc_yield.c
***
*** Comments:      
***   This file contains the function for yeilding the processor to another
*** task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_yield
* Returned Value   : void
* Comments         :
*   This function is called by a user task to yield the processor to another.
*   It puts the calling task at the end of it's ready to run queue.
*
*END*----------------------------------------------------------------------*/

void _sched_yield
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   TD_STRUCT_PTR           td_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOG(_klog_yield_internal();)

   td_ptr = kernel_data->ACTIVE_PTR;

   _INT_DISABLE();
   _QUEUE_UNLINK(td_ptr);
   _TASK_READY(td_ptr, kernel_data);
   _sched_execute_scheduler_internal();
   _INT_ENABLE();

} /* Endbody */

/* EOF */
