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
*** File: int_disa.c
***
*** Comments:      
***   This file contains the function for disabling interrupts.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_disable
* Returned Value   : none
* Comments         :
*    This function disables all interrupts for this task.
*
*END*-----------------------------------------------------------------------*/

void _int_disable
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   _INT_DISABLE_CODE();

} /* Endbody */

/* EOF */
