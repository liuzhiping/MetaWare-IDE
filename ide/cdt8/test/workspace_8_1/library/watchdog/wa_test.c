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
*** File: wa_test.c
***
*** Comments:
***   This file contains the function for testing the watchdog component.
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
* Function Name   : _watchdog_test
* Returned Value  : _mqx_uint - MQX_OK or an MQX error code.
* Comments        : 
*   This functions tests the consistency and validity of the watchdog
* component.
*
*END*------------------------------------------------------------------*/

_mqx_uint _watchdog_test
   (
      /*  [OUT]  the watchdog component base if an error occurs */
      pointer _PTR_ watchdog_error_ptr,

      /*  [OUT]  the watchdog table pointer if an error occurs */
      pointer _PTR_ watchdog_table_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR          kernel_data;
   WATCHDOG_COMPONENT_STRUCT_PTR   watchdog_component_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_watchdog_test, watchdog_error_ptr, watchdog_table_error_ptr);

   *watchdog_error_ptr = NULL;
   *watchdog_table_error_ptr = NULL;

   watchdog_component_ptr = (WATCHDOG_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_WATCHDOG];
   if (watchdog_component_ptr == NULL) {
      _KLOGX2(KLOG_watchdog_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   *watchdog_error_ptr = watchdog_component_ptr;
   if (watchdog_component_ptr->VALID != WATCHDOG_VALID) {
      _KLOGX2(KLOG_watchdog_test, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */

   _KLOGX2(KLOG_watchdog_test, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_SW_WATCHDOGS */

/* EOF */
