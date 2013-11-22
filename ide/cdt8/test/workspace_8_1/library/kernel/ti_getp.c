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
*** File: ti_getp.c
***
*** Comments:      
***   This file contains the function for returing the current timer tick
*** period.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get_ticks_per_sec
* Returned Value   : _mqx_uint period of clock interrupt in ticks per second
* Comments         :
*    This function returns the period of the clock interrupt
*
*END*----------------------------------------------------------------------*/

_mqx_uint _time_get_ticks_per_sec
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return( kernel_data->TICKS_PER_SECOND );
 
} /* Endbody */

/* EOF */
