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
*** File: int_isr.c
***
*** Comments:      
***   This file contains the function for the default ISR called
*** by MQX if an unhandled interrupt/exception occurs.
***
*** This function can be replaced by the user as the default ISR
*** by calling:
***     _int_install_unexpected_isr or _int_install_exception_isr
*** which install MQX provided default ISRs.
***
*** or by calling _int_install_default_isr 
*** to install an application provided default ISR.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_default_isr
* Returned Value   : void
* Comments         :
*    The second level handler for all unhandled interrupts.
*
*END*----------------------------------------------------------------------*/

void _int_default_isr
   (
      /* [IN] the parameter passed to the ISR by the kernel */
      pointer vector_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;

   _GET_KERNEL_DATA(kernel_data);

   td_ptr = kernel_data->ACTIVE_PTR;
   _KLOGE5(KLOG_int_default_isr, td_ptr, vector_number, 
      &vector_number, vector_number);

   _int_disable();
   if (td_ptr->STATE != UNHANDLED_INT_BLOCKED) {
      td_ptr->STATE = UNHANDLED_INT_BLOCKED;
      td_ptr->INFO  = (_mqx_uint)vector_number;

      _QUEUE_UNLINK(td_ptr);
   } /* Endif */
   _int_enable();

} /* Endbody */

/* EOF */
