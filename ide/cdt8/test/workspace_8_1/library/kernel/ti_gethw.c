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
*** File: ti_gethw.c
***
*** Comments:      
***   This file contains the function for returning the number of hardware
*** ticks since the last tick.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get_hwticks
* Returned Value   : uint_32 - hardware ticks
* Comments         :
*    This function returns the number of hardware ticks since the last tick.
*
*END*----------------------------------------------------------------------*/

uint_32 _time_get_hwticks
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   if (kernel_data->GET_HWTICKS) {
      return (*kernel_data->GET_HWTICKS)(kernel_data->GET_HWTICKS_PARAM);
   } /* Endif */

   return 0;
 
} /* Endbody */

/* EOF */
