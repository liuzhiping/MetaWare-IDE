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
*** File: ti_shwtt.c
***
*** Comments:      
***   This file contains the function for setting the number of hardware
*** ticks in a tick.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_hwticks_per_tick
* Returned Value   : none
* Comments         :
*    This function set the number of hardware ticks per tick
*
*END*----------------------------------------------------------------------*/

void _time_set_hwticks_per_tick
   (
      uint_32 new
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   kernel_data->HW_TICKS_PER_TICK = new;

} /* Endbody */

/* EOF */
