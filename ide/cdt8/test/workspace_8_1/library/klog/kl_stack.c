/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: kl_stack.c
***
*** Comments:
***   This file contains the functions for functions that
***   will determine current stack utilization of tasks.
***   NOTE:
***   MQX must be compiled with MQX_MONITOR_STACK set
***   in mqx_cnfg.h
***
*** $Header:kl_stack.c, 8, 9/29/2005 1:11:11 PM, Goutham D. R.$
***
*** $NoKeywords$
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _klog_get_task_stack_usage
* Returned Value  : MQX_OK or error code.
* Comments        : This function returns the stack size,
*    and the total amount of the stack used.
*
*END*------------------------------------------------------------------*/

_mqx_uint  _klog_get_task_stack_usage
   (
      /* [IN] task id of task to display */
      _task_id       task_id,
      
      /* [OUT] address where size of stack to be written to */
      _mem_size_ptr  stack_size_ptr,
      
      /* [OUT] amount of stack used */
      _mem_size_ptr  stack_used_ptr
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

#if MQX_MONITOR_STACK
   TD_STRUCT_PTR   td_ptr;

   td_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);
#if MQX_CHECK_ERRORS
   if (td_ptr == NULL)  {
      return(MQX_INVALID_TASK_ID);
   } /* Endif */
#endif
   
   return _klog_get_task_stack_usage_internal(td_ptr, stack_size_ptr, stack_used_ptr);

#else
   return MQX_INVALID_CONFIGURATION;
#endif
   
} /* Endbody */


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _klog_get_task_stack_usage_internal
* Returned Value  : MQX_OK or error code.
* Comments        : This function returns the stack size,
*    and the total amount of the stack used. Used by MQX functions that
* already have TD pointer.
*
*END*------------------------------------------------------------------*/

_mqx_uint  _klog_get_task_stack_usage_internal
   (
      /* [IN] td of task to display */
      TD_STRUCT_PTR  td_ptr,
      
      /* [OUT] address where size of stack to be written to */
      _mem_size_ptr  stack_size_ptr,
      
      /* [OUT] amount of stack used */
      _mem_size_ptr  stack_used_ptr
   )
{ /* Body */
#if MQX_MONITOR_STACK
   _mqx_uint_ptr   stack_ptr;
   _mqx_uint_ptr   stack_base;

#if MQX_CHECK_ERRORS
   if (td_ptr == NULL)  {
      return(MQX_INVALID_TASK_ID);
   } /* Endif */
#endif
   
#if PSP_STACK_GROWS_TO_LOWER_MEM
   stack_base = (_mqx_uint_ptr)td_ptr->STACK_BASE;
   stack_ptr  = (_mqx_uint_ptr)td_ptr->STACK_LIMIT;
   /* CR1183: stack_ptr++; */
   while (stack_ptr < stack_base) {
      if (*stack_ptr != MQX_STACK_MONITOR_VALUE) {
         break;
      } /* Endif */
      ++stack_ptr;
   } /* Endwhile */
   *stack_used_ptr = (_mqx_uint)((uchar_ptr)td_ptr->STACK_BASE - 
      (uchar_ptr)stack_ptr);
   *stack_size_ptr = (_mqx_uint)((uchar_ptr)td_ptr->STACK_BASE -
      (uchar_ptr)td_ptr->STACK_LIMIT);
#else
   stack_base = (pointer)td_ptr->STACK_BASE;
   stack_ptr  = (pointer)td_ptr->STACK_LIMIT;
   stack_ptr--;
   while (stack_ptr > stack_base) {
      if (*stack_ptr != MQX_STACK_MONITOR_VALUE) {
         break;
      } /* Endif */
      --stack_ptr;
   } /* Endwhile */
   *stack_used_ptr = (_mem_size)((uchar_ptr)stack_ptr - 
      (uchar_ptr)td_ptr->STACK_BASE);
   *stack_size_ptr = (_mem_size)((uchar_ptr)td_ptr->STACK_LIMIT - 
      (uchar_ptr)td_ptr->STACK_BASE);
#endif
   return(MQX_OK);
#else
   return MQX_INVALID_CONFIGURATION;
#endif
   
} /* Endbody */


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _klog_get_interrupt_stack_usage
* Returned Value  : stack size, and amount used.
* Comments        : This function returns the stack boundary,
*   and the total amount of the stack used.
*
*END*------------------------------------------------------------------*/

_mqx_uint  _klog_get_interrupt_stack_usage
   (
      /* [OUT] address where size of stack to be written to */
      _mem_size_ptr      stack_size_ptr,
      
      /* [OUT] amount of stack used */
      _mem_size_ptr      stack_used_ptr
   )
{ /* Body */
#if MQX_MONITOR_STACK
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _mqx_uint_ptr          stack_ptr;
   _mqx_uint_ptr          stack_base;

   _GET_KERNEL_DATA(kernel_data);
   
   *stack_size_ptr = kernel_data->INIT.INTERRUPT_STACK_SIZE;

#if PSP_STACK_GROWS_TO_LOWER_MEM
   stack_base = (_mqx_uint_ptr)kernel_data->INTERRUPT_STACK_PTR;
   stack_ptr  = (_mqx_uint_ptr)((uchar_ptr)stack_base -
      kernel_data->INIT.INTERRUPT_STACK_SIZE
#if PSP_MEMORY_ALIGNMENT
      + PSP_MEMORY_ALIGNMENT + 1
#endif
      );
   /* CR1183: stack_ptr++; */
   while (stack_ptr < stack_base) {
      if (*stack_ptr != MQX_STACK_MONITOR_VALUE) {
         break;
      } /* Endif */
      ++stack_ptr;
   } /* Endwhile */
   *stack_used_ptr = (_mem_size)((uchar_ptr)stack_base - (uchar_ptr)stack_ptr);
#else
   stack_base = (_mqx_uint_ptr)kernel_data->INTERRUPT_STACK_PTR;
   stack_ptr  = (_mqx_uint_ptr)((uchar_ptr)stack_base +
      kernel_data->INIT.INTERRUPT_STACK_SIZE - 1
#if PSP_MEMORY_ALIGNMENT
      - PSP_MEMORY_ALIGNMENT
#endif
      );
   stack_ptr--;
   while (stack_ptr > stack_base) {
      if (*stack_ptr != MQX_STACK_MONITOR_VALUE) {
         break;
      } /* Endif */
      --stack_ptr;
   } /* Endwhile */
   *stack_used_ptr = (_mem_size)((uchar_ptr)stack_ptr - (uchar_ptr)stack_base);
#endif

   return(MQX_OK);
#else
   return MQX_INVALID_CONFIGURATION;
#endif


/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */

/* EOF */