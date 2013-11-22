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
*** File: int_geh.c
***
*** Comments:      
***   This file contain the function for returning the current exception
*** handler for an ISR.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _int_get_exception_handler
* Returned Value   : void _CODE_PTR_ address or NULL on error
* Comments         :
*  returns the address of the current ISR exception handler
*
*END*-------------------------------------------------------------------------*/

void (_CODE_PTR_ _int_get_exception_handler
   (
      /* [IN] the vector number whose exception handler is to be returned */
      _mqx_uint vector

   ))(_mqx_uint, _mqx_uint, pointer, pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
#if MQX_CHECK_ERRORS
   if ( kernel_data->INTERRUPT_TABLE_PTR == NULL ) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      return(NULL);
   } /* Endif */
   if ((vector < kernel_data->FIRST_USER_ISR_VECTOR) ||
       (vector > kernel_data->LAST_USER_ISR_VECTOR))
   {
      _task_set_error(MQX_INVALID_VECTORED_INTERRUPT);
      return(NULL);
   }/* Endif */
#endif

   vector -= kernel_data->FIRST_USER_ISR_VECTOR;
   return(kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_EXCEPTION_HANDLER);
      
} /* Endbody */

/* EOF */
