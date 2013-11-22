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
*** File: ti_setv.c
***
*** Comments:      
***   This file contains the function for setting the timer interrupt
*** vector number.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_set_timer_vector
* Returned Value   : none
* Comments         :
*    This function sets the internal timer interupt vector number, for
* use by kernel utilities and debuggers.
*
*END*----------------------------------------------------------------------*/

void _time_set_timer_vector
   (
      /* [IN] the timer interrupt vector used by the kernel */
      _mqx_uint vector
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->SYSTEM_CLOCK_INT_NUMBER = vector;

} /* Endbody */

/* EOF */
