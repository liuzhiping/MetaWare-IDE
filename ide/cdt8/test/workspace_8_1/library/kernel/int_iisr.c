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
*** File: int_iisr.c
***
*** Comments:      
***   This file contains the function for installing an ISR.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_install_isr
* Returned Value   : _CODE_PTR_ address or NULL on error
* Comments         :
*    This function provides a user interface for dynamic isr
*    installation.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_install_isr
   (
      /* [IN] the interrupt vector number (NOT OFFSET) */
      _mqx_uint  vector,

      /* [IN] the address of the function to be executed */
      void (_CODE_PTR_ isr_ptr)(pointer),

      /* 
      ** [IN] the value to be provided to the function as it's
      ** first parameter when an interrupt occurs.
      */
      pointer  isr_data

   ))(pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   INTERRUPT_TABLE_STRUCT_PTR table_ptr;
   void         (_CODE_PTR_   old_isr_ptr)(pointer);

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_int_install_isr, vector, isr_ptr, isr_data);

#if MQX_CHECK_ERRORS
   if ( kernel_data->INTERRUPT_TABLE_PTR == NULL ) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      return(NULL);
   } /* Endif */      
   if ((vector < kernel_data->FIRST_USER_ISR_VECTOR) ||
       (vector > kernel_data->LAST_USER_ISR_VECTOR))
   {
      _task_set_error(MQX_INVALID_VECTORED_INTERRUPT);
      _KLOGX2(KLOG_int_install_isr, NULL);
      return(NULL);
   }/* Endif */
#endif

   table_ptr = &kernel_data->INTERRUPT_TABLE_PTR[vector -
      kernel_data->FIRST_USER_ISR_VECTOR];

   _int_disable();
   old_isr_ptr = table_ptr->APP_ISR;
   table_ptr->APP_ISR  = isr_ptr;
   table_ptr->APP_ISR_DATA = isr_data;
   _int_enable();

   _KLOGX2(KLOG_int_install_isr, old_isr_ptr);

   return (old_isr_ptr);

} /* Endbody */

/* EOF */
