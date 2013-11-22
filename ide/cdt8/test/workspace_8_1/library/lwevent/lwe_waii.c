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
*** File: lwe_waii.c
***
*** Comments:      
***   This file contains the function for waiting for a light weight event
***                                                               
*** $Header:lwe_waii.c, 9, 9/29/2005 1:12:00 PM, Goutham D. R.$
***
*** $NoKeywords$
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_wait_internal
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to for any specified event. If all time parameters are 0
*    then the timeout is infinite
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_wait_internal
   (
      /* [IN] - The address of the light weight event */
      LWEVENT_STRUCT_PTR   event_ptr, 

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint            bit_mask,
      
      /* [IN] - waiting for all bits or just any */
      boolean              all,

      /* [IN] - The maximum number of ticks to wait for or time to until */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /* [IN] - if wait is specified in ticks is it absolute or relative time */
      boolean              ticks_are_absolute

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   result = MQX_OK;
   td_ptr = kernel_data->ACTIVE_PTR;
   _INT_DISABLE();

#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != LWEVENT_VALID) {
      _int_enable();
      return(MQX_LWEVENT_INVALID);
   } /* Endif */
#endif

   if ( ( all && (event_ptr->VALUE & bit_mask) == bit_mask) ||
        (!all && (event_ptr->VALUE & bit_mask)))
   {
      if ((event_ptr->FLAGS & LWEVENT_AUTO_CLEAR) != 0) { // CR1366
         // Manual states: "If the lightweight event group has
         // autoclearing event bits, MQX clears the event bits
         // as soon as they are set and makes ONE task ready.
         event_ptr->VALUE &= ~bit_mask;
      } /* Endif */
      _INT_ENABLE();
      return(result);
   } /* Endif */

   /* Must wait for a event to become available */

   td_ptr->LWEVENT_BITS = bit_mask;
   if (all) {
      td_ptr->FLAGS |= TASK_LWEVENT_ALL_BITS_WANTED;
/* Start CR 1166 */
   } else {
      td_ptr->FLAGS &= ~TASK_LWEVENT_ALL_BITS_WANTED;
/* End CR   1166 */
   } /* Endif */

   /* Enqueue at end */   
   _QUEUE_ENQUEUE(&event_ptr->WAITING_TASKS, &td_ptr->AUX_QUEUE);

   /* Now put the task to sleep */
   td_ptr->STATE = LWEVENT_BLOCKED;
   td_ptr->INFO  = (_mqx_uint)&event_ptr->WAITING_TASKS;      
   if (tick_ptr) {
      if (ticks_are_absolute) {
         _time_delay_until(tick_ptr);
      } else {
         _time_delay_for(tick_ptr);
      } /* Endif */
      if (td_ptr->INFO) {
         /* Must have timed out */
/* Start CR 544 */
         /*_QUEUE_REMOVE(&event_ptr->WAITING_TASKS, &td_ptr->AUX_QUEUE);*/
/* End CR 544 */
         result = LWEVENT_WAIT_TIMEOUT;
      } /* Endif */
   } else {
      _task_block();
   } /* Endif */

#if MQX_COMPONENT_DESTRUCTION
   if (event_ptr->VALID == 0) {  /* We've been deleted */
      result = MQX_LWEVENT_INVALID;
   } /* Endif */
#endif

   _INT_ENABLE();
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
