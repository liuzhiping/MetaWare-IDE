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
*** File: int_gdat.c
***
*** Comments:      
***   This file contains the function for returning the data pointer
*** for an ISR.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_isr_data
* Returned Value   : pointer address or NULL on error
* Comments         : 
*   This function retrieves the address of the interrupt handler data
*   for the specified vector
*
*END*----------------------------------------------------------------------*/

pointer _int_get_isr_data
   (
      /* [IN] the vector number whose ISR data is to be returned */
      _mqx_uint vector
   )
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
   return(kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_DATA);
      
} /* Endbody */


/* EOF */
