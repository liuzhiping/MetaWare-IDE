/*HEADER******************************************************************
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
*** File: ti_diff.c
***
*** Comments:      
***   This file contains the function for calculating the difference
*** between two times.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _time_diff
* Returned Value   : void
* Comments         :
*    This function calculates the difference between two times.
*
*END*----------------------------------------------------------------------*/

void _time_diff
   (
      /* [IN] the starting time */
      TIME_STRUCT_PTR start_time_ptr,

      /* [IN] the ending time */
      TIME_STRUCT_PTR end_time_ptr,

      /* [OUT] the difference in time */
      TIME_STRUCT_PTR diff_time_ptr
   )
{ /* Body */
   TIME_STRUCT temp;

   /* 
   ** Use temporary variable in case diff_time_ptr is the
   ** same as either start or end pointers
   */
   temp.SECONDS      = end_time_ptr->SECONDS;
   temp.MILLISECONDS = end_time_ptr->MILLISECONDS;
   if (temp.MILLISECONDS < start_time_ptr->MILLISECONDS) {
      temp.MILLISECONDS += 1000;
      temp.SECONDS--;
   } /* Endif */
   temp.SECONDS      -= start_time_ptr->SECONDS;
   temp.MILLISECONDS -= start_time_ptr->MILLISECONDS;

   if (temp.MILLISECONDS > 1000) {
      temp.SECONDS      += (temp.MILLISECONDS / 1000);
      temp.MILLISECONDS = temp.MILLISECONDS % 1000;
   } /* Endif */

   *diff_time_ptr = temp;

} /* Endbody */

/* EOF */
