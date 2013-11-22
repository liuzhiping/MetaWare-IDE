/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: mqx_gxit.c
***
*** Comments:      
***   This file contains the functions for returning the mqx exit
*** handler function.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_exit_handler
* Returned Value   : pointer entry
* Comments         : 
*   This function returns the address of the mqx exit handler function,
* called when mqx exits.
*
*END*----------------------------------------------------------------------*/
/* START CR 2361 */
#if MQX_EXIT_ENABLED
void (_CODE_PTR_ _mqx_get_exit_handler
   (
      void
   ))(void)
{ /* Body */

   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return (kernel_data->EXIT_HANDLER);

} /* Endbody */
#endif /* MQX_EXIT_ENABLED */
/* END CR 2361 */
/* EOF */
