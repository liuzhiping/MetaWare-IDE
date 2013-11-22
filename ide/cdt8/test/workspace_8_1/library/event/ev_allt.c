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
*** File: ev_allt.c
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
* Function Name    : _event_wait_all_ticks
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to wait for all specified events.
* 
*END*------------------------------------------------------------------*/
 
_mqx_uint _event_wait_all_ticks
   (
      /* [IN] - An event handle returned from a call to _event_open() or
      ** _event_open_fast()
      */
      pointer   users_event_ptr, 

      /* [IN] - bit mask - each bit of which represents an event. */
      _mqx_uint  bit_mask, 

      /* [IN] - The maximum number of ticks to wait for the events 
      **        to be set.  If the value is 0, then the timeout will be 
      **        infinite.
      */
      _mqx_uint  timeout_in_ticks
   )
{ /* Body */
   MQX_TICK_STRUCT                ticks;
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   _mqx_uint                      result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_event_wait_all_ticks, users_event_ptr, bit_mask, 
      timeout_in_ticks);

   if (!timeout_in_ticks) {
      result = _event_wait_internal(users_event_ptr, bit_mask, NULL,
         FALSE, TRUE);
   } else {
      ticks = _mqx_zero_tick_struct;

      PSP_ADD_TICKS_TO_TICK_STRUCT(&ticks, timeout_in_ticks, &ticks);
      result = _event_wait_internal(users_event_ptr, bit_mask, &ticks,
         FALSE, TRUE);
   } /* Endif */

   _KLOGX2(KLOG_event_wait_all_ticks, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
