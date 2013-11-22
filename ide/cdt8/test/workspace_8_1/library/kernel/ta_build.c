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
*** File: ta_build.c
***
*** Comments:      
***   This file contains the function for creating a task, but not letting
*** it run.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_build_internal
* Returned Value   : TD_STRUCT_PTR the td for the created task.
* Comments         :
*    This function will create a new task of the type specified by the
*    task template number, but not make it ready to run.
*
*END*----------------------------------------------------------------------*/

TD_STRUCT_PTR _task_build_internal
   (  
      /* [IN] the task template index number for this task */
      _mqx_uint     template_index,
      
/* START CR 897 */
      /* [IN] the parameter to pass to the newly created task */
      uint_32       parameter,

      /* [IN] if not NULL, the location of the stack is provided */
      pointer       stack_ptr,

      /* [IN] the stack size if provided by the application */
      _mqx_uint     stack_size
/* END CR 897 */
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   TD_STRUCT_PTR            td_ptr;
   TASK_TEMPLATE_STRUCT_PTR task_template_ptr;
   boolean                  dynamic;
   uint_32                  create_parameter;
   
   _GET_KERNEL_DATA(kernel_data);

   if ( template_index ) {
      dynamic = FALSE;
      create_parameter = parameter;
         
      /* Search all the local task templates for one whose index matches */
      task_template_ptr = kernel_data->TASK_TEMPLATE_LIST_PTR;
      while ( task_template_ptr->TASK_TEMPLATE_INDEX &&
         (task_template_ptr->TASK_TEMPLATE_INDEX != template_index) )
      {
         ++task_template_ptr;
      } /* Endwhile */
      if (task_template_ptr->TASK_TEMPLATE_INDEX == 0) {
         /* Task not found */
         task_template_ptr = NULL;
      } /* Endif */
   } else {
      task_template_ptr = (TASK_TEMPLATE_STRUCT_PTR)parameter;
      create_parameter  = task_template_ptr->CREATION_PARAMETER;
      dynamic = TRUE;
   } /* Endif */

#if MQX_CHECK_ERRORS
   if (task_template_ptr == NULL) {
      _task_set_error(MQX_NO_TASK_TEMPLATE);
      return NULL;
   } /* Endif */
#endif   

   /* serialize task creation/destruction */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->TASK_CREATE_LWSEM);

   /* Create the task, but do not ready it */
/* START CR 897 */
   td_ptr = _task_init_internal(task_template_ptr, 
      kernel_data->ACTIVE_PTR->TASK_ID, create_parameter, dynamic, stack_ptr,
      stack_size);
/* END CR 897 */

   /* Allow other tasks to create */
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->TASK_CREATE_LWSEM);

   return(td_ptr);
      
} /* Endbody */

/* EOF */
