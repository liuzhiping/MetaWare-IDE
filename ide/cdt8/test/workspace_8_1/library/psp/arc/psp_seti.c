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
*** File: psp_seti.c
***
*** Comments:      
***   This file contains the function for converting seconds to ticks
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_seconds_to_ticks
 * Returned Value   : void
 * Comments         :
 *   This function converts seconds into ticks
 *
 *END*----------------------------------------------------------------------*/

void _psp_seconds_to_ticks
   (
       /* [IN] The number of seconds to convert */
       _mqx_uint           seconds,

       /* [OUT] Pointer to tick structure where the result will be stored */
       PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   tick_ptr->HW_TICKS[0] = 0;
   tick_ptr->TICKS[0]    = (uint_64)seconds * kernel_data->TICKS_PER_SECOND;
      
} /* Endbody */

/* EOF */
