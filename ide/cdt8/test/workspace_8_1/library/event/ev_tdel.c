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
*** File: ev_tdel.c
***
*** Comments:      
***   This file contains the function for cleaning up events when a task
*** is deleted.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_cleanup
* Returned Value   : none
* Comments         :
*    Used during task destruction to free up any events resources
* owned by this task.
* The function walks the resource list of the task looking for 
* event connection struct.  It can detect one by looking for EVENT_VALID
* in the correct location.
* 
*END*------------------------------------------------------------------*/

void _event_cleanup
   ( 
      /* [IN] the task descriptor of the task being destroyed */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
   EVENT_CONNECTION_STRUCT_PTR connection_ptr;
   EVENT_STRUCT_PTR            event_ptr;

   connection_ptr = _mem_get_next_block_internal(td_ptr, NULL);
   while (connection_ptr) {
      if ((connection_ptr->VALID == EVENT_VALID) &&
         (connection_ptr->TD_PTR == td_ptr) )
      {
         event_ptr = connection_ptr->EVENT_PTR;
         if (event_ptr->VALID == EVENT_VALID) {
             /* Check if the connection is on the queue */
             _int_disable();
             event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)
                ((pointer)event_ptr->WAITING_TASKS.NEXT);
             while (event_connection_ptr != 
                (pointer)&event_ptr->WAITING_TASKS.NEXT) 
             {
                if (event_connection_ptr == connection_ptr) {
                   /* Connection is queued, so dequeue it */
                   _QUEUE_REMOVE(&event_ptr->WAITING_TASKS, connection_ptr);
                   break;
                }/* Endif */
                event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)
                   event_connection_ptr->NEXT;
             } /* Endwhile */
             _int_enable();
         }/* Endif */
      } /* Endif */
      connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)
         _mem_get_next_block_internal(td_ptr, connection_ptr);
   } /* Endwhile */

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
