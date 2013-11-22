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
*** File: ta_util.c
***
*** Comments:      
***   This file contains utility functions dealing with tasks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_id_from_name
* Returned Value   : _task_id (or NULL_TASK_ID if not found)
* Comments         :
*    This function uses a task name (from its task template)
*    to find a task id.  Only the first task found with
*    the provided name is found.
*
*END*----------------------------------------------------------------------*/

_task_id _task_get_id_from_name
   (
      /* [IN] the name to look up */
      char_ptr name_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   TD_STRUCT_PTR           td_ptr;
   _mqx_uint                size;

   _GET_KERNEL_DATA(kernel_data);

   td_ptr = (TD_STRUCT_PTR)((uchar_ptr)kernel_data->TD_LIST.NEXT -
      FIELD_OFFSET(TD_STRUCT,TD_LIST_INFO));
   size   = _QUEUE_GET_SIZE(&kernel_data->TD_LIST);

   while (size && td_ptr) {
      if (strncmp( td_ptr->TASK_TEMPLATE_PTR->TASK_NAME, name_ptr, 
         MQX_MAX_TASK_NAME_SIZE) == 0) 
      {
         return td_ptr->TASK_ID;
      } /* Endif */
      size--;
      td_ptr = (TD_STRUCT_PTR)((uchar_ptr)(td_ptr->TD_LIST_INFO.NEXT) -
         FIELD_OFFSET(TD_STRUCT,TD_LIST_INFO));
   } /* Endwhile */

   return MQX_NULL_TASK_ID;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_template_ptr
* Returned Value   : TASK_TEMPLATE_STRUCT_PTR pointer to a task's template
* Comments         :
*    This function obtains a task template from the task id provided
*
*END*----------------------------------------------------------------------*/

TASK_TEMPLATE_STRUCT_PTR  _task_get_template_ptr
   (
      /* [IN] the task id */
      _task_id  task_id
   )
{ /* Body */
   TD_STRUCT_PTR  td_ptr;

   td_ptr = _task_get_td(task_id);

   return td_ptr->TASK_TEMPLATE_PTR;

} /* Endwhile */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_template_index
* Returned Value   : _mqx_uint template number (0 if not found)
* Comments         :
*    This function obtains a task template index (from its task template)
*    using the string name of the task provided
*
*END*----------------------------------------------------------------------*/

_mqx_uint  _task_get_template_index
   (
      /* [IN] the name to look up */
      char_ptr name_ptr 
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   TASK_TEMPLATE_STRUCT_PTR task_template_ptr;

   _GET_KERNEL_DATA(kernel_data);

   /** Search all the local task templates for one whose index matches */
   task_template_ptr = kernel_data->TASK_TEMPLATE_LIST_PTR;
   while (task_template_ptr->TASK_TEMPLATE_INDEX){
      if (task_template_ptr->TASK_NAME != NULL)  {
         if (strncmp( task_template_ptr->TASK_NAME, name_ptr, 
            MQX_MAX_TASK_NAME_SIZE) == 0) 
         {
            break;
         } /* Endif */
      } /* Endif */
      ++task_template_ptr;
   } /* Endwhile */

   return task_template_ptr->TASK_TEMPLATE_INDEX;

} /* Endwhile */

/* EOF */
