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
*** File: psp_tiad.c
***
*** Comments:      
***   This file contains the function for adding two tick structs
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_add_ticks
 * Returned Value   : none
 * Comments         : Adds two PSP tick structure together, including hardware
 *   ticks
 *
 *END*----------------------------------------------------------------------*/

void _psp_add_ticks
   (
       /* [IN] The two structures to add - both must be normalized */
       PSP_TICK_STRUCT_PTR a_ptr,
       PSP_TICK_STRUCT_PTR b_ptr,

       /* [OUT] The result of the addition */
       PSP_TICK_STRUCT_PTR r_ptr
   )
{ /* Body */
   register uint_32       a_hw_ticks;
   register uint_32       b_hw_ticks;
   register uint_32       hwtpt;

   r_ptr->TICKS[0] = a_ptr->TICKS[0] + b_ptr->TICKS[0];

   a_hw_ticks  = a_ptr->HW_TICKS[0];
   b_hw_ticks  = b_ptr->HW_TICKS[0];

   hwtpt = _mqx_kernel_data->HW_TICKS_PER_TICK;

   if ( a_hw_ticks >= (hwtpt - b_hw_ticks)) {
      r_ptr->TICKS[0]++;
      r_ptr->HW_TICKS[0] = a_hw_ticks + (b_hw_ticks - hwtpt);
   } else {
      r_ptr->HW_TICKS[0] = a_hw_ticks + b_hw_ticks;
   } /* Endif */

} /* Endbody */

/* EOF */

