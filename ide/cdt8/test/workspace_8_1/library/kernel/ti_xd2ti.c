/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: ti_xd2ti.c
***
*** Comments:
***   This file contains the function for converting from a xdate struct
*** to a tick struct.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_xdate_to_ticks
* Returned Value  : boolean
* Comments        : converts from the xdate structure into ticks
*                   since 1970.
*
*END*------------------------------------------------------------------*/


boolean _time_xdate_to_ticks
   (

     /* [IN]  pointer to date and time structure */
     MQX_XDATE_STRUCT_PTR  xdate_ptr,

     /* [OUT]  pointer to structure where num of ticks is to be written */   
     MQX_TICK_STRUCT_PTR   tick_ptr

   )
{ /* Body */
   uint_32                time;
   _mqx_uint              tmp;
   MQX_TICK_STRUCT        t2;
   KERNEL_DATA_STRUCT_PTR kernel_data;

#if MQX_CHECK_ERRORS
   if ((xdate_ptr == NULL) || (tick_ptr == NULL)) {
      return FALSE;
   } /* Endif */
#endif

   if (!_time_normalize_xdate(xdate_ptr)) {
      return FALSE;
   } /* Endif */

   /*
   ** Determine the number of days since Jan 1, 1970 at 00:00:00
   */
   time = _time_days_before_year_internal[(xdate_ptr->YEAR - 
      (uint_16)XCLK_FIRST_YEAR)];

   tmp = (_mqx_uint)_time_check_if_leap(xdate_ptr->YEAR);

   /*
   ** Add the number of days since 0000 hours, Jan 1, to the first
   ** day of month.
   */
   time += _time_days_before_month_internal[tmp][xdate_ptr->MONTH-1];

   /*
   ** Add the number of days since the beginning of the
   ** month
   */
   time += (uint_32)(xdate_ptr->MDAY - 1);

   /* Convert everything so far into hours */
   time = time * HOURS_IN_DAY + xdate_ptr->HOUR;

   /* Convert to seconds */
   tick_ptr->HW_TICKS = 0;
   t2 = _mqx_zero_tick_struct;
   PSP_ADD_TICKS_TO_TICK_STRUCT(&t2, SECS_IN_HOUR, &t2);
   PSP_MULTIPLY_TICKS_BY_UINT_32(&t2, time, tick_ptr);

   /* Add in seconds and minutes from XDATE struct */
   tmp = xdate_ptr->SEC + xdate_ptr->MIN * SECS_IN_MINUTE;
   PSP_ADD_TICKS_TO_TICK_STRUCT(tick_ptr, tmp, tick_ptr);

   /* Convert everything into ticks */
   _GET_KERNEL_DATA(kernel_data);
   time = (uint_32)kernel_data->TICKS_PER_SECOND;
   PSP_MULTIPLY_TICKS_BY_UINT_32(tick_ptr, time, tick_ptr);

   if (xdate_ptr->MSEC) {
      PSP_MILLISECONDS_TO_TICKS(xdate_ptr->MSEC, &t2);
      PSP_ADD_TICKS(tick_ptr, &t2, tick_ptr);
   } /* Endif */

   if (xdate_ptr->USEC) {
      PSP_MICROSECONDS_TO_TICKS(xdate_ptr->USEC, &t2);
      PSP_ADD_TICKS(tick_ptr, &t2, tick_ptr);
   } /* Endif */

   if (xdate_ptr->NSEC) {
      PSP_NANOSECONDS_TO_TICKS(xdate_ptr->NSEC, &t2);
      PSP_ADD_TICKS(tick_ptr, &t2, tick_ptr);
   } /* Endif */

   if (xdate_ptr->PSEC) {
      PSP_PICOSECONDS_TO_TICKS(xdate_ptr->PSEC, &t2);
      PSP_ADD_TICKS(tick_ptr, &t2, tick_ptr);
   } /* Endif */

   return TRUE;

} /* Endbody */

/* EOF */
