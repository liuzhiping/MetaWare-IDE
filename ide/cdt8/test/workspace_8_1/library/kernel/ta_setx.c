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
*** File: ta_setx.c
***
*** Comments:      
***   This file contains the function for setting the exception handler
*** for a task.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_set_exception_handler
* Returned Value   : void _CODE_PTR_
* Comments         :
*
*  This function sets the task exception handler to the specified function,
* and returns the old task exit handler.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _task_set_exception_handler
   ( 
      /* [IN] the task id of the task whose exception handler is to be set */
      _task_id        task_id,
   
      /* [IN] the function to call when the task gets an exception */
      void (_CODE_PTR_ handler_address)(_mqx_uint, pointer)

   ))(_mqx_uint, pointer)
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   TD_STRUCT_PTR          td_ptr;
   void     (_CODE_PTR_   old_function)(_mqx_uint, pointer);
   
   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_task_set_exception_handler, task_id, handler_address);
   
   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if ( td_ptr == NULL ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      _KLOGX2(KLOG_task_set_exception_handler, NULL);
      return( NULL );
   } /* Endif */
   
   old_function = td_ptr->EXCEPTION_HANDLER_PTR;
   td_ptr->EXCEPTION_HANDLER_PTR = handler_address;

   _KLOGX2(KLOG_task_set_exception_handler, old_function);
   return(old_function);

} /* Endbody */

/* EOF */
