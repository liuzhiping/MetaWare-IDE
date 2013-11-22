/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: tq_test.c
***
*** Comments:      
***   This file contains the function for testing all task queues
*** in the system.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _taskq_test
* Returned Value   : MQX_OK or an error code
* Comments         :
*   This function tests all task queues for correctness
*
*END*----------------------------------------------------------------------*/

_mqx_uint _taskq_test
   ( 
      /* [OUT] the task queue in error */
      pointer _PTR_ task_queue_error_ptr,

      /* [OUT] the td on a task queue in error */
      pointer _PTR_ td_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TASK_QUEUE_STRUCT_PTR  task_queue_ptr;
   _mqx_uint                queue_size;
   _mqx_uint                result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_taskq_test, task_queue_error_ptr, td_error_ptr);

   *td_error_ptr = NULL;
   *task_queue_error_ptr = NULL;

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_taskq_test, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   }/* Endif */
#endif

   task_queue_ptr = (TASK_QUEUE_STRUCT_PTR)
      ((pointer)kernel_data->KERNEL_TASK_QUEUES.NEXT);
   if (_QUEUE_GET_SIZE(&kernel_data->KERNEL_TASK_QUEUES) == 0) {
      _KLOGX2(KLOG_taskq_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   _int_disable();

   result = _queue_test((QUEUE_STRUCT_PTR)&kernel_data->KERNEL_TASK_QUEUES,
      task_queue_error_ptr);
   if (result != MQX_OK) {
	  /* START CR 2064 */
      _int_enable();
	  /* END CR 2064 */
      _KLOGX3(KLOG_taskq_test, result, *task_queue_error_ptr);
      return(result);
   } /* Endif */

   queue_size = _QUEUE_GET_SIZE(&kernel_data->KERNEL_TASK_QUEUES);
   while (queue_size--) {
      if (task_queue_ptr->VALID != TASK_QUEUE_VALID) {
         result = MQX_INVALID_TASK_QUEUE;
         break;
      } /* Endif */

      result = _queue_test(&task_queue_ptr->TD_QUEUE, td_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      
      task_queue_ptr = task_queue_ptr->NEXT;
   } /* Endwhile */

   _int_enable();

   if (result != MQX_OK) {
      *task_queue_error_ptr = (pointer)task_queue_ptr;
   } /* Endif */
   _KLOGX4(KLOG_taskq_test, result, *task_queue_error_ptr, *td_error_ptr);

   return(result);
   
} /* Endbody */

/* EOF */
