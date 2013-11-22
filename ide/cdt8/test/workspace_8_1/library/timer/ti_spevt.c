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
*** File: ti_spevt.c
***
*** Comments:
***   This file contains the function for starting a periodic timer
*** every given number of ticks
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
* Function Name   : _timer_start_periodic_every_ticks
* Returned Value  : _timer_id - Returns id of timer or null on error
* Comments        : initialize a timer to fire periodically.
*
*END*------------------------------------------------------------------*/

_timer_id _timer_start_periodic_every_ticks
   ( 

      /* [IN] the function to call when the timer expires */
      void (_CODE_PTR_    notification_function_ptr)
         (_timer_id, pointer, MQX_TICK_STRUCT_PTR), 

      /* [IN] the data to pass to the function when the timer expires */
      pointer             notification_data_ptr,
      
      /* 
      ** [IN] which time to use when calculating time to fire 
      **   TIMER_ElAPSED_TIME_MODE
      **   TIMER_KERNEL_TIME_MODE
      */
      _mqx_uint           mode, 
      
      /* 
      ** [IN] the number of ticks to wait before calling the notification
      ** function.
      */
      MQX_TICK_STRUCT_PTR wtick_ptr

   )
{
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   _timer_id                      result;
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_timer_start_periodic_every_ticks, notification_function_ptr, 
      mode, wtick_ptr);

#if MQX_CHECK_ERRORS
   if ((notification_function_ptr == NULL) || (wtick_ptr == NULL)) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_timer_start_periodic_every_ticks, TIMER_NULL_ID);
      return TIMER_NULL_ID;
   } /* Endif */
#endif

   result = _timer_start_periodic_every_internal(notification_function_ptr,
      notification_data_ptr, mode, wtick_ptr, TRUE);

   _KLOGX2(KLOG_timer_start_periodic_every_ticks, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */