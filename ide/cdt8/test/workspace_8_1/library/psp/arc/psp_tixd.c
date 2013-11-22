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
*** File: psp_tixd.c
***
*** Comments:      
***   This file contains the functions for converting between ticks
*** and the xdate structure.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_ticks_to_xdate
* Returned Value   : boolean - Returns FALSE if ticks are out of range of 
*                    the xdate structure, TRUE otherwise
* Comments         :
*    This function converts ticks into the xdate structure
*
*END*----------------------------------------------------------------------*/

boolean _psp_ticks_to_xdate
   (
      /*  [IN]  pointer to tick structure  */
      PSP_TICK_STRUCT_PTR  tick_ptr,

      /*  [OUT]  pointer to a xdate structure  */
      MQX_XDATE_STRUCT_PTR xdate_ptr
   )
{ /* Body */
   PSP_TICK_STRUCT        tmp_ticks1;

/* Start CR 2129 2365 */
/*   PSP_TICK_STRUCT        tmp_ticks2;
     boolean                over_flow; */
/* END CR 2129 2365 */



   uint_64                seconds;
   uint_64                leftover_ticks;
   KERNEL_DATA_STRUCT_PTR kernel_data;
   uint_32                days,years;
   uint_32                tps, tmp;
   uint_16                leap;
   /* START CR 2129 */
   uint_64                hwticks;
   uint_64                tmp64, tmp64_2;
   uint_32                ms_per_tick, us_per_tick, ns_per_tick, ps_per_tick;
   
   
   _GET_KERNEL_DATA(kernel_data);

   PSP_NORMALIZE_TICKS(tick_ptr);
   /* END CR 2129 */

   tps = kernel_data->TICKS_PER_SECOND;

   /* Convert ticks to seconds */
   seconds        = tick_ptr->TICKS[0] / tps;
   leftover_ticks = tick_ptr->TICKS[0] % tps;
      
   /* Convert to minutes */
   tmp = seconds / SECS_IN_MINUTE;
   xdate_ptr->SEC = seconds % SECS_IN_MINUTE;

   /* Convert to hours */
   xdate_ptr->MIN = tmp % MINUTES_IN_HOUR;
   tmp /= MINUTES_IN_HOUR;

   /* Convert to days */
   xdate_ptr->HOUR = tmp % HOURS_IN_DAY;
   days = tmp / HOURS_IN_DAY;


   /* Make an educated quess where to start in table */
   years = days / 366;
   if (years > 511) {
      /* This is out of range of the xdate structure */
      return FALSE;
   } /* Endif */

   xdate_ptr->YEAR = (uint_16)years;
   while (days >= _time_days_before_year_internal[xdate_ptr->YEAR + 1]) {
      xdate_ptr->YEAR++;
   } /* Endwhile */

   /* Jan 1, 1970 was a Thursday */
   xdate_ptr->WDAY  = (uint_16)((days + 3 + 1) % 7);

   /* Calculate the number of days since the beginning of the year */
   days -= _time_days_before_year_internal[xdate_ptr->YEAR];

   xdate_ptr->YEAR += (uint_16)1970;
   xdate_ptr->YDAY  = (uint_16)days;

   /*
   ** Find out if we are in a leap year.
   **
   ** if (the year is a century year not divisible by 400
   ** then it is not a leap year, otherwise if year divisible by
   ** four then it is a leap year
   */

   if ((xdate_ptr->YEAR % (uint_16)100) == (uint_16)0) {

      if ((xdate_ptr->YEAR % (uint_16)400) == (uint_16)0) {
         leap = (uint_16)LEAP_YEAR;
      } else {
         leap = (uint_16)NON_LEAP_YEAR;
      } /* Endif */
         
   } else {

      if ((xdate_ptr->YEAR % (uint_16)4) == (uint_16)0) {
         leap = (uint_16)LEAP_YEAR;
      } else {
         leap = (uint_16)NON_LEAP_YEAR;
      } /* Endif */

   } /* Endif */

   /* calculate the month */
   xdate_ptr->MONTH = days/31;
   while (days >= _time_days_before_month_internal[leap][xdate_ptr->MONTH]) {
      xdate_ptr->MONTH++;
   } /* Endwhile */
   days -= _time_days_before_month_internal[leap][xdate_ptr->MONTH - 1];

   /* calculate the day */
   xdate_ptr->MDAY = (uint_16)(days);

   /* first day is 1*/
   xdate_ptr->MDAY++;

   /* 
   ** Calculate ms, us, ns and ps from remaining ticks
   */

   /* START CR 2129 */
   /* 
   ** Here we do all the calculations manually, even though there are APIs for these
   ** conversions.  This is to avoid losing some time due to roundings that occur in each
   ** function.  By doing each conversion here, any leftovers can be passed to the next
   ** smallest time unit. 
   */
   tmp_ticks1.TICKS[0]    = leftover_ticks;
   tmp_ticks1.HW_TICKS[0] = tick_ptr->HW_TICKS[0];

   /* Calculate milliseconds */
   /*
   ** tmp = _psp_ticks_to_milliseconds(&tmp_ticks1, &over_flow);
   ** xdate_ptr->MSEC = (uint_16)tmp;

   ** _psp_msecs_to_ticks(tmp, &tmp_ticks2);
   ** PSP_SUB_TICKS(&tmp_ticks1, &tmp_ticks2, &tmp_ticks1);

   ** if ((tmp_ticks1.TICKS[0] == 0) && (tmp_ticks2.HW_TICKS[0] == 0)) {
   */

   /* Convert to hardware ticks */
   hwticks = ((uint_64)kernel_data->HW_TICKS_PER_TICK * tmp_ticks1.TICKS[0]) +
      tmp_ticks1.HW_TICKS[0];

   /* 
   ** Convert hardware ticks to ms. (H / (T/S * H/T) * 1000)
   */
   tmp64 = hwticks;
   ms_per_tick = 1000 / kernel_data->TICKS_PER_SECOND;
   if ((ms_per_tick * kernel_data->TICKS_PER_SECOND) == 1000) {
      /* Integral ms per tick, can do a multiply instead of divide and keep more accuracy */
      tmp64 = tmp64 * ms_per_tick;
   } else {
      tmp64 = (tmp64 * 1000);
      tmp64 = tmp64 / kernel_data->TICKS_PER_SECOND;
   }/* Endif */
   tmp64 = tmp64 / kernel_data->HW_TICKS_PER_TICK;

   xdate_ptr->MSEC = (uint_16)tmp64;
   
   /* Convert back to hwticks to handle round off */
   tmp64_2 = tmp64 * kernel_data->TICKS_PER_SECOND * kernel_data->HW_TICKS_PER_TICK / 1000;

   /* Find remaining hwticks for next calculation */   
   hwticks = hwticks - tmp64_2;
   if (hwticks == 0) {
      xdate_ptr->USEC = xdate_ptr->NSEC = xdate_ptr->PSEC = 0;
      return TRUE;
   } /* Endif */

   /* Calculate microseconds */
   /* 
   ** tmp = _psp_ticks_to_microseconds(&tmp_ticks1, &over_flow);
   ** xdate_ptr->USEC = (uint_16)tmp;

   ** _psp_usecs_to_ticks(tmp, &tmp_ticks2);
   ** PSP_SUB_TICKS(&tmp_ticks1, &tmp_ticks2, &tmp_ticks1);

   ** if ((tmp_ticks1.TICKS[0] == 0) && (tmp_ticks2.HW_TICKS[0] == 0)) {
   */
   /* 
   ** Convert hardware ticks to us. (H / (T/S * H/T) * 1000000)
   */
   tmp64 = hwticks;
   us_per_tick = 1000000 / kernel_data->TICKS_PER_SECOND;
   if ((us_per_tick * kernel_data->TICKS_PER_SECOND) == 1000000) {
      /* Integral ms per tick, can do a multiply instead of divide and keep more accuracy */
      tmp64 = tmp64 * us_per_tick;
   } else {
      tmp64 = (tmp64 * 1000000);
      tmp64 = tmp64 / kernel_data->TICKS_PER_SECOND;
   }/* Endif */
   tmp64 = tmp64 / kernel_data->HW_TICKS_PER_TICK;

   xdate_ptr->USEC = (uint_16)tmp64;
   
   /* Convert back to hwticks to handle round off */
   tmp64_2 = tmp64 * kernel_data->TICKS_PER_SECOND * kernel_data->HW_TICKS_PER_TICK / 1000000;

   /* Find remaining hwticks for next calculation */   
   hwticks = hwticks - tmp64_2;
   if (hwticks == 0) {
      /* Conversion complete */
      xdate_ptr->NSEC = xdate_ptr->PSEC = 0;
      return TRUE;
   } /* Endif */

   /* Calculate nanoseconds */
   /* 
   ** tmp = _psp_ticks_to_nanoseconds(&tmp_ticks1, &over_flow);
   ** xdate_ptr->NSEC = (uint_16)tmp;

   ** _psp_nsecs_to_ticks(tmp, &tmp_ticks2);
   ** PSP_SUB_TICKS(&tmp_ticks1, &tmp_ticks2, &tmp_ticks1);

   ** if ((tmp_ticks1.TICKS[0] == 0) && (tmp_ticks2.HW_TICKS[0] == 0)) {
   */

   /* 
   ** Convert hardware ticks to ns. (H / (T/S * H/T) * 1000000000)
   */
   tmp64 = hwticks;
   ns_per_tick = 1000000000 / kernel_data->TICKS_PER_SECOND;
   if ((ns_per_tick * kernel_data->TICKS_PER_SECOND) == 1000000000) {
      /* Integral ms per tick, can do a multiply instead of divide and keep more accuracy */
      tmp64 = tmp64 * ns_per_tick;
   } else {
      tmp64 = (tmp64 * 1000000000);
      tmp64 = tmp64 / kernel_data->TICKS_PER_SECOND;
   }/* Endif */
   tmp64 = tmp64 / kernel_data->HW_TICKS_PER_TICK;

   xdate_ptr->NSEC = (uint_16)tmp64;
   
   /* Convert back to hwticks to handle round off */
   tmp64_2 = tmp64 * kernel_data->TICKS_PER_SECOND * kernel_data->HW_TICKS_PER_TICK / 1000000000;

   /* Find remaining hwticks for next calculation */   
   hwticks = hwticks - tmp64_2;
   if (hwticks == 0) {
      /* Conversion complete */
      xdate_ptr->PSEC = 0;
      return TRUE;
   } /* Endif */

   /* Calculate picoseconds */
   /* 
   ** tmp = _psp_ticks_to_microseconds(&tmp_ticks1, &over_flow);
   ** xdate_ptr->PSEC = (uint_16)tmp;
   */
   
   /* 
   ** Convert hardware ticks to ps. (H / (T/S * H/T) * 1000000000000)
   */
   tmp64 = hwticks;
   ps_per_tick = 1000000000000ULL / kernel_data->TICKS_PER_SECOND;
   if ((ps_per_tick * kernel_data->TICKS_PER_SECOND) == 1000000000000ULL) {
      /* Integral ms per tick, can do a multiply instead of divide and keep more accuracy */
      tmp64 = tmp64 * ns_per_tick;
   } else {
      tmp64 = (tmp64 * 1000000000000ULL);
      tmp64 = tmp64 / kernel_data->TICKS_PER_SECOND;
   }/* Endif */
   tmp64 = tmp64 / kernel_data->HW_TICKS_PER_TICK;

   xdate_ptr->PSEC = (uint_16)tmp64;

   /* END CR 2129 */

   return TRUE;

} /* Endbody */

/* EOF */
