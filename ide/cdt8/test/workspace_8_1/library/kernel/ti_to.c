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
*** File: ti_to.c
***
*** Comments:
***   This file contains the function for converting from a time struct
*** to a date struct.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_to_date
* Returned Value  : boolean
* Comments        : converts seconds/msecs value into a date and time from
*                   Jan.1 1970
*
*END*------------------------------------------------------------------*/

boolean _time_to_date
   (

      /*  [IN]  pointer to time structure  */
      TIME_STRUCT_PTR  ts_ptr,

      /*  [IN]  pointer to a date/time structure  */
      DATE_STRUCT_PTR  time_ptr

   )
{ /* Body */
   uint_32   time;
   _mqx_uint leap;

#if MQX_CHECK_ERRORS
   if ((ts_ptr == NULL) || (time_ptr == NULL)) {
      return (FALSE);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if ((ts_ptr->SECONDS     > MAXIMUM_SECONDS_IN_TIME) ||
      (ts_ptr->MILLISECONDS > MAXIMUM_MILLISECONDS))
   {
      return( FALSE );
   } /* Endif */
#endif

   /* Find the year the date is in. */

   time_ptr->YEAR = 0;
   while (ts_ptr->SECONDS >= _time_secs_before_year_internal[time_ptr->YEAR + 1]) {
      time_ptr->YEAR++;
   } /* Endwhile */

   /* Calculate the number of seconds since the beginning of the year */
   time = ts_ptr->SECONDS - _time_secs_before_year_internal[time_ptr->YEAR];

   time_ptr->YEAR += (uint_16)CLK_FIRST_YEAR;

   /* Find out if we are in a leap year. */
   leap = (_mqx_uint)_time_check_if_leap(time_ptr->YEAR);

   /* calculate the month */
   time_ptr->MONTH = 1;
   while (time >= _time_secs_before_month_internal[leap][time_ptr->MONTH]) {
      time_ptr->MONTH++;
   } /* Endwhile */
   time -= _time_secs_before_month_internal[leap][time_ptr->MONTH - 1];

   /* calculate the day */
   time_ptr->DAY = (uint_16)(time / SECS_IN_DAY);
   time -= ((uint_32)time_ptr->DAY * SECS_IN_DAY);

   /* first day is 1*/
   time_ptr->DAY++;

   /* calculate the hour */
   time_ptr->HOUR = (uint_16)(time  / SECS_IN_HOUR);
   time -= ((uint_32)time_ptr->HOUR * SECS_IN_HOUR);

   /* calculate the minute */
   time_ptr->MINUTE = (uint_16)(time / SECS_IN_MINUTE);
   time-= ((uint_32)time_ptr->MINUTE * SECS_IN_MINUTE);

   /* the second */
   time_ptr->SECOND = (uint_16)time;

   /* the millisecond */
   time_ptr->MILLISEC = (uint_16)ts_ptr->MILLISECONDS;

   return( TRUE );

} /* Endbody */

/* EOF */
