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
*** File: ta_param.c
***
*** Comments:      
***   This file contains the functions for getting and setting the
*** task parameter.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_parameter
* Comments         :
*    This function returns the creation parameter of the active task
*
*END*----------------------------------------------------------------------*/

uint_32 _task_get_parameter
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   return _task_get_parameter_internal(kernel_data->ACTIVE_PTR);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_parameter_for
* Comments         :
*    This function returns the creation parameter of the given task
*
*END*----------------------------------------------------------------------*/

uint_32 _task_get_parameter_for
   (
      /* [IN] the task ID of the task to get the create parameter for */
      _task_id  tid
   )
{ /* Body */
   TD_STRUCT_PTR td_ptr;

   td_ptr = (TD_STRUCT_PTR)_task_get_td(tid);

   return _task_get_parameter_internal(td_ptr);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_get_parameter_internal
* Comments         :
*    This function returns the create parameter for the given task
*
*END*----------------------------------------------------------------------*/

uint_32 _task_get_parameter_internal
   (
      /* [IN] the task descriptor of the task to get the create parameter for */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   PSP_STACK_START_STRUCT_PTR stack_start_ptr;

   stack_start_ptr = _psp_get_stack_start(td_ptr);
   return stack_start_ptr->PARAMETER;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_set_parameter
* Comments         :
*    This function allows for the modification of the task parameter.
*    it returns the original value.
*
*END*----------------------------------------------------------------------*/

uint_32 _task_set_parameter
   (
      /* [IN] the value to set the task parameter to */
      uint_32 new_value
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   uint_32                    old_value;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_task_set_parameter, new_value);

   old_value = _task_set_parameter_internal(new_value, kernel_data->ACTIVE_PTR);

   _KLOGX2(KLOG_task_set_parameter, old_value);
   return old_value;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_set_parameter_for
* Comments         :
*    This function allows for the modification of the task parameter.
*    it returns the original value.
*
*END*----------------------------------------------------------------------*/

uint_32 _task_set_parameter_for
   (
      /* [IN] the value to set the task parameter to */
      uint_32   new_value,

      /* [IN] the task ID of the task to change */
      _task_id  tid
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   TD_STRUCT_PTR           td_ptr;
   uint_32                 old_value;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_task_set_parameter_for, tid, new_value);

   td_ptr = (TD_STRUCT_PTR)_task_get_td(tid);
#if MQX_CHECK_ERRORS
   if (td_ptr == NULL) {
      _KLOGX3(KLOG_task_set_parameter_for, tid, MQX_INVALID_PARAMETER);
      return 0;
   } /* Endif */
#endif
      
   old_value = _task_set_parameter_internal(new_value, td_ptr);

   _KLOGX2(KLOG_task_set_parameter_for, old_value);
   return old_value;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_set_parameter_internal
* Comments         :
*    This function changes the specified task's creation parameter. It will
*   return the old value.
*
*END*----------------------------------------------------------------------*/

uint_32 _task_set_parameter_internal
   (
      /* [IN] the value to set the task parameter to */
      uint_32       new_value,

      /* [IN] the task descriptor of the task to change */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   PSP_STACK_START_STRUCT_PTR stack_start_ptr;
   uint_32                    old_value;

   stack_start_ptr = _psp_get_stack_start(td_ptr);

   old_value = stack_start_ptr->PARAMETER;
   stack_start_ptr->PARAMETER = new_value;

   return old_value;
    
} /* Endbody */

/* EOF */
