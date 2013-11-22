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
*** File: lws_wati.c
***
*** Comments:      
***   This file contains the functions for manipulating the 
***   light weight semaphores.  These semaphores have low memory
***   requirements, and no extra features.  Tasks are suspended
***   in fifo order while waiting for a post.  No limits on values
***   are imposed.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwsem_wait_ticks
* Returned Value   : an error code
*   queue
* Comments         :
*   This function obtains a semaphore from the lwsem.  If one is not
* available, the task waits (in a FIFO order) or until the timeout expires
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_wait_ticks
   (
      /* [IN] the semaphore address */
      LWSEM_STRUCT_PTR sem_ptr,

      /* [IN] the number of ticks to delay, if 0, delay forever */
      _mqx_uint time_in_ticks
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_lwsem_wait_ticks, sem_ptr, time_in_ticks);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_lwsem_wait_ticks, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != LWSEM_VALID) {
      _KLOGX2(KLOG_lwsem_wait_ticks, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   _INT_DISABLE();
   if (sem_ptr->VALUE <= 0) {
      td_ptr = kernel_data->ACTIVE_PTR;
      if (time_in_ticks == 0) {
         td_ptr->STATE = LWSEM_BLOCKED;
         td_ptr->INFO  = (_mqx_uint)&sem_ptr->TD_QUEUE;
         _QUEUE_UNLINK(td_ptr);
         _QUEUE_ENQUEUE(&sem_ptr->TD_QUEUE, &td_ptr->AUX_QUEUE);
         _sched_execute_scheduler_internal();  /* Let the other tasks run */
         /* Another task has posted a semaphore, and it has been tranfered to this 
         ** task.
         */
         result = MQX_OK;
      } else {
         PSP_ADD_TICKS_TO_TICK_STRUCT(&kernel_data->TIME, time_in_ticks, 
            &td_ptr->TIMEOUT);
         result = _lwsem_wait_timed_internal(sem_ptr, td_ptr);
      } /* Endif */
   } else {
      --sem_ptr->VALUE;
      /* Start CR 788 */
      result = MQX_OK;
      /* End  CR 788 */
   } /* Endif */

#if MQX_COMPONENT_DESTRUCTION
   /* We must check for component destruction */
   if (sem_ptr->VALID != LWSEM_VALID) {
      _int_enable();
      /* The semaphore has been deleted */
      _KLOGX2(KLOG_lwsem_wait_ticks, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   _INT_ENABLE();

   _KLOGX2(KLOG_lwsem_wait_ticks, result);

   return(result);
   
} /* Endbody */

/* EOF */
