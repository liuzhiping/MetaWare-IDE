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
*** File: ti_ansec.c
***
*** Comments:      
***   This file contains the function for adding nanoseconds to a tick
*** struct
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_add_nsec_to_ticks
 * Returned Value   : MQX_TICK_STRUCT_PTR - first parameter to function
 * Comments         :
 *   This function adds a quantity in nanoseconds to a tick struct
 *
 *END*----------------------------------------------------------------------*/

MQX_TICK_STRUCT_PTR _time_add_nsec_to_ticks
   (
       /* [IN] The structure to add to */
       MQX_TICK_STRUCT_PTR tick_ptr,

       /* [IN] The number of nanoseconds to add */
       _mqx_uint           nsecs
   )
{ /* Body */
   MQX_TICK_STRUCT tmp;

   if (nsecs) {
      PSP_NANOSECONDS_TO_TICKS(nsecs, &tmp);
      PSP_ADD_TICKS(tick_ptr, &tmp, tick_ptr);
   } /* Endif */

   return tick_ptr;

} /* Endbody */

/* EOF */
