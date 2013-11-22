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
*** File: se_iprio.c
***
*** Comments:      
***   This file contains the utility function for inserting a semaphore
*** connection into a list, sorted by task priority.
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
* Function Name    : _sem_insert_priority_internal
* Returned Value   : none
* Comments         :
*   This function inserts a connection pointer by priority onto a queue.
* 
*END*----------------------------------------------------------------------*/

void _sem_insert_priority_internal
   (  
      /* [IN] the queue to insert the member into */
      QUEUE_STRUCT_PTR queue_ptr,

      /* [IN] the connection pointer to insert into the queue */
      SEM_CONNECTION_STRUCT_PTR sem_connection_ptr
   )
{ /* Body */
   SEM_CONNECTION_STRUCT_PTR conn2_ptr;
   SEM_CONNECTION_STRUCT_PTR conn_prev_ptr;
   _mqx_uint                  count;
   _mqx_uint                  priority;

   conn_prev_ptr = (SEM_CONNECTION_STRUCT_PTR)((pointer)queue_ptr);
   conn2_ptr     = (SEM_CONNECTION_STRUCT_PTR)((pointer)queue_ptr->NEXT);
   count         = _QUEUE_GET_SIZE(queue_ptr) + 1;
   priority      = sem_connection_ptr->TD_PTR->MY_QUEUE->PRIORITY;
   while (--count) {
      if (conn2_ptr->TD_PTR->MY_QUEUE->PRIORITY > priority) {
         break;
      } /* Endif */
      conn_prev_ptr = conn2_ptr;
      conn2_ptr     = (SEM_CONNECTION_STRUCT_PTR)conn2_ptr->NEXT;
   } /* Endwhile */
   _QUEUE_INSERT(queue_ptr, conn_prev_ptr, sem_connection_ptr);

} /* Endbody */
#endif /* MQX_USE_SEMAPHORES */

/* EOF */
