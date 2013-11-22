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
*** File: ti_st1at.c
***
*** Comments:
***   This file contains the function for starting a oneshot timer
*** at a certain time.
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
* Function Name   : _timer_start_oneshot_at
* Returned Value  : _timer_id - Returns id of timer or null on error
* Comments        : initialize a timer to fire once at a specified time.
*
*END*------------------------------------------------------------------*/

_timer_id _timer_start_oneshot_at
   ( 

      /* [IN] the function to call when the timer expires */
      void (_CODE_PTR_ notification_function_ptr)
         (_timer_id, pointer, uint_32, uint_32), 

      /* [IN] the data to pass to the function when the timer expires */
      pointer          notification_data_ptr,
      
      /* 
      ** [IN] which time to use when calculating time to fire 
      **   TIMER_ElAPSED_TIME_MODE
      **   TIMER_KERNEL_TIME_MODE
      */
      _mqx_uint        mode, 
      
      /* 
      ** [IN] the time at which to call the notification
      ** function, and then cancel the timer.
      */
      TIME_STRUCT_PTR  time_ptr

   )
{
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   MQX_TICK_STRUCT                ticks;
   _timer_id                      result;
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_timer_start_oneshot_at, notification_function_ptr, mode, time_ptr);

#if MQX_CHECK_ERRORS
   if (time_ptr == NULL) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_timer_start_oneshot_at, TIMER_NULL_ID);
      return TIMER_NULL_ID;
   } /* Endif */
#endif

   PSP_TIME_TO_TICKS(time_ptr, &ticks);

   result = _timer_start_oneshot_at_internal(notification_function_ptr,
      notification_data_ptr, mode, (MQX_TICK_STRUCT_PTR)&ticks, FALSE);

   _KLOGX2(KLOG_timer_start_oneshot_at, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */