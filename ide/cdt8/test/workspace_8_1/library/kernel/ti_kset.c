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
* Function Name    : _time_set
* Returned Value   : void
* Comments         :
*   This function sets the current time on the system.  The input time is
* the UCT time.
*
*END*----------------------------------------------------------------------*/

void _time_set
   (
      /* [IN] the address where the new time is to be taken from */
      register TIME_STRUCT_PTR time_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   MQX_TICK_STRUCT                 ticks;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_time_set, time_ptr, time_ptr->SECONDS, time_ptr->MILLISECONDS);

   /* Normalize time */
   MQX_NORMALIZE_TIME_STRUCT(time_ptr);

   /* First convert old time struct into the tick struct */
   PSP_TIME_TO_TICKS(time_ptr, &ticks);

   _INT_DISABLE();

   /* Calculate offset */
   PSP_SUB_TICKS(&ticks, &kernel_data->TIME, &kernel_data->TIME_OFFSET);

   _INT_ENABLE();

   _KLOGX1(KLOG_time_set);

} /* Endbody */

/* EOF */
