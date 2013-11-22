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
*** File: mqx_scpu.c
***
*** Comments:      
***   This file contains the function for setting the cpu type 
***   in the kernel data structure.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_set_cpu_type
* Returned Value   : none
* Comments         : 
*   This function stores the type of CPU in the CPU_TYPE field
*   of the kernel data.
*
*END*----------------------------------------------------------------------*/

void _mqx_set_cpu_type
   (
      /* [IN] the value representing the kernel CPU type.
      ** this is used by the debugger
      */
      _mqx_uint cpu_type
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->CPU_TYPE = cpu_type;

} /* Endbody */

/* EOF */
