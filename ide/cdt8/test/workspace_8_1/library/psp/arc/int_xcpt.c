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
*** File: int_xcpt.c
***
*** Comments:      
***   This file contains the isr that handles exceptions.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*ISR*---------------------------------------------------------------------
*
* Function Name    : _int_exception_isr
* Returned Value   : none
* Comments         :
*  This function is the exception handler which may be used to replace the
* default exception handler, in order to provide support for exceptions.
*
* If an exception happens while a task is running,
* then: if a task exception handler exists, it is executed, 
*       otherwise the task is aborted.
*
* If an exception happens while an isr is running,
* then: if an isr exception handler exists, it is executed, and the isr aborted
*       otherwise the isr is aborted.
*
*END*-------------------------------------------------------------------------*/

void _int_exception_isr
   (
      /* [IN] the parameter passed to the default ISR, the vector number */
      pointer parameter
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   TD_STRUCT_PTR              td_ptr;
   PSP_INT_CONTEXT_STRUCT_PTR exception_frame_ptr;
   PSP_INT_CONTEXT_STRUCT_PTR isr_frame_ptr;
   INTERRUPT_TABLE_STRUCT_PTR table_ptr;
   void                     (*exception_handler)
      (uint_32, uint_32, pointer, pointer);
   uint_32                    exception_vector;
   uint_32                    isr_vector;
   void                     (*func)(void);
   pointer                    fp_addr;

   _GET_KERNEL_DATA(kernel_data);
   td_ptr = kernel_data->ACTIVE_PTR;

   /* Stop all interrupts */
   _PSP_SET_DISABLE_SR(kernel_data->DISABLE_SR);

   if ( kernel_data->IN_ISR > 1 ) {
      /* We have entered this function from an exception that happened
      ** while an isr was running.
      */

      /* the interrupt context pointer stored in the kernel is part
      ** of the interrupt frame structure, whose address can then be determined.
      */
      exception_frame_ptr = (pointer)kernel_data->INTERRUPT_CONTEXT_PTR;
      exception_vector    = exception_frame_ptr->EXCEPTION_NUMBER;

      /* the current context contains a pointer to the next one */
      isr_frame_ptr = (pointer)
         exception_frame_ptr->PREV_CONTEXT;
      if (isr_frame_ptr == NULL) {
         /* This is not allowable */
         _mqx_fatal_error(MQX_CORRUPT_INTERRUPT_STACK);
      }/* Endif */
      isr_vector    = isr_frame_ptr->EXCEPTION_NUMBER;

      /* Call the isr exception handler for the ISR that WAS running */
      table_ptr = kernel_data->INTERRUPT_TABLE_PTR;
#if MQX_CHECK_ERRORS
      if ((table_ptr != NULL) &&
         (isr_vector >= kernel_data->FIRST_USER_ISR_VECTOR) &&
         (isr_vector <= kernel_data->LAST_USER_ISR_VECTOR))
      {
#endif
      /* Call the exception handler for the isr on isr_vector,
      ** passing the isr_vector, the exception_vector, the isr_data and
      ** the basic frame pointer for the exception
      */
      table_ptr = &table_ptr[isr_vector - kernel_data->FIRST_USER_ISR_VECTOR];
      exception_handler = table_ptr->APP_ISR_EXCEPTION_HANDLER;
      if (exception_handler != NULL) {
         (*exception_handler)(isr_vector, exception_vector, 
            table_ptr->APP_ISR_DATA, exception_frame_ptr);
      } /* Endif */

#if MQX_CHECK_ERRORS
      } else {
         /* In this case, the exception occured in this handler */
         _mqx_fatal_error(MQX_INVALID_VECTORED_INTERRUPT);
      } /* Endif */
#endif
      
      /* Indicate we have popped 1 interrupt stack frame (the exception frame) */
      --kernel_data->IN_ISR;

      /* Jump to the location in _int_kernel_isr after where the user isr
      ** has returned, and before we de-allocate the interrupt frame
      */
      func = _int_kernel_isr_return_internal;

      /* Reset the link register */
      fp_addr = (pointer)((uchar_ptr)isr_frame_ptr +
         sizeof(PSP_INT_CONTEXT_STRUCT));

      _PSP_SET_FP_SP_AND_GO(fp_addr, isr_frame_ptr, func);
   } else {
      /* We have entered this function from an exception that happened
      ** while a task was running.
      */
      if (td_ptr->EXCEPTION_HANDLER_PTR != NULL ) {
         (*td_ptr->EXCEPTION_HANDLER_PTR)((uint_32)parameter,
            td_ptr->STACK_PTR);
      } else {
         /* Abort the current task */
         _task_abort(MQX_NULL_TASK_ID);
      }/* Endif */

   }/* Endif */
   
} /* Endbody */

/* EOF */
