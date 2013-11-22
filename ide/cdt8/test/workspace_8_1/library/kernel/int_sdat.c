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
*** File: int_sdat.c
***
*** Comments:      
***   This file contains the function for setting the data parameter
*** for an ISR
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_set_isr_data
* Returned Value   : pointer address or NULL on error
* Comments         : 
*   This function sets the address of the interrupt handler data
*   for the specified vector, and returns the old value
*
*END*----------------------------------------------------------------------*/

pointer _int_set_isr_data
   ( 
      /* [IN] the interrupt vector that this data is for */
      _mqx_uint vector,
      
      /* [IN] the data passed to the ISR by the kernel */
      pointer data
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                old_data;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_int_set_isr_data,vector,data);

#if MQX_CHECK_ERRORS
   if ( kernel_data->INTERRUPT_TABLE_PTR == NULL ) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      _KLOGX2(KLOG_int_set_isr_data,NULL);
      return(NULL);
   } /* Endif */
   if ((vector < kernel_data->FIRST_USER_ISR_VECTOR) ||
       (vector > kernel_data->LAST_USER_ISR_VECTOR))
   {
      _task_set_error(MQX_INVALID_VECTORED_INTERRUPT);
      _KLOGX2(KLOG_int_set_isr_data,NULL);
      return(NULL);
   } /* Endif */
#endif

   vector -= (kernel_data->FIRST_USER_ISR_VECTOR);

   old_data = kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_DATA;
   kernel_data->INTERRUPT_TABLE_PTR[vector].APP_ISR_DATA = data;

   _KLOGX2(KLOG_int_set_isr_data,old_data);
   return old_data;

} /* Endbody */

/* EOF */
