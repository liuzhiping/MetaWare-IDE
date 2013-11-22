/*HEADER*******************************************************************
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
*** File: psp_tiol.c
***
*** Comments:      
***   This file contains the functions for converting from time to ticks.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_time_to_ticks
* Returned Value   : void
* Comments         :
*    This function converts the time struct format into ticks
*
*END*----------------------------------------------------------------------*/

void _psp_time_to_ticks
   (
      TIME_STRUCT_PTR     time_ptr,
      PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   PSP_TICK_STRUCT tick1;
   PSP_TICK_STRUCT tick2;

   _psp_seconds_to_ticks(time_ptr->SECONDS, &tick1);
   _psp_msecs_to_ticks(time_ptr->MILLISECONDS, &tick2);

   _psp_add_ticks(&tick1, &tick2, tick_ptr);

} /* Endbody */

/* EOF */
