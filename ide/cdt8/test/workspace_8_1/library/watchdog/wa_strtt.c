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
*** File: wa_strtt.c
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
* Function Name   : _watchdog_start_ticks
* Returned Value  : boolean - TRUE if succeeded
* Comments        : 
*  This function starts a software watchdog for the
*  current task.  The timer will expire at the specified number of
*  ticks in the future.  The application should call
*  _watchdog_start_ticks periodically to re-arm the timer.
*
*END*------------------------------------------------------------------*/

boolean _watchdog_start_ticks
   (
      /*  [IN]  the time in ticks at which to expire the watchdog */
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR   kernel_data;)
   boolean                         result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE2(KLOG_watchdog_start_ticks, tick_ptr);

   result = _watchdog_start_internal(tick_ptr);

   _KLOGX2(KLOG_watchdog_start_ticks, result);

   return(result);

} /* Endbody */
#endif /* MQX_USE_SW_WATCHDOGS */

/* EOF */
