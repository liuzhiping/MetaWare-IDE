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
*** File: ti_sett.c
***
*** Comments:      
***   This file contains the function for setting the timer period
*** period in ticks per second
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_ticks_per_sec
* Returned Value   : void
* Comments         :
*    This function sets the period of the clock interrupt.
*    THIS MUST AGREE WITH THE ACTUAL INTERRUPT PERIOD, or all _time_delays
*    WILL CHANGE.
*
*END*----------------------------------------------------------------------*/

void _time_set_ticks_per_sec
   (
      /* [IN] the timer period in ticks per second to be used by the kernel */
      _mqx_uint ticks_per_sec
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   kernel_data->TICKS_PER_SECOND = ticks_per_sec;

} /* Endbody */

/* EOF */
