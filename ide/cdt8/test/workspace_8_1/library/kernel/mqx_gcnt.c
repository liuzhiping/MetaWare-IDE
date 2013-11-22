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
*** File: mqx_gcnt.c
***
*** Comments:      
***   This file contains the function for returning a unique number
*** from the kernel.  This number is simply incremented.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_counter
* Returned Value   : _mqx_uint counter
* Comments         : 
*    This function increments the counter and then returns value of the counter.
*    This provides a unique number for whoever requires it.  
*    Note that this unique number will never be 0.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mqx_get_counter
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
            _mqx_uint                return_value;

   _GET_KERNEL_DATA(kernel_data);
   _INT_DISABLE();
 
   /*
   ** Increment counter, and ensure it is not zero. If it is zero, set it to
   ** one.
   */
   if ( ++kernel_data->COUNTER == 0 ) {
      kernel_data->COUNTER = 1;
   } /* Endif */
   return_value = kernel_data->COUNTER;
   _INT_ENABLE();
   return (return_value);

} /* Endbody */

/* EOF */
