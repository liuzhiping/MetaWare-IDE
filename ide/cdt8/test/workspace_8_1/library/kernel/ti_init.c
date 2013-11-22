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
*** File: ti_init.c
***
*** Comments:      
***   This file contains the function for initializing a tick struct with
*** a quantity of ticks
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _time_init_ticks
 * Returned Value   : _mqx_uint - MQX_OK or error code
 * Comments         :
 *   This function initializes a tick struct
 *
 *END*----------------------------------------------------------------------*/

_mqx_uint _time_init_ticks
   (
       /* [IN/OUT] The structure to initialize */
       MQX_TICK_STRUCT_PTR tick_ptr,

       /* [IN] The number of ticks to initialize with */
       _mqx_uint           ticks
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if (tick_ptr == NULL) {
      return MQX_INVALID_PARAMETER;
   } /* Endif */
#endif

   if (ticks) {
      /* Start CR 195 */
      tick_ptr->HW_TICKS = 0;
      /* End CR 195 */
      PSP_ADD_TICKS_TO_TICK_STRUCT(&_mqx_zero_tick_struct, ticks, tick_ptr);
   } else {
      *tick_ptr = _mqx_zero_tick_struct;
   } /* Endif */

   return MQX_OK;

} /* Endbody */

/* EOF */
