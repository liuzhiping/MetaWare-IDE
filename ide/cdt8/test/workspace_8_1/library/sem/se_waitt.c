/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: se_waitt.c
***
*** Comments:      
***   This file contains the function for waiting on a semaphore.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "sem.h"
#include "sem_prv.h"

#if MQX_USE_SEMAPHORES
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sem_wait_ticks
* Returned Value   : _mqx_uint MQX_OK, or SEM_INVALID_SEMAPHORE_HANDLE,
*                       SEM_INVALID_SEMAPHORE_COUNT, SEM_WAIT_TIMEOUT
* Comments         :
*   This function waits for a semaphore to become available.  If one is not
* the task is queued according to the queueing policy for this semaphore.
* 
*END*----------------------------------------------------------------------*/

_mqx_uint _sem_wait_ticks
   (
      /* [IN] -  The semaphore handle returned by _sem_open. */
      pointer  users_sem_ptr,

      /* 
      ** [IN] - the number of ticks to wait for a semaphore.
      **        If the value is 0, then the timeout will be infinite.
      */
      _mqx_uint timeout_in_ticks
   )
{ /* Body */
   MQX_TICK_STRUCT                ticks;
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   _mqx_uint                      result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE3(KLOG_sem_wait_ticks, users_sem_ptr, timeout_in_ticks);

   if (timeout_in_ticks) {
      ticks = _mqx_zero_tick_struct;

      ticks.TICKS[0] = timeout_in_ticks;
      result = _sem_wait_internal(users_sem_ptr, &ticks, FALSE);
   } else {
      result = _sem_wait_internal(users_sem_ptr, NULL, FALSE);
   } /* Endif */

   _KLOGX2(KLOG_sem_wait_ticks, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
