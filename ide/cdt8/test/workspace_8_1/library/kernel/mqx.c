/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: mqx.c
***
*** Comments:
***   This file contains the source for the main MQX function _mqx().
***
***
**************************************************************************
*END*********************************************************************/

#ifndef __NO_SETJMP
#include <setjmp.h>
#endif

#include "mqx_inc.h"
#include "gen_rev.h"
#include "psp_rev.h"

/*
** A global zero initialized MQX tick structure.
** It is used by various MQX functions that need
** to zero initialize local tick structures. An
** extern to it is provided in MQX.H so applications
** can use it as well.
*/
const MQX_TICK_STRUCT _mqx_zero_tick_struct = {0};

/* Identify the product */
#if MQX_CRIPPLED_EVALUATION
const char _PTR_ _mqx_version           = "2.52c";
#else
const char _PTR_ _mqx_version           = "2.52";
#endif
const char _PTR_ _mqx_generic_revision  = REAL_NUM_TO_STR(GEN_REVISION);
const char _PTR_ _mqx_psp_revision      = REAL_NUM_TO_STR(PSP_REVISION);
const char _PTR_ _mqx_copyright = "(c) 1989-2007 ARC International. All rights reserved.";
const char _PTR_ _mqx_date      = __DATE__ " at " __TIME__;

/* A global pointer to the location of the kernel data structure */
struct kernel_data_struct _PTR_ _mqx_kernel_data;
volatile pointer _mqx_system_stack; /* CR 1169 */

/* Error return jump buffer for kernel errors */
#if MQX_EXIT_ENABLED || MQX_CRIPPLED_EVALUATION
jmp_buf _mqx_exit_jump_buffer_internal;
#endif

/*FUNCTION********************************************************************
*
* Function Name    : _mqx
* Returned Value   : _mqx_uint result
* Comments         :
*      This function initializes and starts up MQX.  It will return
* if an error is detected, or the application calls _mqx_exit.
*
*END************************************************************************/

_mqx_uint _mqx
   (
      /* [IN] the address of the MQX initialzation structure */
      register MQX_INITIALIZATION_STRUCT_PTR mqx_init
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   TASK_TEMPLATE_STRUCT_PTR template_ptr;
   TD_STRUCT_PTR            td_ptr;
   _mqx_uint                 result;
   pointer                  stack_ptr;
   /* Start CR 1169 */   
   pointer					sys_td_stack_ptr;
   uchar_ptr                sys_stack_base_ptr;
   /* End CR 1169 */

#if MQX_EXIT_ENABLED || MQX_CRIPPLED_EVALUATION
   /* Setup a longjmp buffer using setjmp, so that if an error occurs
   ** in mqx initialization, we can perform a longjmp to this location.
   **
   ** Also _mqx_exit will use this jumpbuffer to longjmp to here in order
   ** to cleanly exit MQX.
   */
   if ( MQX_SETJMP( _mqx_exit_jump_buffer_internal ) ) {
      _GET_KERNEL_DATA(kernel_data);
      _int_set_vector_table(kernel_data->USERS_VBR);
      return kernel_data->USERS_ERROR;
   } /* Endif */
#endif

   /*
   ** The kernel data structure starts at the start of kernel memory,
   ** as specified in the initialization structure. Make sure address
   ** specified is aligned
   */
   kernel_data = (KERNEL_DATA_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(mqx_init->START_OF_KERNEL_MEMORY);

   /* Set the global pointer to the kernel data structure */
   _SET_KERNEL_DATA(kernel_data);

   /* Set the global pointer to the kernel data structure */
   _mqx_kernel_data = (struct kernel_data_struct _PTR_)kernel_data;

   /* Initialize the kernel data to zero. */
   _mem_zero((pointer)kernel_data, (_mem_size)sizeof(KERNEL_DATA_STRUCT));

#if MQX_CHECK_ERRORS && MQX_VERIFY_KERNEL_DATA
   /* Verify that kernel data can be read and written correcly without
   ** errors.  This is necessary during BSP development to validate the
   ** DRAM controller is initialized properly.
   */
   /* Start CR 1327 */
# ifndef PSP_KERNEL_DATA_VERIFY_ENABLE
   /*
   ** PSP's should define this to be a link time symbol so users can
   ** enable/disable just by re-linking their program.
   */
#  define PSP_KERNEL_DATA_VERIFY_ENABLE	1
# endif
   if (PSP_KERNEL_DATA_VERIFY_ENABLE) {
   /* End CR 1327 */
      result = _mem_verify((uchar_ptr)kernel_data + sizeof(KERNEL_DATA_STRUCT),
         mqx_init->END_OF_KERNEL_MEMORY);
      if ( result != MQX_OK ) {
	 _mqx_exit(result);   /* RETURN TO USER */
      } /* Endif */
   } /* Endif */
#endif

   /* Copy the MQX initialization structure into kernel data. */
   kernel_data->INIT = *mqx_init;
   kernel_data->INIT.START_OF_KERNEL_MEMORY = (pointer)kernel_data;
   kernel_data->INIT.END_OF_KERNEL_MEMORY = (pointer)
      _ALIGN_ADDR_TO_LOWER_MEM(kernel_data->INIT.END_OF_KERNEL_MEMORY);

    /* init kernel data structures */
   _mqx_init_kernel_data_internal();

   /* Initialize the memory resource manager for the kernel */
   result = _mem_init_internal();
#if MQX_CHECK_ERRORS
   if ( result != MQX_OK ) {
      _mqx_exit(result);   /* RETURN TO USER */
   } /* Endif */
#endif

   /* Now obtain the interrupt stack */
/* START CR 897 */
   if (kernel_data->INIT.INTERRUPT_STACK_LOCATION) {
      stack_ptr = kernel_data->INIT.INTERRUPT_STACK_LOCATION;
      result    = kernel_data->INIT.INTERRUPT_STACK_SIZE;
   } else {
      if ( kernel_data->INIT.INTERRUPT_STACK_SIZE < PSP_MINSTACKSIZE ) {
         kernel_data->INIT.INTERRUPT_STACK_SIZE = PSP_MINSTACKSIZE;
      } /* Endif */
#if PSP_STACK_ALIGNMENT
      result = kernel_data->INIT.INTERRUPT_STACK_SIZE + PSP_STACK_ALIGNMENT + 1;
#else
      result = kernel_data->INIT.INTERRUPT_STACK_SIZE;
#endif
      stack_ptr = _mem_alloc_system((_mem_size)result);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (stack_ptr == NULL) {
         _mqx_exit(MQX_OUT_OF_MEMORY);   /* RETURN TO USER */
      } /* Endif */
#endif
   } /* Endif */

#if MQX_MONITOR_STACK
   _task_fill_stack_internal((_mqx_uint_ptr)stack_ptr, result);
#endif

   kernel_data->INTERRUPT_STACK_PTR = _GET_STACK_BASE(stack_ptr, result);
/* END CR 897 */

/* Start CR 1169 */
   /*
   ** Set the stack for the system TD, in case the idle task gets blocked 
   ** by an exception or if idle task is not used.
   */
   result = PSP_MINSTACKSIZE;
   sys_td_stack_ptr = _mem_alloc_system((_mem_size)result);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (sys_td_stack_ptr == NULL) {
      _mqx_exit(MQX_OUT_OF_MEMORY);   /* RETURN TO USER */
   } /* Endif */
#endif   
   sys_stack_base_ptr  = (uchar_ptr)_GET_STACK_BASE(sys_td_stack_ptr, result);
   td_ptr = SYSTEM_TD_PTR(kernel_data);
   td_ptr->STACK_PTR   = (pointer)(sys_stack_base_ptr - sizeof(PSP_STACK_START_STRUCT));
   td_ptr->STACK_BASE  = sys_stack_base_ptr;
   td_ptr->STACK_LIMIT = _GET_STACK_LIMIT(sys_td_stack_ptr, result);
   _mqx_system_stack = td_ptr->STACK_PTR;
/* End CR 1169 */

   /* Build the MQX ready to run queues */
   result = _psp_init_readyqs();
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if ( result != MQX_OK ) {
      _mqx_exit(result);   /* RETURN TO USER */
   } /* Endif */
#endif

   /* Create a light wait semaphore for component creation */
   _lwsem_create((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM, 1);

   /* Create a light wait semaphore for task creation/destruction creation */
   _lwsem_create((LWSEM_STRUCT_PTR)&kernel_data->TASK_CREATE_LWSEM, 1);

   /* Call bsp to enable timers and other devices */
   result = _bsp_enable_card();
#if MQX_CHECK_ERRORS
   if ( result != MQX_OK ) {
      _mqx_exit(result);   /* RETURN TO USER */
   } /* Endif */
#endif

#if MQX_HAS_TIME_SLICE
   /* Set the kernel default time slice value */
   PSP_ADD_TICKS_TO_TICK_STRUCT(&kernel_data->SCHED_TIME_SLICE,
      MQX_DEFAULT_TIME_SLICE, &kernel_data->SCHED_TIME_SLICE);
#endif

   /* Create the idle task */
#if MQX_USE_IDLE_TASK
/* START CR 897 */
   td_ptr = _task_init_internal(
      (TASK_TEMPLATE_STRUCT_PTR)&kernel_data->IDLE_TASK_TEMPLATE,
      kernel_data->ACTIVE_PTR->TASK_ID, (uint_32)0, TRUE, NULL, 0);
/* END CR 897 */
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (td_ptr == NULL) {
      _mqx_exit(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif
   _task_ready_internal(td_ptr);
#endif

   /* Check here for auto-create tasks, and create them here */
   template_ptr = kernel_data->TASK_TEMPLATE_LIST_PTR;
   while (template_ptr->TASK_TEMPLATE_INDEX) {
      if (template_ptr->TASK_ATTRIBUTES & MQX_AUTO_START_TASK) {
/* START CR 897 */
         td_ptr = _task_init_internal( template_ptr,
            kernel_data->ACTIVE_PTR->TASK_ID,
            template_ptr->CREATION_PARAMETER, FALSE, NULL, 0);
/* END CR 897 */
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
         if (td_ptr == NULL) {
            _mqx_exit(MQX_OUT_OF_MEMORY);
         } /* Endif */
#endif
         _task_ready_internal(td_ptr);
      } /* Endif */
      ++template_ptr;
   } /* Endwhile */

   _sched_start_internal();   /* WILL NEVER RETURN FROM HERE */

   return MQX_OK;  /* To satisfy lint */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _mqx_exit
* Returned Value   : none
* Comments         :
*   This function causes mqx to exit.
*   If an exit handler has been installed, it is called before exiting.
*
************************************************************************/

void _mqx_exit
   (
      /* [IN] the error code to return to the calling function of _mqx */
      _mqx_uint error
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _int_disable();

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_mqx_exit, error);

#if MQX_EXIT_ENABLED
   kernel_data->USERS_ERROR = error;
   if (kernel_data->EXIT_HANDLER) {
      (*kernel_data->EXIT_HANDLER)();
   }/* Endif */
   MQX_LONGJMP( _mqx_exit_jump_buffer_internal, 1 );
#else
   while (TRUE) {
   } /* Endwhile */
#endif

} /* Endbody */

/* EOF */
