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
*** File: psp_iinit.c
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
* Function Name    : _psp_int_init
* Returned Value   : void
* Comments         :
*    This function initializes the kernel interrupt tables as well as the
* low level interrupt table.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _psp_int_init
   (
      /* [IN] the first (lower) user ISR vector number */
      _mqx_uint first_user_isr_vector_number,

      /* [IN] the last user ISR vector number */
      _mqx_uint last_user_isr_vector_number
   )
{ /* Body */
   uint_32 error;

   /* Install kernel interrupt services */
   error = _int_init(first_user_isr_vector_number, last_user_isr_vector_number);

   /* Install PSP interrupt services */
   if (error == MQX_OK) {
      _psp_int_install();
   } /* Endif */

   return error;
      
} /* Endbody */

/* EOF */
