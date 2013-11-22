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
*** File: mqx_gini.c
***
*** Comments:      
***   This file contain the function for returning the address of the
*** mqx initialization structure, copied into the kernel data area.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_initialization
* Returned Value   : MQX_INITIALIZATION_STRUCT *
* Comments         : 
*   This function returns the address of the MQX initialization structure
*   for this processor
*
*END*----------------------------------------------------------------------*/

MQX_INITIALIZATION_STRUCT_PTR _mqx_get_initialization
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return ((MQX_INITIALIZATION_STRUCT_PTR)&kernel_data->INIT);

} /* Endbody */

/* EOF */
