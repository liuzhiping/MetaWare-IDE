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
*** File: ti_st1af.c
***
*** Comments:
***   This file contains the function for starting a oneshot timer
*** after a certain delay.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "timer.h"
#include "tim_prv.h"

#if MQX_USE_TIMER
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _timer_start_oneshot_after
* Returned Value  : _timer_id - Returns id of timer or null on error
* Comments        : initialize a timer to fire once after a delay period.
*
*END*------------------------------------------------------------------*/

_timer_id _timer_start_oneshot_after
   ( 

      /* [IN] the function to call when the timer expires */
      void (_CODE_PTR_ notification_function_ptr)
         (_timer_id id, pointer data_ptr, uint_32 seconds, uint_32 milliseconds), 

      /* [IN] the data to pass to the function when the timer expires */
      pointer          notification_data_ptr,
      
      /* 
      ** [IN] which time to use when calculating time to fire 
      **   TIMER_ElAPSED_TIME_MODE
      **   TIMER_KERNEL_TIME_MODE
      */
      _mqx_uint        mode, 
      
      /* 
      ** [IN] the number of milliseconds to wait before calling the notification
      ** function, and canceling the timer.
      */
      uint_32          milliseconds

   )
{
   _KLOGM(KERNEL_DATA_STRUCT_PTR   kernel_data;)
   MQX_TICK_STRUCT                 ticks;
   /* Start CR 330 */
   /* TIME_STRUCT                     time; */
   /* End CR 330 */
   _timer_id                       result;
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_timer_start_oneshot_after, notification_function_ptr, mode, 
      milliseconds);

#if MQX_CHECK_ERRORS
   if (milliseconds == 0) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_timer_start_oneshot_after, TIMER_NULL_ID);
      return TIMER_NULL_ID;
   } /* Endif */
#endif

   /* Start CR 330 */
   /* time.SECONDS      = 0;            */
   /* time.MILLISECONDS = milliseconds; */
   /*                                   */
   /* PSP_TIME_TO_TICKS(&time, &ticks); */
   PSP_MILLISECONDS_TO_TICKS_QUICK(milliseconds, &ticks);
   /* End CR 330 */
  
   result = _timer_start_oneshot_after_internal(notification_function_ptr,
      notification_data_ptr, mode, (MQX_TICK_STRUCT_PTR)&ticks, FALSE);

   _KLOGX2(KLOG_timer_start_oneshot_after, result);

   return result;
   
} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */