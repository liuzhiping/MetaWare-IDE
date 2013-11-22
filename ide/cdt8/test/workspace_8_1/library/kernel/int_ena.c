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
*** File: int_ena.c
***
*** Comments:      
***   This file contains the function that enables interrupts.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_enable
* Returned Value   : none
* Comments         :
*    This function enables all interrupts for this task.
*
*END*-----------------------------------------------------------------------*/

void _int_enable
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   _INT_ENABLE_CODE();

} /* Endbody */

/* EOF */
