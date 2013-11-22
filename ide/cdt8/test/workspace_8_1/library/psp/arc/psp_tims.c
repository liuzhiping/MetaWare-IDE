/*HEADER*******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: psp_tims.c
***
*** Comments:      
***   This file contains the functions for converting ticks to milliseconds
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_ticks_to_milliseconds
* Returned Value   : uint_32 - number of milliseconds
* Comments         :
*    This function converts ticks into milliseconds
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_ticks_to_milliseconds
   (
      /* [IN] Ticks to be converted */
      PSP_TICK_STRUCT_PTR tick_ptr,

      /* [OUT] pointer to where the overflow boolean is to be written */
      boolean _PTR_       overflow_ptr
   )
{ /* Body */
   PSP_128_BIT_UNION      tmp;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

#if (PSP_ENDIAN == MQX_BIG_ENDIAN)
   tmp.LLW[0] = 0;
   tmp.LLW[1] = tick_ptr->TICKS[0];
#else
   tmp.LLW[1] = 0;
   tmp.LLW[0] = tick_ptr->TICKS[0];
#endif

   /* Convert ticks to hardware ticks */
   _psp_mul_128_by_32(&tmp, kernel_data->HW_TICKS_PER_TICK, &tmp);

   /* Add in hardware ticks */
   /* START CR 2364 */
   _psp_add_element_to_array(&tmp, tick_ptr->HW_TICKS[0], 
      sizeof(PSP_128_BIT_UNION) / sizeof(uint_32), &tmp);
   /* END CR 2364 */


   /* 
   ** Convert hardware ticks to ms. (H / (T/S * H/T) * 1000)
   ** Multiply by an extra 10 for rounding purposes.
   */
   _psp_mul_128_by_32(&tmp, 10000, &tmp);
   _psp_div_128_by_32(&tmp, kernel_data->TICKS_PER_SECOND, &tmp);
   _psp_div_128_by_32(&tmp, kernel_data->HW_TICKS_PER_TICK, &tmp);

/* START CR 2364 */
   _psp_add_element_to_array(&tmp, 5, 
      sizeof(PSP_128_BIT_UNION) / sizeof(uint_32), &tmp);
/* END CR 2364 */

   _psp_div_128_by_32(&tmp, 10, &tmp);

#if (PSP_ENDIAN == MQX_BIG_ENDIAN)
   *overflow_ptr = (boolean)(tmp.LLW[0] || tmp.LW[2]);
   return tmp.LW[3];
#else
   *overflow_ptr = (boolean)(tmp.LLW[1] || tmp.LW[1]);
   return tmp.LW[0];
#endif

} /* Endbody */

/* EOF */