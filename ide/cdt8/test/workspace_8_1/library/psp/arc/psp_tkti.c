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
*** File: psp_tkti.c
***
*** Comments:      
***   This file contains the functions for converting between ticks and
***  the TIME_STRUCT format.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_ticks_to_time
* Returned Value   : boolean - Returns FALSE is overflow occurs
* Comments         :
*    This function converts ticks into the time struct format
*
*END*----------------------------------------------------------------------*/

boolean _psp_ticks_to_time
   (
      /* [IN] Pointer to the tick struct to store the results in */
      PSP_TICK_STRUCT_PTR tick_ptr,

      /* [OUT] Pointer to the time struct to convert */
      TIME_STRUCT_PTR     time_ptr
   )
{ /* Body */
   uint_64                tmp;
   uint_32                tps;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   tps = kernel_data->TICKS_PER_SECOND;

   /* Saturate if ticks go out of range of time struct */
   if ( (tick_ptr->TICKS[0] / tps) > MAX_UINT_32) {
      time_ptr->SECONDS      = MAX_UINT_32;
      time_ptr->MILLISECONDS = 999;
      return FALSE;
   } /* Endif */

   /* This is guaranteed to work since ticks/tps is less than MAX_UINT_32 */
   tmp = (tick_ptr->TICKS[0] * 1000) / tps;

   time_ptr->SECONDS      = tmp / 1000;
   time_ptr->MILLISECONDS = tmp - time_ptr->SECONDS * 1000;

   /* Add in hardware ticks */
   tmp =  (uint_64)tick_ptr->HW_TICKS[0] * 1000 * 2;
   tmp =  tmp / tps / kernel_data->HW_TICKS_PER_TICK;
   tmp += 1;
   tmp >>= 1;

   time_ptr->MILLISECONDS += (uint_32)tmp;

   return TRUE;

} /* Endbody */

/* EOF */
