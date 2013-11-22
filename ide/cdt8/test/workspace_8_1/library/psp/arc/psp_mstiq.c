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
*** File: psp_mstiq.c
***
*** Comments:      
***   This file contains the function for converting milliseconds to a ticks
*** ignoring the Hardware Ticks
***                                                               
**************************************************************************
*END*********************************************************************/

#ifdef __MET__
#include <stdlib.h>
#endif
#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_msecs_to_ticks_quick
 * Returned Value   : void
 * Comments         :
 *   This function converts milliseconds into ticks without HW ticks
 *
 *END*----------------------------------------------------------------------*/

void _psp_msecs_to_ticks_quick
   (
       /* [IN] The number of milliseconds to convert */
       _mqx_uint           msecs,

       /* [OUT] Pointer to tick structure where the result will be stored */
       PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   uint_32                ms_per_tick;

   _GET_KERNEL_DATA(kernel_data);

   ms_per_tick = 1000 / kernel_data->TICKS_PER_SECOND;
   if ((ms_per_tick * kernel_data->TICKS_PER_SECOND) == 1000) {
      /* Perform fast calculation */
      tick_ptr->TICKS[0]    = msecs / ms_per_tick;
      tick_ptr->HW_TICKS[0] = 0;
   } else {
      tick_ptr->TICKS[0] = ((uint_64)msecs * kernel_data->TICKS_PER_SECOND) /
         1000;
      tick_ptr->HW_TICKS[0] = 0;
   }/* Endif */

} /* Endbody */

/* EOF */

