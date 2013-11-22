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
*** File: psp_timn.c
***
*** Comments:      
***   This file contains the functions for converting ticks to minutes
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_ticks_to_minutes
* Returned Value   : uint_32 - number of minutes
* Comments         :
*    This function converts ticks into minutes
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_ticks_to_minutes
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

   tmp = (tmp / kernel_data->TICKS_PER_SECOND) / SECS_IN_MINUTE;
  
   *overflow_ptr = (boolean)(tmp > MAX_UINT_32);

   return (uint_32)tmp;

} /* Endbody */

/* EOF */
