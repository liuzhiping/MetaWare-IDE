/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2006 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ti_gett.c
***
*** Comments:      
***   This file contains the function for returning the current time in ticks.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_get_ticks
* Returned Value   : void
* Comments         :
*    This function returns a tick structure filled in with the current time.
*
*   ***  Keep in sync with ti_elapt.c  ***
*
*END*----------------------------------------------------------------------*/

void _time_get_ticks
   (
      /* [IN/OUT] the address where the time is to be written */
      register MQX_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

#if MQX_CHECK_ERRORS
   if ( tick_ptr == NULL ) {
      return;
   } /* Endif */
#endif

   _GET_KERNEL_DATA(kernel_data);

   _INT_DISABLE();
      
   /* Start CR 2068 */
   /*PSP_ADD_TICKS(&kernel_data->TIME, &kernel_data->TIME_OFFSET, tick_ptr);*/
   *tick_ptr = kernel_data->TIME;

   // The hardware clock keeps counting...

   if (kernel_data->GET_HWTICKS) {
      // The hardware clock may have counted passed it's reference
      // and have an interrupt pending.  Thus, HW_TICKS may exceed
      // kernel_data->HW_TICKS_PER_TICK and this tick_ptr may need
      // normalizing.  This is done in a moment.
      tick_ptr->HW_TICKS = (*kernel_data->GET_HWTICKS)
         (kernel_data->GET_HWTICKS_PARAM);
   } /* Endif */
   
   PSP_ADD_TICKS(tick_ptr, &kernel_data->TIME_OFFSET, tick_ptr);
   /* End CR 2068 */

   _INT_ENABLE(); // The timer ISR may go off and increment kernel_data->TIME

   // The tick_ptr->HW_TICKS value might exceed the
   // kernel_data->HW_TICKS_PER_TICK and need to be
   // normalized for the PSP.
   PSP_NORMALIZE_TICKS(tick_ptr);

} /* Endbody */

/* EOF */