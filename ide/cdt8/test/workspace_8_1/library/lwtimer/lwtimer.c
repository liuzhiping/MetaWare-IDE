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
*** File: lwtimer.c
***
*** Comments:
***   This file contains the function for creating the timer
*** component.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "lwtimer.h"
#include "lwtimprv.h"

#if MQX_USE_LWTIMER
/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _lwtimer_create_periodic_queue
* Return Value    : _mqx_uint MQX_OK or error code
* Comments        : This is the function that installs a periodic
*   timer queue.
*
*END*------------------------------------------------------------------*/

_mqx_uint _lwtimer_create_periodic_queue
   (
      /* [IN] the location of the data structure defining the
      ** timing cycle.
      */
      LWTIMER_PERIOD_STRUCT_PTR period_ptr,

      /* [IN] the cycle length of this timer in ticks */
      _mqx_uint                 period,

      /* [IN] the number of ticks to wait before starting this queue */
      _mqx_uint                 wait_ticks
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_lwtimer_create_periodic_queue, period_ptr, period, wait_ticks);

   period_ptr->PERIOD = period;
   period_ptr->EXPIRY = 0;
   period_ptr->WAIT   = wait_ticks;
   _QUEUE_INIT(&period_ptr->TIMERS,0);
   period_ptr->TIMER_PTR = (pointer)&period_ptr->TIMERS;

   _int_disable();
   if (kernel_data->LWTIMERS.NEXT == NULL) {
      /* Initialize the light weight timer queue */
      _QUEUE_INIT(&kernel_data->LWTIMERS, 0);
      kernel_data->LWTIMER_ISR = _lwtimer_isr_internal;
   } /* Endif */
   period_ptr->VALID  = LWTIMER_VALID;
   _QUEUE_ENQUEUE(&kernel_data->LWTIMERS, &period_ptr->LINK);
   _int_enable();

   _KLOGX2(KLOG_lwtimer_create_periodic_queue, MQX_OK);
   return(MQX_OK);

} /* Endbody */


/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _lwtimer_add_timer_to_queue
* Return Value    : _mqx_uint MQX_OK or error code
* Comments        : This is the function that installs a timer onto
*   a periodic timer queue.
*
*END*------------------------------------------------------------------*/

_mqx_uint _lwtimer_add_timer_to_queue
   (
      /* [IN] the location of the data structure defining the
      ** timing cycle.
      */
      LWTIMER_PERIOD_STRUCT_PTR period_ptr,
      
      /* [IN] the timer to add to the period */
      LWTIMER_STRUCT_PTR        timer_ptr,

      /* [IN] tick offset from the timers period to fire at */
      _mqx_uint                 ticks,

      /* [IN] the function to call */
      void          (_CODE_PTR_ func)(pointer),

      /* [IN] the parameter to pass to the function */
      pointer                   parameter
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWTIMER_STRUCT_PTR     qe_ptr;
   _mqx_uint              i;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_lwtimer_add_timer_to_queue, period_ptr, timer_ptr, ticks);

#if MQX_CHECK_ERRORS
   if (period_ptr->VALID != LWTIMER_VALID) {
      _KLOGX2(KLOG_lwtimer_add_timer_to_queue, MQX_LWTIMER_INVALID);
      return(MQX_LWTIMER_INVALID);
   } /* Endif */
   if (ticks >= period_ptr->PERIOD) {
      _KLOGX2(KLOG_lwtimer_add_timer_to_queue, MQX_INVALID_PARAMETER);
      return(MQX_INVALID_PARAMETER);
   } /* Endif */
#endif

   timer_ptr->TIMER_FUNCTION = func;
   timer_ptr->PARAMETER      = parameter;
   timer_ptr->PERIOD_PTR     = period_ptr;
   timer_ptr->RELATIVE_TICKS = ticks;
   _int_disable();
   /* Insert into queue in order of increasing offset from start time */
   qe_ptr = (pointer)&period_ptr->TIMERS.NEXT;
   /* SPR P171-0023-01 use pre-decrement on while loop */
   i = _QUEUE_GET_SIZE(&period_ptr->TIMERS) + 1;
   while (--i) {
   /* END SPR */
      if (qe_ptr->RELATIVE_TICKS >= ticks) {
         qe_ptr = (pointer)qe_ptr->LINK.PREV;
         break;
      } /* Endif */
      qe_ptr = (pointer)qe_ptr->LINK.NEXT;
   } /* Endwhile */
   timer_ptr->VALID = LWTIMER_VALID;
   _QUEUE_INSERT(&period_ptr->TIMERS, qe_ptr, &timer_ptr->LINK);
   _int_enable();

   _KLOGX2(KLOG_lwtimer_add_timer_to_queue, MQX_OK);
   return(MQX_OK);

} /* Endbody */


/*FUNCTION*--------------------------------------------------------------------
*
* Function Name    : _lwtimer_isr_internal
* Returned Value   : none
* Comments         :
*   check the lwtimer queues, and if a timer is ready
* call the application function.
*
*END*-------------------------------------------------------------------------*/

void _lwtimer_isr_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   LWTIMER_PERIOD_STRUCT_PTR period_ptr;
   LWTIMER_STRUCT_PTR        timer_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   
   period_ptr = (pointer)kernel_data->LWTIMERS.NEXT;
   while ((pointer)period_ptr != (pointer)&kernel_data->LWTIMERS) {
      if (period_ptr->WAIT) {
         --period_ptr->WAIT;
      } else {
         timer_ptr = (pointer)period_ptr->TIMER_PTR->LINK.NEXT;
         while (timer_ptr != (pointer)&period_ptr->TIMERS) {
            if (timer_ptr->RELATIVE_TICKS > period_ptr->EXPIRY) {
               break;
            } /* Endif */
/* START CR 304 */
            period_ptr->TIMER_PTR = timer_ptr;
/* END CR 304 */
            (*timer_ptr->TIMER_FUNCTION)(timer_ptr->PARAMETER);
            timer_ptr = (LWTIMER_STRUCT_PTR)(pointer)timer_ptr->LINK.NEXT;
/* START CR 304 */
#if 0
            period_ptr->TIMER_PTR = timer_ptr;
#endif
/* END START CR 304 */
         } /* Endwhile */
         if (++period_ptr->EXPIRY == period_ptr->PERIOD) {
            period_ptr->EXPIRY = 0;
            period_ptr->TIMER_PTR = (pointer)&period_ptr->TIMERS;
         } /* Endif */
      } /* Endif */
      period_ptr = (LWTIMER_PERIOD_STRUCT_PTR)(pointer)period_ptr->LINK.NEXT;
   } /* Endwhile */

} /* Endbody */
#endif /* MQX_USE_LWTIMER */

/* EOF */
