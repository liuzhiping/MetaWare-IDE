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
*** File: ta_gexit.c
***
*** Comments:      
***   This file contains the function for obtaining the exit handler
*** for a task.
***                                                               
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_get_exit_handler
* Returned Value   : void _CODE_PTR_
* Comments         :
*
*  This function gets the task exit handler.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _task_get_exit_handler
   (
      /* [IN] the task id for the task whose exit handler is desired */
      _task_id        task_id
   ))(void)
{ /* Body */
   TD_STRUCT_PTR          td_ptr;
   void     (_CODE_PTR_   exit_function)(void);
   
   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if ( td_ptr == NULL ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      return( NULL );
   }/* Endif */

   exit_function = td_ptr->EXIT_HANDLER_PTR;

   return(exit_function);

} /* Endbody */

/* EOF */
