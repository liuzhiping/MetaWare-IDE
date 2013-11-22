/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ta_rest.c
***
*** Comments:      
***   This file contains the function for restarting a task.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _task_restart
* Returned Value   : _mqx_uint error code
* Comments         : 
*   Restart the task specified by the given task-id (the victim).  
*   All of the victim's resources are released, specifically,
*   all queues closed, and all memory is freed.
*   Component cleanup functions are called to free any component
*   resources owned by this task.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _task_restart
   (
      /* [IN] the task id of the task to restart */
      _task_id    task_id,

      /* [IN] pointer to new task parameter if not NULL */
      uint_32_ptr param_ptr,

      /* 
      ** [IN] whether the task should be restarted in the
      ** blocked state or not
      */
      boolean     blocked

   )
{ /* Body */
   PSP_STACK_START_STRUCT_PTR  stack_start_ptr;
   KERNEL_DATA_STRUCT_PTR      kernel_data;
   TASK_TEMPLATE_STRUCT_PTR    template_ptr;
   TD_STRUCT_PTR               victim_ptr;
   TD_STRUCT_PTR               td_ptr;
   READY_Q_STRUCT_PTR          ready_q_ptr;
   char_ptr                    stack_ptr;
   pointer                     block_ptr;
   uint_32                     create_param;
   _processor_number           processor;
   _mem_size                   stack_size;
#if MQX_COMPONENT_DESTRUCTION
   _mqx_uint                   i;
#endif

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_task_restart, task_id);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      /* Cannot be called from an ISR */
      _KLOGX2(KLOG_task_restart, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

/* Start CR 1902 */   
   if (task_id != MQX_NULL_TASK_ID) {
      processor = PROC_NUMBER_FROM_TASKID(task_id);
      if (processor != (_processor_number)kernel_data->PROCESSOR_NUMBER ) {
#if MQX_IS_MULTI_PROCESSOR
         if ( kernel_data->IPC != NULL ) {
            _KLOGX2(KLOG_task_restart, MQX_OK);
            return( (*kernel_data->IPC)(FALSE, processor,
               KERNEL_MESSAGES, IPC_TASK_RESTART, 3, (_mqx_uint)task_id,
               (_mqx_uint)param_ptr, (_mqx_uint)blocked));
         } else {
#endif
            _KLOGX2(KLOG_task_restart, MQX_INVALID_TASK_ID);
            return(MQX_INVALID_TASK_ID);
#if MQX_IS_MULTI_PROCESSOR
         } /* Endif */
#endif
      }/* Endif */
   }/* Endif */
   
   victim_ptr = (TD_STRUCT_PTR)_task_get_td(task_id);

#if MQX_CHECK_ERRORS
   if (victim_ptr == NULL) {
      _KLOGX2(KLOG_task_restart, MQX_INVALID_TASK_ID);
      return(MQX_INVALID_TASK_ID);
   } /* Endif */

   if (victim_ptr == SYSTEM_TD_PTR(kernel_data)) {
      _KLOGX2(KLOG_task_restart, MQX_INVALID_TASK_ID);
      return(MQX_INVALID_TASK_ID);
   } /* Endif */
#endif
/* End CR 1902 */

   /* First, serialize task creation/destruction/restart */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->TASK_CREATE_LWSEM);

   _int_disable();
   /* remove the victim from any queues it may be in. */
   if (victim_ptr->STATE == READY) {
      if (victim_ptr != kernel_data->ACTIVE_PTR) {
         /* Remove other task from ready queue */
         _QUEUE_UNLINK(victim_ptr);
      } /* Endif */
   } else if (victim_ptr->STATE & IS_ON_TIMEOUT_Q) {
      /* Remove from time queue */
      _TIME_DEQUEUE(victim_ptr, kernel_data);
   } else if (victim_ptr->STATE & TD_IS_ON_QUEUE) {
      /* Remove from queue, where suspended 
      ** eg, MUTEX_BLOCKED, IO_BLOCKED, TASKQ_BLOCKED
      */
      _QUEUE_REMOVE(victim_ptr->INFO, victim_ptr);
   } /* Endif */
               
   if (victim_ptr->STATE & TD_IS_ON_AUX_QUEUE) {
      /* 
      ** Remove from queue, where suspended 
      ** eg, LWSEM_BLOCKED
      ** (uses AUX_QUEUE field)
      */
      _QUEUE_REMOVE(victim_ptr->INFO, &victim_ptr->AUX_QUEUE);
   } /* Endif */

   victim_ptr->STATE   = DYING;

   /* Stop Floating point context monitoring */
   if (kernel_data->FP_ACTIVE_PTR == victim_ptr) {
      kernel_data->FP_ACTIVE_PTR = NULL;
   }/* Endif */

   _int_enable();

#if MQX_COMPONENT_DESTRUCTION
   for (i = 0; i < MAX_KERNEL_COMPONENTS; ++i) {
      if (kernel_data->COMPONENT_CLEANUP[i] != NULL) {
         (*kernel_data->COMPONENT_CLEANUP[i])(victim_ptr);
      } /* Endif */
   } /* Endfor */

   /* Call I/O component cleanup functions */      
   for (i = 0; i < MAX_IO_COMPONENTS; ++i) {
      if (kernel_data->IO_COMPONENT_CLEANUP[i] != NULL) {
         (*kernel_data->IO_COMPONENT_CLEANUP[i])(victim_ptr);
      } /* Endif */
   } /* Endfor */
#endif

   td_ptr = kernel_data->ACTIVE_PTR;
   while (victim_ptr->MEMORY_RESOURCE_LIST != victim_ptr) {
      block_ptr = victim_ptr->MEMORY_RESOURCE_LIST;
      if (td_ptr != victim_ptr) {
         _mem_transfer_td_internal(block_ptr, victim_ptr, td_ptr);
      } /* Endif */
      _mem_free(block_ptr);
   } /* Endwhile */
               
   /* Free up create/destroy/restart for other tasks */
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->TASK_CREATE_LWSEM);

   template_ptr    = victim_ptr->TASK_TEMPLATE_PTR;
   stack_start_ptr = _psp_get_stack_start(victim_ptr);
   if (param_ptr == NULL) {
      create_param = stack_start_ptr->PARAMETER;
   } else {
      create_param = *param_ptr;
   } /* Endif */
   stack_ptr       = (char_ptr)victim_ptr + sizeof(TD_STRUCT);

   stack_size = template_ptr->TASK_STACKSIZE;
   if ( stack_size <= PSP_MINSTACKSIZE  ) {
      stack_size = PSP_MINSTACKSIZE; 
   } /* Endif */

   /* Make the size a multiple of the memory alignment */   
   _MEMORY_ALIGN_VAL_LARGER(stack_size);  

/* Start CR 2355 */
#if PSP_MEMORY_ALIGNMENT
   /* But we need to add size to allow for alignment of stack base */
   stack_size += PSP_STACK_ALIGNMENT + 1;
#endif
/* End CR 2355 */

   ready_q_ptr = kernel_data->READY_Q_LIST - template_ptr->TASK_PRIORITY;
   victim_ptr->HOME_QUEUE  = victim_ptr->MY_QUEUE = ready_q_ptr;
   victim_ptr->TASK_SR     = ready_q_ptr->ENABLE_SR;
   victim_ptr->FLAGS       = template_ptr->TASK_ATTRIBUTES;

#if MQX_HAS_TIME_SLICE
   /* Reset the time slice back to the default */
   if (template_ptr->DEFAULT_TIME_SLICE) {
#if (MQX_DEFAULT_TIME_SLICE_IN_TICKS == 0)
/* START CR 301 */
      uint_32 ticks;
#if 0      
      TIME_STRUCT              time;

      time.MILLISECONDS = template_ptr->DEFAULT_TIME_SLICE;
      time.SECONDS      = 0;
      PSP_TIME_TO_TICKS(&time, &victim_ptr->TIME_SLICE);
#endif
      ticks = ((template_ptr->DEFAULT_TIME_SLICE * 2 *
         kernel_data->TICKS_PER_SECOND) / 1000) / 2  /* Rounding.. */;
      PSP_ADD_TICKS_TO_TICK_STRUCT(&td_ptr->TIME_SLICE, 
         ticks, &td_ptr->TIME_SLICE);
/* END CR 301 */
#else
      PSP_ADD_TICKS_TO_TICK_STRUCT(&victim_ptr->TIME_SLICE, 
         template_ptr->DEFAULT_TIME_SLICE, &victim_ptr->TIME_SLICE);
#endif
   } else {
      victim_ptr->TIME_SLICE = kernel_data->SCHED_TIME_SLICE;
   } /* Endif */
#endif

   /* Reset the io streams back to the default */
   victim_ptr->STDIN_STREAM  = kernel_data->PROCESSOR_STDIN;
   victim_ptr->STDOUT_STREAM = kernel_data->PROCESSOR_STDOUT;
   victim_ptr->STDERR_STREAM = kernel_data->PROCESSOR_STDERR;

   /* Reset scheduling policy for task */
   if (kernel_data->SCHED_POLICY == MQX_SCHED_RR) {
      victim_ptr->FLAGS |= MQX_TIME_SLICE_TASK;
   } /* Endif */

   /* Rebuild the task's initial context */
   _psp_build_stack_frame(victim_ptr, stack_ptr, stack_size,
      template_ptr, victim_ptr->TASK_SR, create_param);

   if (victim_ptr == td_ptr) {
      _QUEUE_UNLINK(victim_ptr);  /* Remove victim from the ready queue */
   } /* Endif */

   _int_disable();

   if (victim_ptr == td_ptr) {
      _QUEUE_UNLINK(victim_ptr);
   } /* Endif */

   if (blocked) {
      victim_ptr->STATE = BLOCKED;
   } else {
      _task_ready_internal(victim_ptr);
   } /* Endif */

   victim_ptr->DISABLED_LEVEL = 0;

   if (victim_ptr == td_ptr) {
      /* 
      ** The active task is to be restarted.
      ** Will not return from here
      */
      _sched_run_internal();
   } /* Endif */

   /* The victim has been added to the ready Q. Preemption check must be made */
   if (kernel_data->CURRENT_READY_Q != td_ptr->MY_QUEUE) {
      _sched_execute_scheduler_internal();
   } /* Endif */

   _int_enable();

   _KLOGX2(KLOG_task_restart, MQX_OK);
   return(MQX_OK);

} /* Endbody */

/* EOF */
