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
*** File: lwt_cncp.c
***
*** Comments:
***   This file contains the function for canceling a timer period.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "lwtimer.h"
#include "lwtimprv.h"

#if MQX_USE_LWTIMER
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _lwtimer_cancel_period
* Returned Value  : _mqx_uint MQX error code
* Comments        : 
*  This function will cancel an entire timer period.
*
*END*------------------------------------------------------------------*/

_mqx_uint _lwtimer_cancel_period
   (
      /* [IN] the timer period to cancel */
      LWTIMER_PERIOD_STRUCT_PTR period_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_lwtimer_cancel_period, period_ptr);

#if MQX_CHECK_VALIDITY
   if (period_ptr->VALID != LWTIMER_VALID) {
      _KLOGX2(KLOG_lwtimer_cancel_period, MQX_LWTIMER_INVALID);
      return MQX_LWTIMER_INVALID;
   } /* Endif */
#endif

   _int_disable();
   period_ptr->VALID = 0;
   _QUEUE_REMOVE(&kernel_data->LWTIMERS, period_ptr);
   _int_enable();

   _KLOGX2(KLOG_lwtimer_cancel_period, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWTIMER */

/* EOF */

