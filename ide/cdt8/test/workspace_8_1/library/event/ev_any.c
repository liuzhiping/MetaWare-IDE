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
*** File: ev_any.c
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
* Function Name    : _event_wait_any
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to for any specified event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_wait_any
   (
      /* [IN] - An event handle returned from a call to _event_open() */
      pointer   users_event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint  bit_mask, 

      /* [IN] - The maximum number of milliseconds to wait for the events 
      **        to be set.  If the value is 0, then the timeout will be 
      **        infinite.
      */
      uint_32   timeout 
   )
{ /* Body */
   /* Start CR 330 */
   /* TIME_STRUCT                    time; */
   /* End CR 330 */
   MQX_TICK_STRUCT                ticks;
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   _mqx_uint                      result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_event_wait_any, users_event_ptr, bit_mask, timeout);

   if (timeout) {
      /* Convert milliseconds to ticks */
      /* Start CR 330 */
      /* time.MILLISECONDS = timeout % 1000; */
      /* time.SECONDS      = timeout / 1000; */
      /*                                     */
      /* PSP_TIME_TO_TICKS(&time, &ticks);   */
      PSP_MILLISECONDS_TO_TICKS_QUICK(timeout, &ticks);
      /* End CR 330 */

      result = _event_wait_internal(users_event_ptr, bit_mask, &ticks, 
         FALSE, FALSE);
   } else {
      result = _event_wait_internal(users_event_ptr, bit_mask, NULL, 
         FALSE, FALSE);
   } /* Endif */

   _KLOGX2(KLOG_event_wait_any, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
