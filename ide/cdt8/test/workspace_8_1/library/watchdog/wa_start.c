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
*** File: wa_start.c
***
*** Comments:
***   This file contains the function for starting a watchdog.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "watchdog.h"
#include "wdog_prv.h"

#if MQX_USE_SW_WATCHDOGS
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _watchdog_start
* Returned Value  : boolean TRUE if succeeded
* Comments        : This function starts a software watchdog for the
*  current task.  The timer will expire at the specified number of
*  milliseconds in the future.  The application should call
*  _watchdog_start periodically to re-arm the timer.
*
*END*------------------------------------------------------------------*/

boolean _watchdog_start
   (
      /*  [IN]  the time in ms at which to expire the watchdog */
      uint_32 time
   )
{
   _KLOGM(KERNEL_DATA_STRUCT_PTR   kernel_data;)
   boolean                         result;
   MQX_TICK_STRUCT                 start_time;
   /* Start CR 330 */
   /* TIME_STRUCT                     tmp; */
   /* End CR 330 */

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE2(KLOG_watchdog_start, time);

   /* Start CR 330 */
   /* tmp.MILLISECONDS = time;              */
   /* tmp.SECONDS      = 0;                 */
   /*                                       */
   /* PSP_TIME_TO_TICKS(&tmp, &start_time); */
   PSP_MILLISECONDS_TO_TICKS_QUICK(time, &start_time);
   /* End CR 330 */

   result = _watchdog_start_internal((MQX_TICK_STRUCT_PTR)&start_time);

   _KLOGX2(KLOG_watchdog_start, result);

   return(result);

} /* Endbody */
#endif /* MQX_USE_SW_WATCHDOGS */

/* EOF */
