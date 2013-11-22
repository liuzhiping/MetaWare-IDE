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
*** File: ti_from.c
***
*** Comments:
***   This file contains the function for converting from a date struct
*** to a time struct..
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_from_date
* Returned Value  : boolean
* Comments        : converts from date structure into seconds and
*                   milliseconds since 1970.
*
*END*------------------------------------------------------------------*/


boolean _time_from_date
   (

     /*  [IN]  pointer to date and time structure */
     DATE_STRUCT_PTR  time_ptr,

     /*  [OUT]  pointer to structure where num of secs and msecs
     **         is to be written
     */   
     TIME_STRUCT_PTR  ts_ptr

   )
{ /* Body */
   uint_32   time;
   _mqx_uint leap;

   /* Validate each of the parameters before doing time conversion. */

#if MQX_CHECK_ERRORS
   if ((time_ptr == NULL) || (ts_ptr == NULL)) {
      return (FALSE);
   } /* Endif */
#endif

   if (time_ptr->MILLISEC >= (uint_16)MILLISECS_IN_SECOND) {
      time_ptr->SECOND +=    (time_ptr->MILLISEC / (uint_16)MILLISECS_IN_SECOND);
      time_ptr->MILLISEC =   (time_ptr->MILLISEC % (uint_16)MILLISECS_IN_SECOND);
   } /* Endif */

   if (time_ptr->SECOND >= (uint_16)SECS_IN_MINUTE) {
      time_ptr->MINUTE +=   (time_ptr->SECOND / (uint_16)SECS_IN_MINUTE);
      time_ptr->SECOND  =   (time_ptr->SECOND % (uint_16)SECS_IN_MINUTE);
   } /* Endif */

   if (time_ptr->MINUTE >= (uint_16)MINUTES_IN_HOUR) {
      time_ptr->HOUR +=   (time_ptr->MINUTE / (uint_16)60);
      time_ptr->MINUTE =  (time_ptr->MINUTE % (uint_16)60);
   } /* Endif */

   if (time_ptr->HOUR >= (uint_16)HOURS_IN_DAY) {
      time_ptr->DAY  +=   (time_ptr->HOUR / (uint_16)HOURS_IN_DAY);
      time_ptr->HOUR =    (time_ptr->HOUR % (uint_16)HOURS_IN_DAY);
   } /* Endif */

   if (time_ptr->DAY < 1) {
      return( FALSE );
   } /* Endif */

   if (time_ptr->MONTH > (uint_16)MONTHS_IN_YEAR) {
      time_ptr->YEAR +=   (time_ptr->MONTH / (uint_16)MONTHS_IN_YEAR);
      time_ptr->MONTH =   (time_ptr->MONTH % (uint_16)MONTHS_IN_YEAR);
   } /* Endif */

   if (time_ptr->YEAR < (uint_16)CLK_FIRST_YEAR) {
      time_ptr->YEAR = (uint_16)CLK_FIRST_YEAR;
   } /* Endif */

   while (1) {
      
      /* Find out if we are in a leap year. */
      leap = (_mqx_uint)_time_check_if_leap(time_ptr->YEAR);

      if (time_ptr->DAY > (uint_16)_time_days_in_month_internal[leap][time_ptr->MONTH]) {
         time_ptr->DAY -= (uint_16)_time_days_in_month_internal[leap][time_ptr->MONTH];
         time_ptr->MONTH++;
      } else {
         break;
      } /* Endif */

      if (time_ptr->MONTH > (uint_16)MONTHS_IN_YEAR) {
         time_ptr->YEAR  +=   (time_ptr->MONTH / (uint_16)MONTHS_IN_YEAR);
         time_ptr->MONTH =    (time_ptr->MONTH % (uint_16)MONTHS_IN_YEAR);
      } /* Endif */

   } /* Endwhile */

   if (time_ptr->YEAR > (uint_16)CLK_LAST_YEAR) {
      return( FALSE );
   } /* Endif */
   
   /*
   ** Determine the number of seconds since Jan 1, 1970 at 00:00:00
   */
   time = _time_secs_before_year_internal[
      time_ptr->YEAR - (uint_16)CLK_FIRST_YEAR];

   /*
   ** Add the number of seconds_ptr since 0000 hours, Jan 1, to the first
   ** day of month.
   */
   time += _time_secs_before_month_internal[leap][time_ptr->MONTH - 1];

   /*
   ** Add the number of seconds in the days since the beginning of the
   ** month
   */
   time += ((uint_32)time_ptr->DAY - 1) * SECS_IN_DAY;

   /*
   ** Add the number of seconds in the hours since midnight
   */
   time += (uint_32)time_ptr->HOUR * SECS_IN_HOUR;

   /*
   ** Add the number of seconds in the minutes since the hour
   */
   time += (uint_32)time_ptr->MINUTE * 60;

   /*
   ** add the number of seconds since the beginning of the minute
   */
   time += (uint_32)time_ptr->SECOND;

   /*
   ** assign the times
   */
   ts_ptr->SECONDS      = time;
   ts_ptr->MILLISECONDS = (uint_32)time_ptr->MILLISEC;

   return(TRUE);

} /* Endbody */

/* EOF */
