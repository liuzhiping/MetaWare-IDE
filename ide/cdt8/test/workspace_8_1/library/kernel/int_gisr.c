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
*** File: int_gisr.c
***
*** Comments:      
***   This file contains the function for returning the current
*** ISR handler for a specified vector number.
***                                                               
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_isr
* Returned Value   : _CODE_PTR_ address or NULL on error
* Comments         : 
*   This function retrieves the address of the first level interrupt
*   handler for the specified vector
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_get_isr
   ( 
      /* [IN] the vector number whose ISR is to be returned */
      _mqx_uint vector

   ))(pointer)
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
      return NULL;
   } /* Endif */
#endif

   vector -= kernel_data->FIRST_USER_ISR_VECTOR;

   return(kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR);

} /* Endbody */

/* EOF */
