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
*** File: td_sysid.c
***
*** Comments:      
***   This file contains the function for returning the ID of the MQX
*** system task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_system_task_id
* Returned Value   : _task_id
* Comments         : 
*    This function returns the task id of the system task.
*
*END*----------------------------------------------------------------------*/

_task_id _mqx_get_system_task_id
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return(kernel_data->SYSTEM_TD.TASK_ID);

} /* Endbody */

/* EOF */
