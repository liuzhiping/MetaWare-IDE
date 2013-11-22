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
*** File: int_seh.c
***
*** Comments:      
***   This file contains the function for setting an exception handler
*** for an ISR.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _int_set_exception_handler
* Returned Value   : _CODE_PTR_ - address of old exception handler
* Comments         :
*  sets the address of the current ISR exception handler,
* and returns the old one.
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _int_set_exception_handler
   (
      /* [IN] the interrupt vector that this exception handler is for */
      _mqx_uint vector,

      /* [IN] the exception handler function address */
      void (_CODE_PTR_ error_handler_address)(_mqx_uint, _mqx_uint, pointer, pointer)

   ))(_mqx_uint, _mqx_uint, pointer, pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   void (_CODE_PTR_ old_handler)(_mqx_uint, _mqx_uint, pointer, pointer) = NULL;
 
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_int_set_exception_handler, vector, error_handler_address);

#if MQX_CHECK_ERRORS
   if ( kernel_data->INTERRUPT_TABLE_PTR == NULL ) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      _KLOGX2(KLOG_int_set_exception_handler, NULL);
      return(NULL);
   } /* Endif */
   if ((vector < kernel_data->FIRST_USER_ISR_VECTOR) ||
       (vector > kernel_data->LAST_USER_ISR_VECTOR))
   {
      _task_set_error(MQX_INVALID_VECTORED_INTERRUPT);
      _KLOGX2(KLOG_int_set_exception_handler, NULL);
      return(NULL);
   } /* Endif */
#endif

   vector -= kernel_data->FIRST_USER_ISR_VECTOR;

   old_handler = kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_EXCEPTION_HANDLER;
   kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_EXCEPTION_HANDLER = 
      error_handler_address;

   _KLOGX2(KLOG_int_set_exception_handler, old_handler);
   return(old_handler);

} /* Endbody */

/* EOF */
