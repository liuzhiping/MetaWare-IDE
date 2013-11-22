/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: get_nsec.c
***
*** Comments:      
***   This file contains the function that reads the timer and returns
*** the number of microseconds elapsed since the last interrupt.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "bsp_prv.h"


/*FUNCTION****************************************************************
* 
* Function Name    : _time_get_nanoseconds
* Returned Value   : uint_16 microseconds
* Comments         :
*    This routine returns the number of nanoseconds that have elapsed
* since the last interrupt.
* 
*END**********************************************************************/

uint_32 _time_get_nanoseconds
   (
      void
   )
{ /* Body */
   uint_32                result = 0;
   uint_32                ticks;
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   ticks = _psp_get_aux(BSP_TCOUNT);
/* Start CR 2396 */
#if MQX_USE_PMU
   if (ticks < (kernel_data->TIMER_HW_REFERENCE)) { /* Int pending */
      result = (kernel_data->TICKS_PER_SECOND) * 1000;
      while (ticks > (kernel_data->HW_TICKS_PER_TICK)) {
         result += ((kernel_data->TICKS_PER_SECOND) * 1000);
         ticks  -= (kernel_data->HW_TICKS_PER_TICK);
      } /* Endwhile */
   } else {
      ticks -= (kernel_data->TIMER_HW_REFERENCE);
   } /* Endif */

   result += 1000000000 / (kernel_data->TICKS_PER_SECOND) * ticks / 
      (kernel_data->HW_TICKS_PER_TICK);
#else
   if (ticks < BSP_TIMER_REFERENCE(kernel_data)) { /* Int pending */
      result = BSP_ALARM_RESOLUTION * 1000;
      while (ticks > BSP_HW_TICKS_PER_TICK(kernel_data)) {
         result += (BSP_ALARM_RESOLUTION * 1000);
         ticks  -= BSP_HW_TICKS_PER_TICK(kernel_data);
      } /* Endwhile */
   } else {
      ticks -= BSP_TIMER_REFERENCE(kernel_data);
   } /* Endif */

   result += 1000000000 / BSP_ALARM_FREQUENCY * ticks / 
      BSP_HW_TICKS_PER_TICK(kernel_data);
#endif
/* End CR 2396 */
   return result;

} /* Endbody */

/* EOF */
