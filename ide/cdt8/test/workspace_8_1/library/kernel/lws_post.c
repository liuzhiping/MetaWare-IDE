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
*** File: lws_post.c
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
* Function Name    : _lwsem_post
* Returned Value   : an error code
*   queue
* Comments         :
*   This function posts a semaphore to the lwsem.  If any tasks are waiting
* for semaphores, the first available task is made ready.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_post
   (
      /* [IN] the semaphore address */
      LWSEM_STRUCT_PTR  sem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_lwsem_post, sem_ptr);

#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != LWSEM_VALID) {
      _KLOGX2(KLOG_lwsem_post, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   _INT_DISABLE();
   if ((sem_ptr->VALUE >= 0) && (_QUEUE_GET_SIZE(&sem_ptr->TD_QUEUE))) {
      _QUEUE_DEQUEUE(&sem_ptr->TD_QUEUE, td_ptr);
      _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
      _TIME_DEQUEUE(td_ptr, kernel_data);
      td_ptr->INFO = 0;  /* Signal that post is activating the task */
      _TASK_READY(td_ptr, kernel_data);
      _CHECK_RUN_SCHEDULER(); /* Let higher priority task run */
   } else {
      ++sem_ptr->VALUE;      
   } /* Endif */
   _INT_ENABLE();

   _KLOGX2(KLOG_lwsem_post, MQX_OK);

   return(MQX_OK);
   
} /* Endbody */

/* EOF */
