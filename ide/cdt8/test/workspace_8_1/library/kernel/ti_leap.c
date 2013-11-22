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
*** File: ti_leap.c
***
*** Comments:
***   This file contains the function for determining if the given
*** year is a leap year.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_check_if_leap
* Returned Value  : boolean - TRUE if a leap year
* Comments        : Determines if the given year is a leap year
*
*END*------------------------------------------------------------------*/

boolean _time_check_if_leap
   (

     /*  [IN]  The year to check */
     uint_16   year

   )
{ /* Body */
   boolean  leap;

   /*
   ** If the year is a century year not divisible by 400
   ** then it is not a leap year, otherwise if year divisible by
   ** four then it is a leap year
   */
   if (year % (uint_16)100 == (uint_16)0) {

      if (year % (uint_16)400 == (uint_16)0) {
         leap = TRUE;
      } else {
         leap = FALSE;
      } /* Endif */
         
   } else {

      if (year % (uint_16)4 == (uint_16)0) {
         leap = TRUE;
      } else {
         leap = FALSE;
      } /* Endif */

   } /* Endif */

   return leap;
} /* Endbody */

/* EOF */
