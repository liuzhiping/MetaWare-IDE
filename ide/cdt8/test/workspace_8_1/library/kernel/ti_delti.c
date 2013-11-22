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
*** File: ti_delti.c
***
*** Comments:      
***   This file contains the function for delaying a task for the 
*** specified number of ticks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_delay_ticks
 * Returned Value   : void
 * Comments         :
 *   This function puts a task on the timeout queue for the specified number
 * of ticks, or until removed by another task.
 *
 *END*----------------------------------------------------------------------*/

void _time_delay_ticks
   (
      /* [IN] the number of ticks to delay */
      register _mqx_uint time_in_ticks
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_delay_ticks, time_in_ticks);

   if ( !time_in_ticks ) {
      _KLOGX1(KLOG_time_delay_ticks);
      return;
   } /* Endif */

   td_ptr = kernel_data->ACTIVE_PTR;

   _INT_DISABLE();

   PSP_ADD_TICKS_TO_TICK_STRUCT(&kernel_data->TIME, time_in_ticks, &td_ptr->TIMEOUT);

   _time_delay_internal(td_ptr);

   _INT_ENABLE();
   _KLOGX1(KLOG_time_delay_ticks);

} /* Endbody */

/* EOF */
