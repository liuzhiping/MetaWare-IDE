/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: psp_tisu.c
***
*** Comments:      
***   This file contains the function for subtracting two tick structs
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_normalize_ticks
 * Returned Value   : none
 * Comments         : Normalizes ticks and partial ticks in a tick structure
 *
 *END*----------------------------------------------------------------------*/

void _psp_normalize_ticks
   (
       /* [IN/OUT] Tick structure to be normalized */
       PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   register uint_32        ticks_per_tick;

   _GET_KERNEL_DATA(kernel_data);

   ticks_per_tick = kernel_data->HW_TICKS_PER_TICK;

   if (tick_ptr->HW_TICKS[0] >= ticks_per_tick) {
      register uint_32 t = tick_ptr->HW_TICKS[0] / ticks_per_tick;
      tick_ptr->TICKS[0] += t;
      tick_ptr->HW_TICKS[0] -= t * ticks_per_tick;
   } /* Endif */

} /* Endbody */

/* EOF */
