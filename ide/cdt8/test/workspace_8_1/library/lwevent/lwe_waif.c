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
*** File: lwe_waif.c
***
*** Comments:      
***   This file contains the functions for waiting for a light weight event.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_wait_for
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to wait for a specified event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_wait_for
   (
      /* [IN] - The address of the light weight event */
      LWEVENT_STRUCT_PTR   event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint            bit_mask, 

      /* [IN] - boolean, wait for all bits or just any bits */
      boolean              all,
      
      /* [IN] - The maximum number of ticks to wait for the events 
      **        to be set.  If the value is 0, then the timeout will be 
      **        infinite.
      */
      MQX_TICK_STRUCT_PTR   tick_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR   kernel_data;)
   _mqx_uint                       result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE5(KLOG_lwevent_wait_for, event_ptr, bit_mask, all, tick_ptr);

   result = _lwevent_wait_internal(event_ptr, bit_mask, all, 
      tick_ptr, FALSE);

   _KLOGX2(KLOG_lwevent_wait_for, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
