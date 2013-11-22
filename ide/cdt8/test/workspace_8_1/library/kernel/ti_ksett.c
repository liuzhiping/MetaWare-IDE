/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ti_kset.c
***
*** Comments:      
***   This file contains the function for setting the current time.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_ticks
* Returned Value   : void
* Comments         :
*   This function sets the current time on the system.  The input time is
* in ticks.
*
*END*----------------------------------------------------------------------*/

void _time_set_ticks
   (
      /* [IN] the address where the new time is to be taken from */
      register MQX_TICK_STRUCT_PTR ticks
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_set_ticks, ticks);

   _INT_DISABLE();

   PSP_SUB_TICKS(ticks, &kernel_data->TIME, &kernel_data->TIME_OFFSET);

   _INT_ENABLE();

   _KLOGX1(KLOG_time_set_ticks);

} /* Endbody */

/* EOF */
