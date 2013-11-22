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
*** File: ta_exit.c
***
*** Comments:      
***   This file contains the function called when a task exits.
*** This may be caused by a call to _task_abort, or by returning
*** from the root function of the task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_exit_function_internal
* Returned Value   : void
* Comments         : 
*    This function executes the exit handler for the current task,
* then destroys the current task
*
*END*----------------------------------------------------------------------*/

/* Dummy function so debuggers will display stack correctly */
extern void _task_exiting_function_internal(void);  /* Dummy prototype */
void _task_exiting_function_internal(void){}

void _task_exit_function_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
#if MQX_TASK_DESTRUCTION
   TD_STRUCT_PTR          td_ptr;
#endif

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_task_exit_function, kernel_data->ACTIVE_PTR->TASK_ID);

   _int_disable();
#if MQX_TASK_DESTRUCTION
   td_ptr = kernel_data->ACTIVE_PTR;
   if (td_ptr->EXIT_HANDLER_PTR != NULL) {
      (*td_ptr->EXIT_HANDLER_PTR)();
   }/* Endif */
   (void)_task_destroy(MQX_NULL_TASK_ID); /* Never returns */
#else
   kernel_data->ACTIVE_PTR->STATE = DYING;
   while(TRUE) {
      _task_block();
   } /* Endwhile */
#endif

} /* Endbody */

/* EOF */
