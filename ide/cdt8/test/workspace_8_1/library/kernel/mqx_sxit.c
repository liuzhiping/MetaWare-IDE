/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: mqx_sxit.c
***
*** Comments:      
***   This file contains the function for setting a MQX's exit
***   handler.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_set_exit_handler
* Returned Value   : none
* Comments         : 
*   This function stores the address of the function as the exit handler
*   to call when mqx exits.
*
*END*----------------------------------------------------------------------*/

void _mqx_set_exit_handler
   (
      /* [IN] the exit handler */
      void (_CODE_PTR_ entry)(void)
   )
{ /* Body */
#if MQX_EXIT_ENABLED
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_mqx_set_exit_handler, entry);
   kernel_data->EXIT_HANDLER = entry;
   _KLOGX1(KLOG_mqx_set_exit_handler);

#endif /* MQX_EXIT_ENABLED */
} /* Endbody */

/* EOF */
