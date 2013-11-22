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
*** File: ti_setr.c
***
*** Comments:      
***   This file contains the function for setting the timer interrupt
*** tick resolution (in ms)
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_resolution
* Returned Value   : _mqx_uint Task error code
* Comments         :
*    This function sets the period of the clock interrupt.
*    THIS MUST AGREE WITH THE ACTUAL INTERRUPT PERIOD, or all _time_delays
*    WILL CHANGE.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _time_set_resolution
   (
      /* [IN] the alarm resolution in milliseconds to be used by the kernel */
      _mqx_uint resolution
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   /* 
   ** Convert resolution into ticks per second so new tick format will
   ** work 
   */
   kernel_data->TICKS_PER_SECOND = 1000 / resolution;

   /* Also set hw ticks per tick */
   kernel_data->HW_TICKS_PER_TICK = resolution * 1000;

   kernel_data->GET_HWTICKS = (uint_32 (_CODE_PTR_)(pointer))
      _time_get_microseconds;

   return MQX_OK;

} /* Endbody */

/* EOF */
