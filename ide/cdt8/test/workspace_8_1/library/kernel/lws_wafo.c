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
*** File: lws_wafo.c
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
* Function Name    : _lwsem_wait_for
* Returned Value   : an error code
*   queue
* Comments         :
*   This function obtains a semaphore from the lwsem.  If one is not
* available, the task waits (in a FIFO order) for the
* specified number of ticks.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_wait_for
   (
      /* [IN] the semaphore address */
      LWSEM_STRUCT_PTR sem_ptr,

      /* [IN] the number of ticks to delay */
      MQX_TICK_STRUCT_PTR ticks
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_lwsem_wait_for, sem_ptr, ticks);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_lwsem_wait_for, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != LWSEM_VALID) {
      _KLOGX2(KLOG_lwsem_wait_for, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   _INT_DISABLE();
   if (sem_ptr->VALUE <= 0) {
      td_ptr = kernel_data->ACTIVE_PTR;
      /* Calculate time to wake up the task */
      PSP_ADD_TICKS(ticks, &kernel_data->TIME, &td_ptr->TIMEOUT);
      result = _lwsem_wait_timed_internal(sem_ptr, td_ptr);
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
      _KLOGX2(KLOG_lwsem_wait_for, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   _INT_ENABLE();

   _KLOGX2(KLOG_lwsem_wait_for, result);

   return(result);
   
} /* Endbody */

/* EOF */
