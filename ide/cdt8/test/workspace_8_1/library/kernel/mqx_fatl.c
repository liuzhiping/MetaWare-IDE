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
*** File: mqx_fatl.c
***
*** Comments:      
***   This file contains the function called when a fatal error has
*** been detected in the mqx kernel, and mqx can no longer function.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_fatal_error
* Returned Value   : none
* Comments         : 
*   This function is called by the mqx kernel when an error has occured
* that is severe enough that the kernel can no longer function properly.
*
*END*----------------------------------------------------------------------*/

void _mqx_fatal_error
   (
      /* [IN] the error code */
      _mqx_uint error
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE2(KLOG_mqx_fatal_error, error);
   _mqx_exit(error);
   _KLOGX1(KLOG_mqx_fatal_error);

} /* Endbody */

/* EOF */
