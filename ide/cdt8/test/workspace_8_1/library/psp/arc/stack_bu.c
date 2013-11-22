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
*** File: stack_bu.c
***
*** Comments:      
***   This file contains the functions for manipulating the user
*** context on the stack.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/* Start CR 818 */
#ifdef PSP_DSP_PRESENT
extern void      _psp_build_dsp_context(TD_STRUCT_PTR);
#endif
/* End   CR 818 */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_build_stack_frame
* Returned Value   : none
* Comments         :
*
*   This function sets up the stack frame of a new task descriptor.
*
*END*----------------------------------------------------------------------*/

void _psp_build_stack_frame
   (
      /* [IN] the address of the task descriptor */
      TD_STRUCT_PTR    td_ptr,

      /* [IN] the address of the stack memory block */
      pointer          stack_ptr,

      /* [IN] the size of the stack */
      uint_32          stack_size,

      /* [IN] the task template address */
      TASK_TEMPLATE_STRUCT_PTR template_ptr,

      /* [IN] the status register to use in creating the task */
      uint_32          status_register,

      /* [IN] the task creation parameter */
      uint_32          create_parameter
   )
{ /* Body */
   uchar_ptr                  stack_base_ptr;
   PSP_STACK_START_STRUCT_PTR stack_start_ptr;

   stack_base_ptr  = (uchar_ptr)_GET_STACK_BASE(stack_ptr, stack_size);
   stack_start_ptr = (pointer)(stack_base_ptr - sizeof(PSP_STACK_START_STRUCT));

   td_ptr->STACK_BASE  = (pointer)stack_base_ptr;
   td_ptr->STACK_LIMIT = _GET_STACK_LIMIT(stack_ptr, stack_size);
   td_ptr->STACK_PTR   = stack_start_ptr;
   td_ptr->FLAGS      |= PSP_SCRATCH_REGISTERS_SAVED;

   /*
   ** Build the task's initial stack frame. This contains the initialized
   ** registers, and an exception frame which will cause the task to 
   ** "return" to the start of the task when it is dispatched.
   */
   _mem_zero(stack_start_ptr, (uint_32)sizeof(PSP_STACK_START_STRUCT));

   stack_start_ptr->INITIAL_CONTEXT.FLAGS = status_register;
   stack_start_ptr->INITIAL_CONTEXT.RETURN_ADDRESS = 
      (uint_32)template_ptr->TASK_ADDRESS;

   stack_start_ptr->INITIAL_CONTEXT.R0 = create_parameter;
   stack_start_ptr->PARAMETER          = create_parameter;
   stack_start_ptr->INITIAL_CONTEXT.R26 = _core_read(26);

   /* Mark the bottom of the stack for debuggers*/
   stack_start_ptr->INITIAL_CONTEXT.R27 = 
      (uint_32)&stack_start_ptr->BACK_TRACE;
   stack_start_ptr->INITIAL_CONTEXT.BLINK = 
      (uint_32)_task_exit_function_internal;

/* Start CR 818 */
#ifdef PSP_DSP_PRESENT
   if (td_ptr->FLAGS & MQX_DSP_TASK) {
      _psp_build_dsp_context(td_ptr);
   } /* Endif */
#endif
/* End   CR 818 */

} /* Endbody */


/* Start CR 818 */
#ifdef PSP_DSP_PRESENT
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_build_dsp_context
* Returned Value   : none
* Comments         :
*
*   This function sets up the DSP context of a new task descriptor.
*
*END*----------------------------------------------------------------------*/

void _psp_build_dsp_context
   (
      /* [IN] the address of the task descriptor */
      TD_STRUCT_PTR    td_ptr
   )
{ /* Body */
   PSP_DSP_EXT_REGISTERS_STRUCT_PTR dsp_ptr;

   /* Allocate space for saving/restoring the DSP registers */
   dsp_ptr = (PSP_DSP_EXT_REGISTERS_STRUCT_PTR)_mem_alloc_system_zero(
      (_mem_size)sizeof(PSP_DSP_EXT_REGISTERS_STRUCT));

#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (!dsp_ptr) {
      /* Couldn't allocate memory for the DSP register context */
      _task_set_error_td_internal(td_ptr, MQX_OUT_OF_MEMORY);
      return;
   } /* Endif */
#endif

   /* 
   ** Transfer the block to the task being created. This will ensure the DSP context
   ** will be freed if the task is destroyed
   */
   _mem_transfer_internal((pointer)dsp_ptr, td_ptr);

#if MQX_CHECK_ERRORS
   /* This field should never be overwitten */
   dsp_ptr->TID = td_ptr->TASK_ID;
#endif
   
   td_ptr->DSP_CONTEXT_PTR = (pointer)dsp_ptr;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_get_dsp_context
* Returned Value   : pointer to task's DSP context
* Comments         :
*
*   This function returns the DSP context of a the specified task.
*
*END*----------------------------------------------------------------------*/

pointer _psp_get_dsp_context
   (
      /* [IN] the address of the task descriptor */
      pointer    in_td_ptr
   )
{ /* Body */
   TD_STRUCT_PTR                    td_ptr;

#if MQX_CHECK_ERRORS
   /* This field should never be overwitten */
   if (!in_td_ptr) {
      return NULL;
   } /* Endif */
#endif

   td_ptr = (TD_STRUCT_PTR)in_td_ptr;

   return td_ptr->DSP_CONTEXT_PTR;

} /* Endbody */
#endif
/* End   CR 818 */

/* EOF */
