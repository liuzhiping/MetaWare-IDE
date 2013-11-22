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
*** File: sc_srrt.c
***
*** Comments:      
***   This file contains the functions for setting the 
***   scheduling rr_interval of a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_set_rr_interval_ticks
* Returned Value   : _mqx_uint - MQX_OK or error code
* Comments         :
*   This function sets various the scheduling rr_interval 
*   for a task or the system
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_set_rr_interval_ticks
   (
      /* 
      ** [IN] the task whose rr_interval is to change:
      ** NULL_TASK_ID => the current task
      ** DEFAULT_TASK_ID => the kernel defaults for task creation
      ** any other    => the specified task
      */
      _task_id             task_id,

      /* [IN] the new scheduling rr_interval */
      MQX_TICK_STRUCT_PTR  new_rr_interval_ptr,

      /* [OUT] the old scheduling rr_interval */
      MQX_TICK_STRUCT_PTR  old_rr_interval_ptr

   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   _mqx_uint              result;
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE2(KLOG_sched_set_rr_interval_ticks, task_id);

   result = _sched_set_rr_interval_internal(task_id, new_rr_interval_ptr, 
      old_rr_interval_ptr);
   
   if (result != MQX_OK) {
      _task_set_error(result);
   } /* Endif */

   _KLOGX2(KLOG_sched_set_rr_interval_ticks, result);

   return result;

} /* Endbody */

/* EOF */
