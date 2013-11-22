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
*** File: ti_shwtf.c
***
*** Comments:      
***   This file contains the function for setting the function to get the 
*** hardware ticks
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_hwtick_function
* Returned Value   : none
* Comments         :
*    This function set the fields in kernel data to get the hardware ticks
*
*END*----------------------------------------------------------------------*/

void _time_set_hwtick_function
   (
      /* [IN] the address of the function to be executed */
      uint_32 (_CODE_PTR_ hwtick_function_ptr)(pointer),
      
      pointer parameter
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   kernel_data->GET_HWTICKS       = hwtick_function_ptr;
   kernel_data->GET_HWTICKS_PARAM = parameter;

} /* Endbody */

/* EOF */
