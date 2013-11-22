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
*** File: tq_dest.c
***
*** Comments:      
***   This file contains the function for destroying a task queue.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _taskq_destroy
* Returned Value   : MQX_OK or a task error code
* Comments         :
*   This function destroys the task queue
*
*END*----------------------------------------------------------------------*/

_mqx_uint _taskq_destroy
   (
      /* [IN] the task queue handle */
      pointer users_task_queue_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register TD_STRUCT_PTR          td_ptr;
   register TASK_QUEUE_STRUCT_PTR  task_queue_ptr = 
      (TASK_QUEUE_STRUCT_PTR)users_task_queue_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_taskq_destroy, users_task_queue_ptr);

#if MQX_CHECK_ERRORS
   if (task_queue_ptr == NULL){
      _int_enable();
      _KLOGX2(KLOG_taskq_destroy, MQX_INVALID_PARAMETER);
      return(MQX_INVALID_PARAMETER);
   } /* Endif */
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_taskq_destroy, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   }/* Endif */
#endif

   _int_disable();
#if MQX_CHECK_VALIDITY
   if (task_queue_ptr->VALID != TASK_QUEUE_VALID)  {
      _int_enable();
      _KLOGX2(KLOG_taskq_destroy, MQX_INVALID_TASK_QUEUE);
      return(MQX_INVALID_TASK_QUEUE);
   } /* Endif */
#endif

   task_queue_ptr->VALID = 0;
   while (_QUEUE_GET_SIZE(&task_queue_ptr->TD_QUEUE)) {
      _QUEUE_DEQUEUE(&task_queue_ptr->TD_QUEUE, td_ptr);
      _TASK_READY(td_ptr, kernel_data);
      _int_enable();
      _int_disable();
   } /* Endwhile */

   _QUEUE_REMOVE(&kernel_data->KERNEL_TASK_QUEUES,task_queue_ptr);
   
   _int_enable();

   _CHECK_RUN_SCHEDULER(); /* Let higher priority task run */

   _mem_free(task_queue_ptr);

   _KLOGX2(KLOG_taskq_destroy, MQX_OK);
   return(MQX_OK);
      
} /* Endbody */

/* EOF */
