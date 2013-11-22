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
*** File: ta_sync.c
***
*** Comments:      
***   This file contains the function for manipulating the tasks
*** context status register on the stack while the task is suspended.
***                                                               
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_sync_priority_internal
* Returned Value   : none
* Comments         :
*
*   This function is called when a quiescent task's priority level has
* changed, and the hardware disable/enable bits may need to be patched
* to the correct level.
*
*END*----------------------------------------------------------------------*/

void _task_sync_priority_internal
   (
      /* [IN] the task descriptor whose stack which must be modified */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   pointer stack_ptr = td_ptr->STACK_PTR;

   _PSP_SET_SR_OF_BLOCKED_TASK(stack_ptr, td_ptr->MY_QUEUE->ENABLE_SR);

} /* Endbody */

/* EOF */
