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
*** File: ev_anyi.c
***
*** Comments:      
***   This file contains the functions for waiting for any event in a set.
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
* Function Name    : _event_wait_any_internal
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to for any specified event. If all time parameters are 0
*    then the timeout is infinite
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_wait_any_internal
   (
      /* [IN] - An event handle returned from a call to _event_open() */
      pointer              users_event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint            bit_mask, 

      /* [IN] - The maximum number of ticks to wait for or time to until */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /* [IN] - if wait is specified in ticks is it absolute or relative time */
      boolean              ticks_are_absolute

   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
            TD_STRUCT_PTR               td_ptr;
   register EVENT_STRUCT_PTR            event_ptr;
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)users_event_ptr;

#if MQX_CHECK_VALIDITY
   if (event_connection_ptr->VALID != EVENT_VALID){
      return(EVENT_INVALID_EVENT_HANDLE); 
   } /* Endif */
#endif

   if (event_connection_ptr->REMOTE_CPU) {
      _KLOGX2(KLOG_event_wait_any, EVENT_CANNOT_WAIT_ON_REMOTE_EVENT);
      return(EVENT_CANNOT_WAIT_ON_REMOTE_EVENT); 
   }/* Endif */

   td_ptr = kernel_data->ACTIVE_PTR;

#if MQX_CHECK_ERRORS
   if (event_connection_ptr->TD_PTR != td_ptr) {
      return(EVENT_INVALID_EVENT_HANDLE); 
   } /* Endif */
#endif

   _INT_DISABLE();
   event_ptr = event_connection_ptr->EVENT_PTR;

#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != EVENT_VALID) {
      _int_enable();
      return(EVENT_INVALID_EVENT); 
   } /* Endif */
#endif

   if ((event_ptr->EVENT & bit_mask) == 0) {
      /* Must wait for a event to become available */

      event_connection_ptr->FLAGS  = 0;
      event_connection_ptr->MASK = bit_mask;

      /* Enqueue at end */   
      _QUEUE_ENQUEUE(&event_ptr->WAITING_TASKS, event_connection_ptr);

      /* Now put the task to sleep */
      td_ptr->STATE = EVENT_BLOCKED;
      
      if (tick_ptr) {
         if (ticks_are_absolute) {
            _time_delay_until(tick_ptr);
         } else {
            _time_delay_for(tick_ptr);
         } /* Endif */
      } else {
         _task_block();
      } /* Endif */

#if MQX_COMPONENT_DESTRUCTION
      if (event_ptr->VALID == 0) {  /* We've been deleted */
         _int_enable();
         return(EVENT_DELETED);
      } /* Endif */
#endif

      _QUEUE_REMOVE(&event_ptr->WAITING_TASKS, event_connection_ptr);

      if (tick_ptr && !(event_connection_ptr->FLAGS & EVENT_OCCURRED)) {
         /* MUST have timed out */
         _INT_ENABLE();
         return(EVENT_WAIT_TIMEOUT);
      } /* Endif */
   } /* Endif */

   _INT_ENABLE();  /* GOT IT */

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
