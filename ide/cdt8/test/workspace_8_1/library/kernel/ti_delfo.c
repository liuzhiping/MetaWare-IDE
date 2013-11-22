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
*** File: ti_delfo.c
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
 * Function Name    : _time_delay_for
 * Returned Value   : void
 * Comments         :
 *   This function puts a task on the timeout queue for the specified number
 * of ticks, or until removed by another task.
 *
 *END*----------------------------------------------------------------------*/

void _time_delay_for
   (
      /* [IN] the number of ticks to delay */
      register MQX_TICK_STRUCT_PTR ticks
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           td_ptr;
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_delay_for, ticks);

#if MQX_CHECK_ERRORS
   if (ticks == NULL) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_time_delay_for, MQX_INVALID_PARAMETER);
      return;
   } /* Endif */
#endif

   td_ptr = kernel_data->ACTIVE_PTR;

   _INT_DISABLE();

   /* Calculate time to wake up the task */
   PSP_ADD_TICKS(ticks, &kernel_data->TIME, &td_ptr->TIMEOUT);

   _time_delay_internal(td_ptr);

   _INT_ENABLE();

   _KLOGX1(KLOG_time_delay_for);

} /* Endbody */

/* EOF */
