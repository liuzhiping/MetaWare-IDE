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
*** File: ti_delun.c
***
*** Comments:      
***   This file contains the function for delaying a task until the 
*** specified number of ticks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_delay_until
 * Returned Value   : void
 * Comments         :
 *   This function puts a task on the timeout queue until the specified
 * tick count is reached, or until removed by another task.
 *
 *END*----------------------------------------------------------------------*/

void _time_delay_until
   (
      /* [IN] the time to delay until */
      register MQX_TICK_STRUCT_PTR ticks
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_delay_until, ticks);

#if MQX_CHECK_ERRORS
   if (ticks == NULL) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_time_delay_until, MQX_INVALID_PARAMETER);
      return;
   } /* Endif */
#endif

   td_ptr = kernel_data->ACTIVE_PTR;

   /* Calculate time to wake up the task */
   td_ptr->TIMEOUT = *ticks;

   _INT_DISABLE();

   _time_delay_internal(td_ptr);

   _INT_ENABLE();

   _KLOGX1(KLOG_time_delay_until);

} /* Endbody */

/* EOF */
