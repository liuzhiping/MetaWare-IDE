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
*** File: tq_tsusp.c
***
*** Comments:      
***   This file contains the functions for suspending a task on a task queue.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _taskq_suspend_task
* Returned Value   : MQX_OK or a task error code
* Comments         :
*   This function suspends a specified task onto the specified
* task queue.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _taskq_suspend_task
   (
      /* [IN] the task to suspend */
      _task_id task_id,
      
      /* [IN] the task queue handle */
      pointer  users_task_queue_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register TASK_QUEUE_STRUCT_PTR  task_queue_ptr = 
      (TASK_QUEUE_STRUCT_PTR)users_task_queue_ptr;
   register TD_STRUCT_PTR          td_ptr;
            boolean                me;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_taskq_suspend_task, task_id, users_task_queue_ptr);

   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   me = (td_ptr == kernel_data->ACTIVE_PTR);

#if MQX_CHECK_ERRORS
   if (td_ptr == NULL) {
      _KLOGX2(KLOG_taskq_suspend_task, MQX_INVALID_TASK_ID);
      return(MQX_INVALID_TASK_ID);
   } /* Endif */
   if (task_queue_ptr == NULL){
      _KLOGX2(KLOG_taskq_suspend_task, MQX_INVALID_PARAMETER);
      return(MQX_INVALID_PARAMETER);
   } /* Endif */
   if (me && kernel_data->IN_ISR) {
      _KLOGX2(KLOG_taskq_suspend_task, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   }/* Endif */
#endif

   _INT_DISABLE();

#if MQX_CHECK_VALIDITY
   if (task_queue_ptr->VALID != TASK_QUEUE_VALID) {
      _int_enable();
      _KLOGX2(KLOG_taskq_suspend_task, MQX_INVALID_TASK_QUEUE);
      return(MQX_INVALID_TASK_QUEUE);
   } /* Endif */
#endif

   if (td_ptr->STATE != READY) {
      _INT_ENABLE();
      _KLOGX2(KLOG_taskq_suspend_task, MQX_INVALID_TASK_STATE);
      return(MQX_INVALID_TASK_STATE);
   } /* Endif */

   td_ptr->STATE = TASK_QUEUE_BLOCKED;
   _QUEUE_UNLINK(td_ptr); /* Remove task from ready to run queue */
   td_ptr->INFO = (_mqx_uint)&task_queue_ptr->TD_QUEUE;
   if (task_queue_ptr->POLICY & MQX_TASK_QUEUE_BY_PRIORITY)  {
      _sched_insert_priorityq_internal(&task_queue_ptr->TD_QUEUE, td_ptr);
   } else {
      _QUEUE_ENQUEUE(&task_queue_ptr->TD_QUEUE, td_ptr);
   } /* Endif */

   if (me && (kernel_data->IN_ISR == 0)) {
      _sched_execute_scheduler_internal(); /* Let the other tasks run */
   } /* Endif */

   _INT_ENABLE();

   _KLOGX2(KLOG_taskq_suspend_task, MQX_OK);
   return( MQX_OK );
   
} /* Endbody */

/* EOF */
