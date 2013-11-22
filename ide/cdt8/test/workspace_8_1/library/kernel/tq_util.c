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
*** File: tq_util.c
***
*** Comments:      
***   This file contains the function for returning the number
*** of tasks on a task queue.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _taskq_get_value
* Returned Value   : size of task queue, or MAX_MQX_UINT on error
* Comments         :
*   This function returns the size of the task queue.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _taskq_get_value
   (
      /* [IN] the task queue handle */
      pointer users_task_queue_ptr
   )
{ /* Body */
   register TASK_QUEUE_STRUCT_PTR  task_queue_ptr = 
      (TASK_QUEUE_STRUCT_PTR)users_task_queue_ptr;

#if MQX_CHECK_ERRORS
   if (task_queue_ptr == NULL)  {
      _task_set_error(MQX_INVALID_PARAMETER);
      return(MAX_MQX_UINT);
   } /* Endif */
#endif

#if MQX_CHECK_VALIDITY   
   if (task_queue_ptr->VALID != TASK_QUEUE_VALID){
      _task_set_error(MQX_INVALID_TASK_QUEUE);
      return(MAX_MQX_UINT);
   } /* Endif */
#endif

   return(task_queue_ptr->TD_QUEUE.SIZE);
   
} /* Endbody */

/* EOF */
