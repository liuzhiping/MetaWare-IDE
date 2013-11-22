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
*** File: ta_getx.c
***
*** Comments:      
***  This file contains the function for obtaining the exception handler
*** for a task.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_get_exception_handler
* Returned Value   : void _CODE_PTR_
* Comments         :
*
*  This function gets the task exception handler.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _task_get_exception_handler
   (
      /* [IN] the task id of the task whose exception handler is wanted */
      _task_id        task_id

   ))(_mqx_uint, pointer)
{ /* Body */
   TD_STRUCT_PTR           td_ptr;
   void        (_CODE_PTR_ excpt_function)(_mqx_uint, pointer);
   
   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if ( td_ptr == NULL ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      return( NULL );
   }/* Endif */

   excpt_function   = td_ptr->EXCEPTION_HANDLER_PTR;

   return(excpt_function);

} /* Endbody */

/* EOF */
