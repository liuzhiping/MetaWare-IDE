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
*** File: psp_gelms.c
***
*** Comments:      
***   This file contains the functions for obtaining the elapsed milliseconds
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_get_milliseconds_elapsed
* Returned Value   : uint_32 - number of milliseconds
* Comments         :
*    This function obtains current elapsed milliseconds
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_get_elapsed_milliseconds
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   /* Start CR 764 */
   PSP_SUPPORT_STRUCT_PTR support_ptr;
   /* End   CR 764 */
   uint_64                ticks;
   uint_32                ms_per_tick;
   uint_32                ms;

   _GET_KERNEL_DATA(kernel_data);

   ticks = ((PSP_TICK_STRUCT_PTR)&kernel_data->TIME)->TICKS[0];
   /* Start CR 764 */
   /* ms_per_tick = 1000 / kernel_data->TICKS_PER_SECOND; */
   support_ptr = (PSP_SUPPORT_STRUCT_PTR)kernel_data->PSP_SUPPORT_PTR;
   ms_per_tick = support_ptr->MS_PER_TICK;
   /* if ((ms_per_tick * kernel_data->TICKS_PER_SECOND) == 1000) { */
   if (support_ptr->MS_PER_TICK_IS_INT) {
   /* End   CR 764 */
      ms = (uint_32)(ticks * ms_per_tick);
   } else {
      ms = (uint_32)((ticks * 1000) / kernel_data->TICKS_PER_SECOND);
   } /* Endif */
   return(ms);
   
} /* Endbody */

/* EOF */
