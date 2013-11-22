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
*** File: ti_difus.c
***
*** Comments:      
***   This file contains the function for calculating the difference in
*** microseconds between two times given in ticks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_diff_microseconds
* Returned Value   : _mqx_int - The result
* Comments         :
*    This function calculates the difference between two times in ticks.
*
*END*----------------------------------------------------------------------*/

int_32 _time_diff_microseconds
   (
      /* [IN] the starting time */
      MQX_TICK_STRUCT_PTR end_tick_ptr,

      /* [IN] the ending time */
      MQX_TICK_STRUCT_PTR start_tick_ptr,

      /* [OUT] Set to TRUE if overflow occurs */
      boolean _PTR_       overflow_ptr
   )
{ /* Body */
   MQX_TICK_STRUCT    diff_tick;

   _time_diff_ticks(end_tick_ptr, start_tick_ptr, &diff_tick);

   return( PSP_TICKS_TO_MICROSECONDS(&diff_tick, overflow_ptr) );

} /* Endbody */

/* EOF */
