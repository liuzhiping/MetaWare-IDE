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
*** File: int_ixcp.c
***
*** Comments:      
***   This file contains the function for installing the exception handler
*** as the default ISR.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _int_install_exception_isr
* Returned Value   : _CODE_PTR_ addressof previous default ISR
* Comments         :
*   This function installs the exception isr as the default ISR.
*   The exception ISR handler performs the following service:
*
*   If an unhandled interrupt occurs and
*      a) A task is running
*         i)  If the task has an exception handler, call this handler
*         ii) Otherwise, abort the task (_task_abort)
*      b) An ISR is running
*         If the ISR has an exception handler installed, call it.
*         then remove both the exception and ISR interrupt frames.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _int_install_exception_isr
   (
      void 
   ))(pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->FLAGS |= MQX_FLAGS_EXCEPTION_HANDLER_INSTALLED;
   return(_int_install_default_isr(_int_exception_isr));

} /* Endbody */

/* EOF */
