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
*** File: sc_numq.c
***
*** Comments:      
***   This file contains the functions for use with priority
***   manipulation of tasks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_get_max_priority_on_q_internal
* Returned Value   : _mqx_uint priority
* Comments         :
*   This function finds the maximum of the priorities of the tasks
* on the waiting queue.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _sched_get_max_priority_on_q_internal
   (
      /* [IN] the address of the queue header */
      register QUEUE_STRUCT_PTR queue_ptr
   )
{ /* Body */
   register TD_STRUCT_PTR td_ptr;
   register _mqx_uint      priority;
   register _mqx_uint      count;

   td_ptr   = (TD_STRUCT_PTR)((pointer)queue_ptr->NEXT);
   count    = _QUEUE_GET_SIZE(queue_ptr) + 1;
   priority = MAX_MQX_UINT;
   while (--count) {
      if (td_ptr->MY_QUEUE->PRIORITY < priority) {
         priority = td_ptr->MY_QUEUE->PRIORITY;
      } /* Endif */
      td_ptr = td_ptr->TD_NEXT;
   } /* Endwhile */
   return priority;

} /* Endbody */

/* EOF */
