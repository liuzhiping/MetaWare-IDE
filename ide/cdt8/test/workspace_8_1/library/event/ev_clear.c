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
*** File: ev_clear.c
***
*** Comments:      
***   This file contains the function for clearing an event.
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
* Function Name    : _event_clear
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to clear the specified event bits in an event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_clear
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer  users_event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint bit_mask
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
   register EVENT_STRUCT_PTR            event_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_event_clear, users_event_ptr, bit_mask);
   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)users_event_ptr;

#if MQX_CHECK_VALIDITY
   if (event_connection_ptr->VALID != EVENT_VALID){
      _KLOGX2(KLOG_event_clear, EVENT_INVALID_EVENT_HANDLE);
      return(EVENT_INVALID_EVENT_HANDLE); 
   } /* Endif */
#endif

   _INT_DISABLE();
   event_ptr = event_connection_ptr->EVENT_PTR;

#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != EVENT_VALID) {
      _int_enable();
      _KLOGX2(KLOG_event_clear, EVENT_INVALID_EVENT);
      return(EVENT_INVALID_EVENT); 
   } /* Endif */
#endif

   event_ptr->EVENT &= ~bit_mask;
   _INT_ENABLE();

   _KLOGX2(KLOG_event_clear, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
