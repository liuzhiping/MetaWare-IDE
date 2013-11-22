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
*** File: ti_stpev.c
***
*** Comments:
***   This file contains the function for starting a periodic timer
*** every given milliseconds.
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
* Function Name   : _timer_start_periodic_every
* Returned Value  : _timer_id - Returns id of timer or null on error
* Comments        : initialize a timer to fire periodically.
*
*END*------------------------------------------------------------------*/

_timer_id _timer_start_periodic_every
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
      ** function.
      */
      uint_32          milliseconds

   )
{
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   MQX_TICK_STRUCT                wticks;
   /* Start CR 330 */
   /* TIME_STRUCT                    wtime; */
   /* End CR 330 */
   _timer_id                      result;
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_timer_start_periodic_every, notification_function_ptr, mode, 
      milliseconds);

   /* Start CR 330 */
   /* wtime.SECONDS      = 0;             */
   /* wtime.MILLISECONDS = milliseconds;  */
   /*                                     */
   /* PSP_TIME_TO_TICKS(&wtime, &wticks); */
   PSP_MILLISECONDS_TO_TICKS_QUICK(milliseconds, &wticks);
   /* End CR 330 */

   result = _timer_start_periodic_every_internal(notification_function_ptr,
      notification_data_ptr, mode, (MQX_TICK_STRUCT_PTR)&wticks, FALSE);

   _KLOGX2(KLOG_timer_start_periodic_every, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */