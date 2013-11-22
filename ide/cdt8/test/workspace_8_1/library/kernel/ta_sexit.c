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
*** File: ta_sexit.c
***
*** Comments:      
***   This file contains the function for setting the exit handler
*** for a task.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_set_exit_handler
* Returned Value   : void _CODE_PTR_
* Comments         :
*
*  This function sets the task exit handler to the specified function,
* and returns the old task exit handler.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _task_set_exit_handler
   (
      /* [IN] the task id of the task whose exit handler must be set */
      _task_id        task_id,

      /* [IN] the new exit handler for the task, to be called when it exits */
      void (_CODE_PTR_ exit_handler_address)(void)

   ))(void)
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   TD_STRUCT_PTR          td_ptr;
   void     (_CODE_PTR_   old_function)(void);
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_task_set_exit_handler, task_id, exit_handler_address);

   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if ( td_ptr == NULL ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      _KLOGX2(KLOG_task_set_exit_handler, NULL);
      return( NULL );
   }/* Endif */

   old_function = td_ptr->EXIT_HANDLER_PTR;
   td_ptr->EXIT_HANDLER_PTR = exit_handler_address;

   _KLOGX2(KLOG_task_set_exit_handler, old_function);
   return(old_function);

} /* Endbody */

/* EOF */
