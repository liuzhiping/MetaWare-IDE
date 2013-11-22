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
*** File: psp_usti.c
***
*** Comments:      
***   This file contains the function for converting microseconds to a ticks
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_usecs_to_ticks
 * Returned Value   : void
 * Comments         :
 *   This function converts microseconds into ticks
 *
 *END*----------------------------------------------------------------------*/

void _psp_usecs_to_ticks
   (
       /* [IN] The number of microseconds to convert */
       _mqx_uint           usecs,

       /* [OUT] Pointer to tick structure where the result will be stored */
       PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   uint_64                tmp;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   tmp = (uint_64)usecs * kernel_data->TICKS_PER_SECOND;
   tick_ptr->TICKS[0] = tmp / (1000 * 1000);

   /* Calculate the remaining microticks */
   tmp %= (1000 * 1000);

   /* Convert to hardware ticks */
   tmp = (tmp * kernel_data->HW_TICKS_PER_TICK + 500000) / (1000 * 1000);

   tick_ptr->HW_TICKS[0] = (uint_32)tmp;

} /* Endbody */

/* EOF */
