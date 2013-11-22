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
*** File: wa_stop.c
***
*** Comments:
***   This file contains the function for stopping the watchdog.
***   routines.
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
* Function Name   : _watchdog_stop
* Returned Value  : boolean TRUE if succeeded
* Comments        : stop the watchdog timer
*
*END*------------------------------------------------------------------*/

boolean _watchdog_stop
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE1(KLOG_watchdog_stop);

   td_ptr = kernel_data->ACTIVE_PTR;
   if (td_ptr->FLAGS & TASK_WATCHDOG_STARTED) {
      _INT_DISABLE();
      /* Start CR 333 */
      /* td_ptr->FLAGS &= ~(TASK_WATCHDOG_STARTED | TASK_WATCHDOG_RUNNING); */
      td_ptr->FLAGS &= ~TASK_WATCHDOG_RUNNING;
      /* End CR 333 */
      _INT_ENABLE();
      _KLOGX2(KLOG_watchdog_stop, TRUE);
      return(TRUE);
   } /* Endif */

   _KLOGX2(KLOG_watchdog_stop, FALSE);
   return(FALSE);
   
} /* Endbody */
#endif /* MQX_USE_SW_WATCHDOGS */

/* EOF */
