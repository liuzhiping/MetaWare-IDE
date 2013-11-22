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
*** File: lws_dest.c
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
* Function Name    : _lwsem_destroy
* Returned Value   : error code
* Comments         :
*   This function deletes a light weight semaphore.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_destroy
   (
      /* [IN] the semaphore handle */
      LWSEM_STRUCT_PTR sem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
#if MQX_COMPONENT_DESTRUCTION
   TD_STRUCT_PTR          td_ptr;
#endif

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_lwsem_destroy, sem_ptr);
#if MQX_COMPONENT_DESTRUCTION

   _int_disable();
#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != LWSEM_VALID) {
      _int_enable();
      _KLOGX2(KLOG_lwsem_destroy, MQX_INVALID_LWSEM);
      return(MQX_INVALID_LWSEM);
   } /* Endif */
#endif

   sem_ptr->VALID  = 0;   /* Invalidate the semaphore */
   while (_QUEUE_GET_SIZE(&sem_ptr->TD_QUEUE)) {
      _QUEUE_DEQUEUE(&sem_ptr->TD_QUEUE, td_ptr);
      _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
      _TIME_DEQUEUE(td_ptr, kernel_data);
      _TASK_READY(td_ptr, kernel_data);
   } /* Endwhile */   

   /* remove semaphore from kernel LWSEM queue */
   _QUEUE_REMOVE(&kernel_data->LWSEM, sem_ptr);

   _int_enable();
     
   _CHECK_RUN_SCHEDULER();   /* Allow higher priority tasks to run */

#endif

   _KLOGX2(KLOG_lwsem_destroy, MQX_OK);
   return(MQX_OK);
   
} /* Endbody */

/* EOF */
