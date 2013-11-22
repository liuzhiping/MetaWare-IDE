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
*** File: ti_delay.c
***
*** Comments:      
***   This file contains the function for delaying a task for the 
*** specified number of milliseconds.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_delay
 * Returned Value   : void
 * Comments         :
 *   This function puts a task on the timeout queue for the specified number
 * of milliseconds, or until removed by another task.
 *
 *END*----------------------------------------------------------------------*/

void _time_delay
   (
      /* [IN] the number of milliseconds to delay */
      register uint_32 milliseconds
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           td_ptr;
   /* Start CR 330 */
   /*         TIME_STRUCT             time; */
   /* End CR 330 */

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_delay, milliseconds);

   if ( ! milliseconds ) {
      _KLOGX1(KLOG_time_delay);
      return;
   } /* Endif */

   /* Start CR 330 */
   /* MQX_TIME_NORMALIZE(0, milliseconds, time.SECONDS, time.MILLISECONDS); */
   /* End CR 330 */

   td_ptr = kernel_data->ACTIVE_PTR;

   /* Convert milliseconds to ticks */
   /* Start CR 330 */
   /* _INT_DISABLE(); */
   
   /* PSP_TIME_TO_TICKS(&time, &td_ptr->TIMEOUT); */
   PSP_MILLISECONDS_TO_TICKS_QUICK(milliseconds, &td_ptr->TIMEOUT);

   _INT_DISABLE();
   /* End CR 330 */

   /* Calculate time to wake up the task */
   PSP_ADD_TICKS(&td_ptr->TIMEOUT, &kernel_data->TIME, &td_ptr->TIMEOUT);

   _time_delay_internal(td_ptr);

   _INT_ENABLE();

   _KLOGX1(KLOG_time_delay);

} /* Endbody */

/* EOF */
