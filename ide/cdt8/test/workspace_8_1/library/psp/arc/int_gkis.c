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
*** File: int_gkis.c
***
*** Comments:      
***   This file contains the functions for returning the kernel 
*** isr for an interrupt.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_kernel_isr
* Returned Value   : _CODE_PTR_ address or NULL on error
* Comments         : 
*   This function retrieves the address of the first level interrupt
*   handler for the specified vector
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_get_kernel_isr
   (
      /* [IN] the vector number whose kernel ISR is wanted */
      uint_32 vector

   ))(void)
{ /* Body */
   uint_32_ptr vbr_ptr;

   vbr_ptr = (uint_32_ptr)_int_get_vector_table();

#if MQX_CHECK_ERRORS
   if ( vector >= PSP_MAXIMUM_INTERRUPT_VECTORS ) {
      _task_set_error(MQX_INVALID_VECTORED_INTERRUPT);
      return NULL;
   } /* Endif */
#endif

   vector = (vector * 2) + 1;
   return (void (_CODE_PTR_)(void))vbr_ptr[vector];

} /* Endbody */

/* EOF */
