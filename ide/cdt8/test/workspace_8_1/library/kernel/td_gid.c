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
*** File: td_gid.c
***
*** Comments:      
***   This file contains the function for returning the task id of 
*** the current task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_id
* Returned Value   : _mqx_uint
* Comments         :
*    Returns the current task's task id
* 
*END*----------------------------------------------------------------------*/

_task_id _task_get_id
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return( kernel_data->ACTIVE_PTR->TASK_ID );

} /* Endbody */

/* EOF */
