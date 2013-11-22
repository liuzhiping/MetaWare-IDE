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
*** File: ti_elaps.c
***
*** Comments:      
***   This file contains the functions for returning the amount of
*** time since the processor started running.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get_elapsed
* Returned Value   : void
* Comments         : 
*   This function retrieves the number of seconds and milliseconds since
*   the processor started.  (without any time offset information)
*
*END*----------------------------------------------------------------------*/

void _time_get_elapsed
   (
      /* [IN/OUT] the address where the time is to be put */
      TIME_STRUCT_PTR time_ptr
   )
{ /* Body */
   MQX_TICK_STRUCT    tick;

#if MQX_CHECK_ERRORS
   if ( time_ptr == NULL ) {
      return;
   } /* Endif */
#endif

   _time_get_elapsed_ticks(&tick);

   PSP_TICKS_TO_TIME(&tick, time_ptr);

} /* Endbody */

/* EOF */
