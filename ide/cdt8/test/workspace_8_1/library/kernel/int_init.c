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
*** File: int_init.c
***
*** Comments:      
***   This file contains the function for initializing the handling of 
***   interrupts.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_init
* Returned Value   : _mqx_uint - MQX OK or an error code
* Comments         :
*    This function initializes the kernel interrupt table.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _int_init
   (
      /* [IN] the first (lower) user ISR vector number */
      _mqx_uint          first_user_isr_vector_number,

      /* [IN] the last user ISR vector number */
      _mqx_uint          last_user_isr_vector_number

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   INTERRUPT_TABLE_STRUCT_PTR int_table_ptr;
   _mqx_uint                  number;

#if MQX_CHECK_ERRORS
   if (last_user_isr_vector_number < first_user_isr_vector_number) {
      return MQX_INVALID_PARAMETER;
   } /* Endif */
#endif

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->INT_KERNEL_ISR_ADDR = _int_kernel_isr;

   /* Set the current default ISR for MQX that is called whenever an 
   ** unhandled interrupt occurs
   */
   kernel_data->DEFAULT_ISR = _int_default_isr;

   number = last_user_isr_vector_number - first_user_isr_vector_number + 1;

   int_table_ptr = _mem_alloc_system_zero((_mem_size)
      (sizeof(INTERRUPT_TABLE_STRUCT) * number));   
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if ( int_table_ptr == NULL ) {
      return(MQX_OUT_OF_MEMORY);
   }/* Endif */
#endif

   kernel_data->INTERRUPT_TABLE_PTR   = int_table_ptr;
   kernel_data->FIRST_USER_ISR_VECTOR = first_user_isr_vector_number;
   kernel_data->LAST_USER_ISR_VECTOR  = last_user_isr_vector_number;
   
   while (number--) {
      int_table_ptr->APP_ISR      = _int_default_isr;
      int_table_ptr->APP_ISR_DATA = (pointer)(first_user_isr_vector_number++);
      ++int_table_ptr;
   } /* Endwhile */

   return MQX_OK;
   
} /* Endbody */

/* EOF */
