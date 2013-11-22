/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2002 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: ta_creas.c
***
*** Comments:
***   This file contains the function for creating a task with the
*** stack location specified.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/* 
** This variable has no use to MQX. 
** Its been created for Task Aware Debug module.
*/
volatile uint_32 _tad_task_at_flag;

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _task_create_at
* Returned Value   : task ID of the created task or NULL on error
* Comments         :
*    This function will create a new task of the type specified by the
*    task template number, puting the TD and Stack at the specified location.
*
*END*----------------------------------------------------------------------*/

_task_id _task_create_at
   (
      /* [IN] the processor upon which to create the task */
      _processor_number processor_number,

      /* [IN] the task template index number for this task */
      _mqx_uint         template_index,

      /* [IN] the parameter to pass to the newly created task */
      uint_32           parameter,

      /* [IN] the location where the stack and TD are to be created */
      pointer           stack_ptr,

      /* [IN] the size of the stack */
      _mem_size         stack_size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   TD_STRUCT_PTR            td_ptr;
   _task_id                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE5(KLOG_task_create_at, processor_number, template_index, parameter,
      stack_ptr);

#if MQX_CHECK_ERRORS
   if (template_index & SYSTEM_TASK_FLAG) {
      _task_set_error(MQX_INVALID_TEMPLATE_INDEX);
      _KLOGX3(KLOG_task_create_at, MQX_NULL_TASK_ID,
         MQX_INVALID_TEMPLATE_INDEX);
      return MQX_NULL_TASK_ID;
   } /* Endif */
#endif

   if (processor_number == 0 ) {
      processor_number = (_processor_number)kernel_data->PROCESSOR_NUMBER;
#if MQX_CHECK_ERRORS
   } else {
      _task_set_error(MQX_INVALID_PROCESSOR_NUMBER);
      _KLOGX3(KLOG_task_create_at, MQX_NULL_TASK_ID,
         MQX_INVALID_PROCESSOR_NUMBER);
      return MQX_NULL_TASK_ID;
#endif
   } /* Endif */

   td_ptr = _task_build_internal(template_index, parameter, stack_ptr,
      stack_size);
   if (td_ptr != NULL) {
      result = td_ptr->TASK_ID;
      _INT_DISABLE();
      _task_ready_internal(td_ptr);
      _INT_ENABLE();
      _CHECK_RUN_SCHEDULER(); /* Let a higher priority task run */
   } else {
      result = MQX_NULL_TASK_ID;
   }/* Endif */

   _KLOGX3(KLOG_task_create_at, result,
      kernel_data->ACTIVE_PTR->TASK_ERROR_CODE);

   _tad_task_at_flag++;
   
   return(result);

} /* Endbody */

/* EOF */
