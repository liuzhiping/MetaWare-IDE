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
*** File: mqx_gcpu.c
***
*** Comments:      
***   This file contains the function for returning the cpu type
***   in the kernel data structure.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_cpu_type
* Returned Value   : _mqx_uint cpu_type
* Comments         : 
*   This function retrieves the type of CPU in the CPU_TYPE field
*   of the kernel data.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mqx_get_cpu_type
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return kernel_data->CPU_TYPE;

} /* Endbody */

/* EOF */
