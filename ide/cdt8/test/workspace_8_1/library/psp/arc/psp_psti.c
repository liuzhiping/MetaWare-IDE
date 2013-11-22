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
*** File: psp_psti.c
***
*** Comments:      
***   This file contains the function for converting picoseconds to a ticks
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_psecs_to_ticks
 * Returned Value   : void
 * Comments         :
 *   This function converts picoseconds into ticks. Note, there is no way to
 * represent MAX_UINT_16 picoseconds in terms of ticks.
 *
 *END*----------------------------------------------------------------------*/

void _psp_psecs_to_ticks
   (
       /* [IN] The number of picoseconds to convert */
       _mqx_uint           psecs,

       /* [OUT] Pointer to tick structure where the result will be stored */
       PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   PSP_128_BIT_UNION      tmp;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

#if PSP_ENDIAN == MQX_LITTLE_ENDIAN
   tmp.LLW[1] = 0;
   tmp.LLW[0] = (uint_64)psecs * kernel_data->TICKS_PER_SECOND;
   tick_ptr->TICKS[0] = tmp.LLW[0] / (1000ULL * 1000 * 1000 * 1000);

   /* Calculate the remaining picoticks */
   tmp.LLW[0] %= (1000ULL * 1000 * 1000 * 1000);
#else
   tmp.LLW[0] = 0;
   tmp.LLW[1] = (uint_64)psecs * kernel_data->TICKS_PER_SECOND;
   tick_ptr->TICKS[0] = tmp.LLW[1] / (1000ULL * 1000 * 1000 * 1000);

   /* Calculate the remaining picoticks */
   tmp.LLW[1] %= (1000ULL * 1000 * 1000 * 1000);
#endif

   /* Convert to hardware ticks */

   _psp_mul_128_by_32(&tmp, kernel_data->HW_TICKS_PER_TICK, &tmp);
   
   _psp_div_128_by_32(&tmp, 1000UL * 1000 * 1000, &tmp);
   _psp_div_128_by_32(&tmp, 1000, &tmp);

#if PSP_ENDIAN == MQX_LITTLE_ENDIAN
   tick_ptr->HW_TICKS[0] = tmp.LW[0];
#else
   tick_ptr->HW_TICKS[0] = tmp.LW[3];
#endif

} /* Endbody */
/* EOF */
