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
*** File: ti_deli.c
***
*** Comments:      
***   This file contains the function for delaying the specified task. It is
*** assumed that the timeout field in the TD has already been set by the API
*** functions.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "timer.h"
#include "tim_prv.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_delay_internal
 * Returned Value   : void
 * Comments         :
 *   This function puts a task on the timeout queue for the specified number
 * of ticks, or until removed by another task. Must be called int disabled.
 *
 *END*----------------------------------------------------------------------*/

void _time_delay_internal
   (
      /* [IN] the task to delay */
      register TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           td2_ptr;
   register TD_STRUCT_PTR           tdprev_ptr;
   register _mqx_uint               count;
   register _mqx_int                result;
   
   _GET_KERNEL_DATA(kernel_data);

   /* Remove task from ready to run queue */
   tdprev_ptr = (TD_STRUCT_PTR)((pointer)&kernel_data->TIMEOUT_QUEUE);
   if ( _QUEUE_GET_SIZE(&kernel_data->TIMEOUT_QUEUE) ) {

      /* Perform insertion sort by time */
      td2_ptr    = (TD_STRUCT_PTR)((pointer)kernel_data->TIMEOUT_QUEUE.NEXT);

      /* SPR P171-0023-01 use pre-decrement on while loop */
      count      = _QUEUE_GET_SIZE(&kernel_data->TIMEOUT_QUEUE) + 1;
      while ( --count ) {
      /* END SPR */
         result = PSP_CMP_TICKS(&td2_ptr->TIMEOUT, &td_ptr->TIMEOUT);
         if (MQX_DELAY_ENQUEUE_POLICY(result)) { /* CR171 */
            /* Enqueue before td2_ptr */
            break;
         } /* Endif */

         tdprev_ptr = td2_ptr;
         td2_ptr    = td2_ptr->TD_NEXT;
      } /* Endwhile */

   } /* Endif */

   /* Remove from ready queue */
   _QUEUE_UNLINK(td_ptr);

   /* Insert into timeout queue */
   _QUEUE_INSERT(&kernel_data->TIMEOUT_QUEUE,tdprev_ptr,td_ptr);

   td_ptr->STATE |= IS_ON_TIMEOUT_Q;

   _sched_execute_scheduler_internal();

} /* Endbody */

/* EOF */
