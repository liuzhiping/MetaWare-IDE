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
*** File: lws_wain.c
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
* Function Name    : _lwsem_wait_timed_internal
* Returned Value   : an error code
*   queue
* Comments         :
*   This function is an internal funciton, it waits for a light weight
* semaphore with a pre-calcualted timeout.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_wait_timed_internal
   (
      /* [IN] the semaphore address */
      LWSEM_STRUCT_PTR sem_ptr,

      /* [IN] the task descriptor waiting */
      TD_STRUCT_PTR          td_ptr
   )
{ /* Body */

   td_ptr->STATE = LWSEM_BLOCKED;
   td_ptr->INFO  = (_mqx_uint)&sem_ptr->TD_QUEUE;
   _QUEUE_UNLINK(td_ptr);
   _QUEUE_ENQUEUE(&sem_ptr->TD_QUEUE, &td_ptr->AUX_QUEUE);
   _time_delay_internal(td_ptr);
   if (td_ptr->INFO != 0) {
/* Start CR 544 */
      /*_QUEUE_REMOVE(&sem_ptr->TD_QUEUE, &td_ptr->AUX_QUEUE);*/
/* End CR 544 */
      return(MQX_LWSEM_WAIT_TIMEOUT);
   } /* Endif */
   
   return(MQX_OK);
   
} /* Endbody */

/* EOF */
