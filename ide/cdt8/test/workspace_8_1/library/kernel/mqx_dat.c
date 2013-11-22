/*HEADER*******************************************************************
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
*** File: mqx_dat.c
***
*** Comments:      
***   This file contains the function that returns the address of the
*** kernel data structure.
***
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_kernel_data
* Returned Value   : none
* Comments         :
*    This function returns the address of the kernel data
*
*END*-----------------------------------------------------------------------*/

pointer _mqx_get_kernel_data
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   return (pointer)kernel_data;

} /* Endbody */

/* EOF */
