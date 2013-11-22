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
*** File: ti_getr.c
***
*** Comments:      
***   This file contains the function for returning the current timer tick
*** resolution.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get_resolution
* Returned Value   : _mqx_uint frequency of clock interrupt in ticks per second
* Comments         :
*    Returns the frequency of the clock interrupt
*
*END*----------------------------------------------------------------------*/

_mqx_uint _time_get_resolution
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   result = kernel_data->TICKS_PER_SECOND;

   return( MILLISECS_IN_SECOND / result );
 
} /* Endbody */

/* EOF */
