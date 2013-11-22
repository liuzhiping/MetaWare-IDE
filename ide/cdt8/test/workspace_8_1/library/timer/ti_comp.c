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
*** File: ti_comp.c
***
*** Comments:
***   This file contains the function for creating the timer
*** component.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "timer.h"
#include "tim_prv.h"

#if MQX_USE_TIMER
/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _timer_create_component
* Return Value    : _mqx_uint MQX_OK or error code
* Comments        : This is the function that installs the timer component
*   into the kernel
*
*END*------------------------------------------------------------------*/

_mqx_uint _timer_create_component
   (
      /* [IN] the task priority of the timer task */
      _mqx_uint timer_task_priority,
      
      /* [IN] the stack size for the timer task */
      _mqx_uint timer_task_stack_size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   TIMER_COMPONENT_STRUCT_PTR timer_component_ptr;
   TASK_TEMPLATE_STRUCT       timer_tt;

   _GET_KERNEL_DATA(kernel_data);
   
   _KLOGE2(KLOG_timer_create_component, timer_task_priority);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_timer_create_component, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif
      
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
                                               
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_TIMER] != NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_timer_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   /* Get the timer component data structure */
   timer_component_ptr = _mem_alloc_system_zero(
      (_mem_size)sizeof(TIMER_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (timer_component_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_timer_create_component, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif   

   _QUEUE_INIT(&timer_component_ptr->ELAPSED_TIMER_ENTRIES, 0);
   _QUEUE_INIT(&timer_component_ptr->KERNEL_TIMER_ENTRIES, 0);

   timer_tt.TASK_TEMPLATE_INDEX = 0;
   timer_tt.TASK_ADDRESS        = _timer_task;
   if (timer_task_stack_size == 0) {
      timer_tt.TASK_STACKSIZE   = TIMER_DEFAULT_STACK_SIZE;
   } else {
      timer_tt.TASK_STACKSIZE   = timer_task_stack_size;
   } /* Endif */
   if (timer_task_priority == 0) {
      timer_tt.TASK_PRIORITY    = TIMER_DEFAULT_TASK_PRIORITY;
   } else {
      timer_tt.TASK_PRIORITY    = timer_task_priority;
   } /* Endif */
   timer_tt.TASK_NAME           = "Timer Task";
   timer_tt.TASK_ATTRIBUTES     = 0;
   timer_tt.DEFAULT_TIME_SLICE  = 0;

   kernel_data->KERNEL_COMPONENTS[KERNEL_TIMER] = timer_component_ptr;

   timer_component_ptr->TIMER_TID = _task_create(0, 0, (uint_32)&timer_tt);
#if MQX_CHECK_ERRORS
   if (timer_component_ptr->TIMER_TID == MQX_NULL_TASK_ID) {
      _mqx_uint result;

      kernel_data->KERNEL_COMPONENTS[KERNEL_TIMER] = NULL;
      timer_component_ptr->VALID = 0;
      result = _task_get_error();
      if (result == MQX_OK) {
         result = MQX_OUT_OF_MEMORY;
      } /* Endif */
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _mem_free(timer_component_ptr);
      _KLOGX2(KLOG_timer_create_component, result);
      return(result);
   }/* Endif */
#endif

#if MQX_TASK_DESTRUCTION
/* Start CR 2326 */
   kernel_data->COMPONENT_CLEANUP[KERNEL_TIMER] = _timer_cleanup;
/* End CR 2326 */
/*   kernel_data->COMPONENT_CLEANUP[KERNEL_EVENTS] = _timer_cleanup; */
#endif
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

   _KLOGX2(KLOG_timer_create_component, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */