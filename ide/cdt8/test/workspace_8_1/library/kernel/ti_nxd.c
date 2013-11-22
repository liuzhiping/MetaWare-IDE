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
*** File: ti_nxd.c
***
*** Comments:
***   This file contains the function for normalizing an xdate struct
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_normalize_xdate
* Returned Value  : boolean
* Comments        : Normalizes an xdate struct. Will return FALSE if
*   illegal values are encountered.
*                   
*
*END*------------------------------------------------------------------*/

boolean _time_normalize_xdate
   (

     /*  [IN]  pointer to date and time structure */
     MQX_XDATE_STRUCT_PTR  xdate_ptr

   )
{ /* Body */
   _mqx_uint leap;

   if (xdate_ptr->YEAR < (_mqx_uint)XCLK_FIRST_YEAR) {
      return FALSE;
   } /* Endif */

   if (xdate_ptr->PSEC >= PICOSECS_IN_NANOSECOND) {
      xdate_ptr->NSEC += xdate_ptr->PSEC / PICOSECS_IN_NANOSECOND;
      xdate_ptr->PSEC  = xdate_ptr->PSEC % PICOSECS_IN_NANOSECOND;
   } /* Endif */

   if (xdate_ptr->NSEC >= NANOSECS_IN_MICROSECOND) {
      xdate_ptr->USEC += xdate_ptr->NSEC / NANOSECS_IN_MICROSECOND;
      xdate_ptr->NSEC  = xdate_ptr->NSEC % NANOSECS_IN_MICROSECOND;
   } /* Endif */

   if (xdate_ptr->USEC >= MICROSECS_IN_MILLISECOND) {
      xdate_ptr->MSEC += xdate_ptr->USEC / MICROSECS_IN_MILLISECOND;
      xdate_ptr->USEC  = xdate_ptr->USEC % MICROSECS_IN_MILLISECOND;
   } /* Endif */

   if (xdate_ptr->MSEC >= MILLISECS_IN_SECOND) {
      xdate_ptr->SEC  += xdate_ptr->MSEC / MILLISECS_IN_SECOND;
      xdate_ptr->MSEC  = xdate_ptr->MSEC % MILLISECS_IN_SECOND;
   } /* Endif */

   if (xdate_ptr->SEC >= SECS_IN_MINUTE) {
      xdate_ptr->MIN += xdate_ptr->SEC / SECS_IN_MINUTE;
      xdate_ptr->SEC  = xdate_ptr->SEC % SECS_IN_MINUTE;
   } /* Endif */

   if (xdate_ptr->MIN >= MINUTES_IN_HOUR) {
      xdate_ptr->HOUR += xdate_ptr->MIN / MINUTES_IN_HOUR;
      xdate_ptr->MIN   = xdate_ptr->MIN % MINUTES_IN_HOUR;
   } /* Endif */

   if (xdate_ptr->HOUR >= HOURS_IN_DAY) {
      xdate_ptr->MDAY += xdate_ptr->HOUR / HOURS_IN_DAY;
      xdate_ptr->HOUR  = xdate_ptr->HOUR % HOURS_IN_DAY;
   } /* Endif */

   if (xdate_ptr->MONTH > MONTHS_IN_YEAR) {
      /* Months range from 1 to 12 */
      xdate_ptr->YEAR  += (xdate_ptr->MONTH-1) / MONTHS_IN_YEAR;
      xdate_ptr->MONTH  = (xdate_ptr->MONTH-1) % MONTHS_IN_YEAR + 1;
   } /* Endif */

   while (1) {

      leap = (_mqx_uint)_time_check_if_leap(xdate_ptr->YEAR);

      if (xdate_ptr->MDAY > _time_days_in_month_internal[leap][xdate_ptr->MONTH]) {
         xdate_ptr->MONTH++;
         xdate_ptr->MDAY -= _time_days_in_month_internal[leap][xdate_ptr->MONTH];
      } else {
         break;
      } /* Endif */

      if (xdate_ptr->MONTH > MONTHS_IN_YEAR) {
         xdate_ptr->YEAR  +=  (xdate_ptr->MONTH / MONTHS_IN_YEAR);
         xdate_ptr->MONTH  =  (xdate_ptr->MONTH % MONTHS_IN_YEAR);
      } /* Endif */

   } /* Endwhile */

   if (xdate_ptr->YEAR > XCLK_LAST_YEAR) {
      return( FALSE );
   } /* Endif */
   
   return TRUE;

} /* Endbody */

/* EOF */
