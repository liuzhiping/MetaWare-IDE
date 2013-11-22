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
*** File: tq_creat.c
***
*** Comments:      
***   This file contains the function for creating a task queue.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _taskq_create
* Returned Value   : a handle to be used when referencing the task
*   queue
* Comments         :
*   This function initializes a task queue for use.
*
*END*----------------------------------------------------------------------*/

pointer _taskq_create
   ( 
      /* [IN] the policy of this task queue (fifo or priority queueing) */
      _mqx_uint policy
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TASK_QUEUE_STRUCT_PTR  task_queue_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_taskq_create, policy);

#if MQX_CHECK_ERRORS
   if (! ((policy == MQX_TASK_QUEUE_FIFO) ||
          (policy == MQX_TASK_QUEUE_BY_PRIORITY)))
   {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_taskq_create, NULL);
      return (NULL);
   } /* Endif */
   if (kernel_data->IN_ISR) {
      _task_set_error(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      _KLOGX2(KLOG_taskq_create, NULL);
      return(NULL);
   }/* Endif */
#endif

   task_queue_ptr = (TASK_QUEUE_STRUCT_PTR)_mem_alloc_system((_mem_size)
      sizeof(TASK_QUEUE_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (task_queue_ptr == NULL) {
      _KLOGX2(KLOG_taskq_create, NULL);
      return(NULL);
   } /* Endif */
#endif

   task_queue_ptr->POLICY = policy;
   _QUEUE_INIT(&task_queue_ptr->TD_QUEUE, 0);
   task_queue_ptr->VALID  = TASK_QUEUE_VALID;
   _int_disable();
   if (kernel_data->KERNEL_TASK_QUEUES.NEXT == NULL) {
       /* Initialize the task queue */
      _QUEUE_INIT(&kernel_data->KERNEL_TASK_QUEUES,0);
   
   } /* Endif */
   _QUEUE_ENQUEUE(&kernel_data->KERNEL_TASK_QUEUES, task_queue_ptr);
   _int_enable();

   _KLOGX2(KLOG_taskq_create, task_queue_ptr);

   return(task_queue_ptr);
   
} /* Endbody */

/* EOF */
