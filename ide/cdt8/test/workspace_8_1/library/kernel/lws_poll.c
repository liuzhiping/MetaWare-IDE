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
*** File: lws_poll.c
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
* Function Name    : _lwsem_poll
* Returned Value   : TRUE is semaphore obtained, otherwise FALSE if not 
*   available
* Comments         :
*   This function obtains a semaphore from the lwsem if one is
* available returning TRUE, otherwise it returns FALSE.  This call does not
* block.
*
*END*----------------------------------------------------------------------*/

boolean _lwsem_poll
   (
      /* [IN] the semaphore address */
      LWSEM_STRUCT_PTR sem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   boolean                result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_lwsem_poll, sem_ptr);

#if MQX_CHECK_VALIDITY
   if (sem_ptr->VALID != LWSEM_VALID) {
      _KLOGX2(KLOG_lwsem_poll, FALSE);
      return(FALSE);
   } /* Endif */
#endif

   _INT_DISABLE();
   if (sem_ptr->VALUE <= 0) {
      result = FALSE;
   } else {
      --sem_ptr->VALUE;
      result = TRUE;
   } /* Endif */
   _INT_ENABLE();

   _KLOGX2(KLOG_lwsem_poll, result);

   return(result);
   
} /* Endbody */

/* EOF */
