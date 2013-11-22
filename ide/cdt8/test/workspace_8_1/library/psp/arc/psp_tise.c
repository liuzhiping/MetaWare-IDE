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
*** File: psp_tise.c
***
*** Comments:      
***   This file contains the functions for converting ticks to hours
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_ticks_to_seconds
* Returned Value   : uint_32 - number of seconds
* Comments         :
*    This function converts ticks into seconds
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_ticks_to_seconds
   (
      /* [IN] Ticks to be converted */
      PSP_TICK_STRUCT_PTR tick_ptr,

      /* [OUT] pointer to where the overflow boolean is to be written */
      boolean _PTR_       overflow_ptr
   )
{ /* Body */
   uint_64                tmp;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   tmp = tick_ptr->TICKS[0];

   if ((tmp != MAX_UINT_64) &&
      (tick_ptr->HW_TICKS[0] > (kernel_data->HW_TICKS_PER_TICK/2)))
   {
      tmp++;
   } /* Endif */

   tmp /= kernel_data->TICKS_PER_SECOND;

   *overflow_ptr = (boolean)(tmp > MAX_UINT_32);

   return (uint_32)tmp;

} /* Endbody */


/* EOF */
