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
*** File: ti_get.c
***
*** Comments:      
***   This file contains the function for returning the current time.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get
* Returned Value   : void
* Comments         :
*    This function returns a time structure filled in with the current time.
*
*END*----------------------------------------------------------------------*/

void _time_get
   (
      /* [IN/OUT] the address where the time is to be written */
      register TIME_STRUCT_PTR time_ptr
   )
{ /* Body */

   MQX_TICK_STRUCT ticks;

   _time_get_ticks(&ticks);

   PSP_TICKS_TO_TIME(&ticks, time_ptr);

} /* Endbody */

/* EOF */
