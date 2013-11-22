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
*** File: mqx_init.c
***
*** Comments:      
***   This file contains the function for initializing the kernel 
*** data structure.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_init_kernel_data_internal
* Returned Value   : none
* Comments         :
* Initialize the static parts of the kernel data structure.
*
* Care has to be taken when calling functions within this file, as the 
* kernel is not running yet. Specifically, functions which rely on
* _mqx_get_kernel_data can not be called.
*
*END*----------------------------------------------------------------------*/

void _mqx_init_kernel_data_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR        kernel_data;
   TASK_TEMPLATE_STRUCT_PTR      task_template_ptr;
   TD_STRUCT_PTR                 td_ptr;
   _mqx_uint                      priority_levels;
   _mqx_uint                      i;

   _GET_KERNEL_DATA(kernel_data);

   /* Store the configuration used when the kernel was compiled */
   kernel_data->CONFIG1 = MQX_CNFG1;
   kernel_data->CONFIG2 = MQX_CNFG2;

   /* Store the addressability of the processor. How many bits in a byte. */
   kernel_data->ADDRESSING_CAPABILITY = PSP_MEMORY_ADDRESSING_CAPABILITY;

   /* Indicate the endianess of the target */
   kernel_data->ENDIANESS = PSP_ENDIAN;

   /* Store PSP memory alignment information */

#if PSP_MEM_STOREBLOCK_ALIGNMENT != 0
   kernel_data->PSP_CFG_MEM_STOREBLOCK_ALIGNMENT = PSP_MEM_STOREBLOCK_ALIGNMENT;
#endif

   kernel_data->PSP_CFG_MEMORY_ALIGNMENT = PSP_MEMORY_ALIGNMENT;
   kernel_data->PSP_CFG_STACK_ALIGNMENT  = PSP_STACK_ALIGNMENT;

   /*
   ** Fill in fields of the kernel data structure from the initialization
   ** structure.
   */
   kernel_data->PROCESSOR_NUMBER =  kernel_data->INIT.PROCESSOR_NUMBER;

   /* Set IPC id for compatibility */
#if MQX_USE_IPC
   kernel_data->MY_IPC_ID = BUILD_TASKID(kernel_data->PROCESSOR_NUMBER, 1);
#endif

    /* Store location of current interrupt vector table */
#if MQX_EXIT_ENABLED
   kernel_data->USERS_VBR = (_mqx_max_type)_int_get_vector_table();
#endif

   kernel_data->TASK_TEMPLATE_LIST_PTR = kernel_data->INIT.TASK_TEMPLATE_LIST;
#if MQX_CHECK_ERRORS
   if (kernel_data->TASK_TEMPLATE_LIST_PTR == NULL) {
      _mqx_exit(MQX_INVALID_POINTER);
   } /* Endif */
#endif

   /* Set the default scheduling policy for created tasks */
   kernel_data->SCHED_POLICY = MQX_SCHED_FIFO;

   /* Initialize the kernel counter. */
   kernel_data->COUNTER    = 1U;

   /* Set up the disable and enable priority levels */
   _psp_set_kernel_disable_level();

   /*
   ** Initialize the system task so that functions which update the
   ** task error code can be called. 
   ** The system task never runs, but it's TD is used for error codes
   ** during initialization, and for storage of memory blocks assigned
   ** to the system.
   */
   td_ptr = (TD_STRUCT_PTR)&kernel_data->SYSTEM_TD;
   kernel_data->ACTIVE_PTR  = td_ptr;
   kernel_data->ACTIVE_SR   = kernel_data->DISABLE_SR;
   td_ptr->TASK_SR          = kernel_data->DISABLE_SR;
   td_ptr->TASK_ID = BUILD_TASKID(kernel_data->PROCESSOR_NUMBER, 0);
   td_ptr->STATE   = BLOCKED;

   /* Initialize the light weight semaphores queue */
   _QUEUE_INIT(&kernel_data->LWSEM, 0);
     
   /* Set up the timeout queue */
   _QUEUE_INIT(&kernel_data->TIMEOUT_QUEUE, 0);

   /* 
   ** Compute the number of MQX priority levels needed. This is done
   ** by determining the task that has the lowest priority (highest number)
   */
   priority_levels = 0;
   task_template_ptr = kernel_data->INIT.TASK_TEMPLATE_LIST;
   for (i = 0; 
      task_template_ptr->TASK_TEMPLATE_INDEX && 
         (i < MQX_MAXIMUM_NUMBER_OF_TASK_TEMPLATES); 
      ++i, ++task_template_ptr) 
   {
      ++kernel_data->NUM_TASK_TEMPLATES;
      if (priority_levels < task_template_ptr->TASK_PRIORITY)  {
         priority_levels = task_template_ptr->TASK_PRIORITY;
      } /* Endif */
   } /* Endfor */
   kernel_data->LOWEST_TASK_PRIORITY = priority_levels;
   

   /* 
   ** Initialize the task template for the IDLE Task.   
   ** NOTE that the idle task runs at 1 level lower than any user task.
   */
   task_template_ptr = (TASK_TEMPLATE_STRUCT_PTR)
      &kernel_data->IDLE_TASK_TEMPLATE;
#if MQX_USE_IDLE_TASK
   task_template_ptr->TASK_TEMPLATE_INDEX = IDLE_TASK;
   task_template_ptr->TASK_STACKSIZE      = PSP_IDLE_STACK_SIZE;
   task_template_ptr->TASK_NAME           = "_mqx_idle_task";
   task_template_ptr->TASK_ADDRESS        = _mqx_idle_task;
#endif
   task_template_ptr->TASK_PRIORITY       = priority_levels + 1;

   /* 
   ** Initialize the linked list of all TDs in the system.
   ** Initially zero. Not including system TD
   */
   _QUEUE_INIT(&kernel_data->TD_LIST, 0);

   /* Set the TD counter */
   /* Start SPR P171-0014-02       */
   /* kernel_data->TD_COUNTER = 1; */
   kernel_data->TASK_NUMBER = 1;
   /* End SPR P171-0014-02         */

} /* Endbody */

/* EOF */
