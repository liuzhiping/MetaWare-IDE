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
*** File: int_dep.c
***
*** Comments:      
***   This file contains the function for returning the current ISR
*** nesting depth.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_isr_depth
* Returned Value   : _mqx_uint depth
* Comments         :
*    This function returns the nesting depth of the current interrupt stack.
* If not in an interrupt this returns 0.
* If in an interrupt it will return 1.
* If in a nested interrupt, it will return 2 or more.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _int_get_isr_depth
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   
   _GET_KERNEL_DATA(kernel_data);
   return( kernel_data->IN_ISR );
   
} /* Endbody */

/* EOF */
