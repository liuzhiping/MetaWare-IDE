/**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ev_allf.c
***
*** Comments:      
***   This file contains the function for waiting for all requested bits
*** to be set in the event.
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
* Function Name    : _event_wait_all_for
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to wait for all specified events.
* 
*END*------------------------------------------------------------------*/
 
_mqx_uint _event_wait_all_for
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer             users_event_ptr, 

      /* [IN] - bit mask - each bit of which represents an event. */
      _mqx_uint            bit_mask, 

      /* [IN] - The maximum number of ticks to wait for the events 
      **        to be set.  If the value is NULL, then the timeout will be 
      **        infinite.
      */
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR      kernel_data;)
   _mqx_uint                    result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_event_wait_all_for, users_event_ptr, bit_mask, tick_ptr);

   result = _event_wait_internal(users_event_ptr, bit_mask, tick_ptr, 
      FALSE, TRUE);

   _KLOGX2(KLOG_event_wait_all_for, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */