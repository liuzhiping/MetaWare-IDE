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
*** File: ta_util2.c
***
*** Comments:      
***   This file contains more utility functions dealing with tasks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_index_from_id
* Returned Value   : task index (or 0 if not found)
* Comments         :
*
*END*----------------------------------------------------------------------*/

_mqx_uint _task_get_index_from_id
   (
      /* [IN] the task Id to look up */
      _task_id  taskid
   )
{ /* Body */
   TD_STRUCT_PTR           td_ptr;

   td_ptr = _task_get_td(taskid);

   if (td_ptr == NULL) {
      return 0;
   } /* Endif */

   return td_ptr->TASK_TEMPLATE_PTR->TASK_TEMPLATE_INDEX;

} /* Endbody */
