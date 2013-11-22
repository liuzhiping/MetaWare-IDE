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
*** File: ev_util.c
***
*** Comments:      
***   This file contains utility functions for Event component.
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
* Function Name    : _event_get_value
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to obtain the current event bits that have been set.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_get_value
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer users_event_ptr, 

      /* [IN] - The location where the data is to be stored */
      _mqx_uint_ptr event_value_ptr
   )
{ /* Body */
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
   register EVENT_STRUCT_PTR            event_ptr;

   *event_value_ptr = 0;
   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)users_event_ptr;
#if MQX_CHECK_VALIDITY
   if (event_connection_ptr->VALID != EVENT_VALID){
      return(EVENT_INVALID_EVENT_HANDLE); 
   } /* Endif */
#endif

   event_ptr = event_connection_ptr->EVENT_PTR;
#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != EVENT_VALID) {
      return(EVENT_INVALID_EVENT); 
   } /* Endif */
#endif

   *event_value_ptr = event_ptr->EVENT;

   return(MQX_OK);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _event_get_wait_count
* Returned Value   : 
*   Returns the number of waiting tasks.  If an error occurs
*   the value MQX_UINT_32 is returned, and the Task Error Code set.
* Comments         :
*    Used by a task to obtain the number of tasks waiting for an event.
*
* 
*END**********************************************************************/

_mqx_uint _event_get_wait_count
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer users_event_ptr
   )
{ /* Body */
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
   register EVENT_STRUCT_PTR            event_ptr;
   
   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)users_event_ptr;
#if MQX_CHECK_VALIDITY
   if (event_connection_ptr->VALID != EVENT_VALID){
      _task_set_error(EVENT_INVALID_EVENT_HANDLE);
      return((_mqx_uint)MAX_UINT_32);
   } /* Endif */
#endif

   event_ptr = event_connection_ptr->EVENT_PTR;
#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != EVENT_VALID) {
      _task_set_error(EVENT_INVALID_EVENT_HANDLE);
      return(MAX_MQX_UINT); 
   } /* Endif */
#endif

   return(_QUEUE_GET_SIZE(&(event_ptr->WAITING_TASKS)));

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
