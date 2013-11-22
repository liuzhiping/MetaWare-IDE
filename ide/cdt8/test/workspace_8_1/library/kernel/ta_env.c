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
*** File: ta_env.c
***
*** Comments:      
***   This file contains the functions for accessing the task environment.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_set_environment
* Returned Value   : pointer
* Comments         :
*   The function sets the address of the environment data for this task
*
*END*-------------------------------------------------------------------------*/

pointer _task_set_environment
   ( 
     /* [IN] the id of the task whose environment is to be set */
     _task_id  task_id, 
     
     /* [IN] the value to set the task environment to */
     pointer   environment_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR      kernel_data;)
   TD_STRUCT_PTR               td_ptr;
   pointer                     old_env_ptr;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_task_set_environment, task_id, environment_ptr);

   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if (td_ptr == NULL) {
      _task_set_error(MQX_INVALID_TASK_ID);
      _KLOGX2(KLOG_task_set_environment, NULL);
      return( NULL );
   }/* Endif */

   old_env_ptr = td_ptr->ENVIRONMENT_PTR;
   td_ptr->ENVIRONMENT_PTR = environment_ptr;

   _KLOGX2(KLOG_task_set_environment, old_env_ptr);
   return(old_env_ptr);
   
} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _task_get_environment
* Returned Value   : pointer
* Comments         :
*   The function returns the address of the environment data for this task
*
*END*-------------------------------------------------------------------------*/

pointer _task_get_environment
   (
     /* [IN] the id of the task whose environment is to be returned */
      _task_id  task_id
   )
{ /* Body */
   TD_STRUCT_PTR          td_ptr;

   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
   if ( td_ptr == NULL ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      return( NULL );
   }/* Endif */

   return(td_ptr->ENVIRONMENT_PTR);
   
} /* Endbody */

/* EOF */
