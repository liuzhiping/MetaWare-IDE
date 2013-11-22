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
*** File: ev_close.c
***
*** Comments:      
***   This file contains the function for closing an event.
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
* Function Name    : _event_close
* Returned Value   : 
*   Returns MQX_OK upon success, or a MQX error code:
* Comments         :
*    Used by a task to close (relinquish access to) an event instance.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_close
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer users_event_ptr 
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_event_close, users_event_ptr);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_event_close, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)users_event_ptr;
#if MQX_CHECK_VALIDITY
   if (event_connection_ptr->VALID != EVENT_VALID) {
      _KLOGX2(KLOG_event_close, EVENT_INVALID_EVENT_HANDLE);
      return(EVENT_INVALID_EVENT_HANDLE); 
   } /* Endif */

#endif

   event_connection_ptr->VALID = 0;

   if (_mem_free(event_connection_ptr) != MQX_OK) {
      _KLOGX2(KLOG_event_close, kernel_data->ACTIVE_PTR->TASK_ERROR_CODE);
      return(kernel_data->ACTIVE_PTR->TASK_ERROR_CODE);
   } else {
      _KLOGX2(KLOG_event_close, MQX_OK);
      return(MQX_OK);
   } /* Endif */

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
