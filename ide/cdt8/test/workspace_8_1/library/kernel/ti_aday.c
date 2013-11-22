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
*** File: ti_aday.c
***
*** Comments:      
***   This file contains the function for adding days to a tick
*** struct
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_add_day_to_ticks
 * Returned Value   : MQX_TICK_STRUCT_PTR - first parameter to function
 * Comments         :
 *   This function adds a quantity in days to a tick struct
 *
 *END*----------------------------------------------------------------------*/

MQX_TICK_STRUCT_PTR _time_add_day_to_ticks
   (
       /* [IN] The structure to add to */
       MQX_TICK_STRUCT_PTR tick_ptr,

       /* [IN] The number of days to add */
       _mqx_uint           days
   )
{ /* Body */
   MQX_TICK_STRUCT tmp;

   if (days) {
      PSP_DAYS_TO_TICKS(days, &tmp);
      PSP_ADD_TICKS(tick_ptr, &tmp, tick_ptr);
   } /* Endif */

   return tick_ptr;

} /* Endbody */

/* EOF */
