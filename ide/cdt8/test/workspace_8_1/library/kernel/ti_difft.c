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
*** File: ti_difft.c
***
*** Comments:      
***   This file contains the function for calculating the difference
*** between two times given in ticks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_diff_ticks
* Returned Value   : _mqx_uint - MQX_OK or error code
* Comments         :
*    This function calculates the difference between two times in ticks.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _time_diff_ticks
   (
      /* [IN] the starting time */
      MQX_TICK_STRUCT_PTR end_tick_ptr,

      /* [IN] the ending time */
      MQX_TICK_STRUCT_PTR start_tick_ptr,

      /* [OUT] the difference in time */
      MQX_TICK_STRUCT_PTR diff_tick_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((end_tick_ptr == NULL) || (start_tick_ptr == NULL) || 
      (diff_tick_ptr == NULL)) 
   {
      return MQX_INVALID_PARAMETER;
   } /* Endif */
#endif

   PSP_SUB_TICKS(end_tick_ptr, start_tick_ptr, diff_tick_ptr);

   return MQX_OK;

} /* Endbody */

/* EOF */
