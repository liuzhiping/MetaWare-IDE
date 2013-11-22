/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: lwt_cncl.c
***
*** Comments:
***   This file contains the function for canceling a timer.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "lwtimer.h"
#include "lwtimprv.h"

#if MQX_USE_LWTIMER
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _lwtimer_cancel_timer
* Returned Value  : _mqx_uint MQX error code
* Comments        : 
*  This function will cancel an outstanding timer request                 
*
*END*------------------------------------------------------------------*/

_mqx_uint _lwtimer_cancel_timer
   (
      /* [IN] the timer to add to the period */
      LWTIMER_STRUCT_PTR        timer_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   LWTIMER_PERIOD_STRUCT_PTR period_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_lwtimer_cancel_timer, timer_ptr);

#if MQX_CHECK_VALIDITY
   if (timer_ptr->VALID != LWTIMER_VALID) {
      _KLOGX2(KLOG_lwtimer_cancel_timer, MQX_LWTIMER_INVALID);
      return MQX_LWTIMER_INVALID;
   } /* Endif */
#endif

   period_ptr = timer_ptr->PERIOD_PTR;
   _int_disable();
#if MQX_CHECK_VALIDITY
   if (period_ptr->VALID != LWTIMER_VALID) {
      _int_enable();
      _KLOGX2(KLOG_lwtimer_cancel_timer, MQX_LWTIMER_INVALID);
      return MQX_LWTIMER_INVALID;
   } /* Endif */
#endif
   timer_ptr->VALID = 0;
   if (timer_ptr == period_ptr->TIMER_PTR) {
      period_ptr->TIMER_PTR = (pointer)timer_ptr->LINK.PREV;
   } /* Endif */
   _QUEUE_REMOVE(&period_ptr->TIMERS, timer_ptr);
   _int_enable();

   _KLOGX2(KLOG_lwtimer_cancel_timer, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWTIMER */

/* EOF */

