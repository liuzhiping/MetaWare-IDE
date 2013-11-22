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
*** File: ti_dque.c
***
*** Comments:      
***   This file contains the function for removing a task from the
*** timeout queue.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_dequeue_td
 * Returned Value   : void
 * Comments         :
 *   This function removes a task from the timeout queue.  It does not
 * reschedule the task
 *
 *END*----------------------------------------------------------------------*/

void _time_dequeue_td
   (
      /* [IN] the address of the task descriptor to be removed */
      pointer td
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr = (TD_STRUCT_PTR)td;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_time_dequeue_td, td);

   _int_disable();
   _TIME_DEQUEUE(td_ptr, kernel_data);
   _int_enable();

   _KLOGX1(KLOG_time_dequeue_td);

} /* Endbody */

/* EOF */