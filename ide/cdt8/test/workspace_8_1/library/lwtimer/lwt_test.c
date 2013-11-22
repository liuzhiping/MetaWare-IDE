/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: lwt_test.c
***
*** Comments:
***   This file contains the function that tests the light weight timers.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "lwtimer.h"
#include "lwtimprv.h"

#if MQX_USE_LWTIMER
/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _lwtimer_test
* Return Value    : _mqx_uint - MQX_OK or error code
* Comments        : This function tests the timer component for validity
*   and consistency.
*
*END*------------------------------------------------------------------*/

_mqx_uint _lwtimer_test
   (
      /* [OUT] the timer period in error */
      pointer _PTR_ period_error_ptr,

      /* [OUT] the timer element in error */
      pointer _PTR_ timer_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   LWTIMER_STRUCT_PTR        timer_ptr;
   LWTIMER_PERIOD_STRUCT_PTR period_ptr;
   _mqx_uint                 result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_lwtimer_test, period_error_ptr, timer_error_ptr);

   *period_error_ptr = NULL;
   *timer_error_ptr  = NULL;

   /* 
   ** It is not considered an error if the lwtimer component has not been
   ** created yet
   */
   if (kernel_data->LWTIMERS.NEXT == NULL) {
      return(MQX_OK);
   } /* Endif */
  
   result = _queue_test(&kernel_data->LWTIMERS, period_error_ptr);
   if (result != MQX_OK) {
      _KLOGX3(KLOG_lwtimer_test, result, *period_error_ptr);
      return(result);
   } /* Endif */
   
   _int_disable();
   period_ptr = (pointer)kernel_data->LWTIMERS.NEXT;
   while ((pointer)period_ptr != (pointer)&kernel_data->LWTIMERS) {
      if (period_ptr->VALID != LWTIMER_VALID) {
         _int_enable();
         *period_error_ptr = period_ptr;
         _KLOGX3(KLOG_lwtimer_test, MQX_LWTIMER_INVALID, period_ptr);
         return(MQX_LWTIMER_INVALID);
      } /* Endif */
      result = _queue_test(&period_ptr->TIMERS, timer_error_ptr);
      if (result != MQX_OK) {
	     /* START CR 2066 */
         _int_enable();
		 /* END CR 2066 */
         *period_error_ptr = period_ptr;
         _KLOGX4(KLOG_lwtimer_test, result, *period_error_ptr, *timer_error_ptr);
         return(result);
      } /* Endif */
      timer_ptr = (pointer)period_ptr->TIMERS.NEXT;
      while (timer_ptr != (pointer)&period_ptr->TIMERS) {
         if (timer_ptr->VALID != LWTIMER_VALID) {
 	        /* START CR 2231 */
            _int_enable();
		    /* END CR 2231 */
            *period_error_ptr = period_ptr;
            *timer_error_ptr  = timer_ptr;
           _KLOGX4(KLOG_lwtimer_test, MQX_LWTIMER_INVALID, period_ptr,
               timer_ptr);
            return(MQX_LWTIMER_INVALID);
         } /* Endif */
         timer_ptr = (pointer)timer_ptr->LINK.NEXT;
      } /* Endwhile */
      period_ptr = (pointer)period_ptr->LINK.NEXT;
   } /* Endwhile */

   _int_enable();
   _KLOGX2(KLOG_lwtimer_test, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWTIMER */

/* EOF */
