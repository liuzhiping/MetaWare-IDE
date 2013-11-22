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
*** File: sc_ipq.c
***
*** Comments:      
***   This file contains the function for inserting at task descriptor
*** into a queue, by task priority.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _sched_insert_priorityq_internal
* Returned Value   : none
* Comments         :
*   This function inserts a task descriptor into a task descriptor queue
* by order of task priority.
*
*END*----------------------------------------------------------------------*/

void _sched_insert_priorityq_internal
   (
      /* [IN] the address of the queue header */
      register QUEUE_STRUCT_PTR queue_ptr,

      /* [IN] the address of the task descriptor to insert */
      register TD_STRUCT_PTR    td_ptr
   )
{ /* Body */
   register TD_STRUCT_PTR td2_ptr;
   register TD_STRUCT_PTR td_prev_ptr;
   register _mqx_uint      priority;
   register _mqx_uint      count;

   td_prev_ptr = (TD_STRUCT_PTR)((pointer)queue_ptr);
   td2_ptr     = (TD_STRUCT_PTR)((pointer)queue_ptr->NEXT);
   count       = _QUEUE_GET_SIZE(queue_ptr) + 1;
   priority    = td_ptr->MY_QUEUE->PRIORITY;
   while (--count) {
      if (td2_ptr->MY_QUEUE->PRIORITY > priority) {
         break;
      } /* Endif */
      td_prev_ptr = td2_ptr;
      td2_ptr     = td2_ptr->TD_NEXT;
   } /* Endwhile */
   _QUEUE_INSERT(queue_ptr,
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)td_prev_ptr),
      (QUEUE_ELEMENT_STRUCT_PTR)((pointer)td_ptr));

} /* Endbody */

/* EOF */
