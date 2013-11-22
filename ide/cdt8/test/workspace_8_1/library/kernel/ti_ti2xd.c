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
*** File: ti_ti2xd.c
***
*** Comments:
***   This file contains the function for converting from a tick struct
*** to an xdate struct.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_ticks_to_xdate
* Returned Value  : boolean
* Comments        : converts ticks into a date and time from
*                   Jan.1 1970
*
*END*------------------------------------------------------------------*/

boolean _time_ticks_to_xdate
   (

      /*  [IN]  pointer to tick structure  */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /*  [OUT]  pointer to a xdate structure  */
      MQX_XDATE_STRUCT_PTR xdate_ptr

   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((tick_ptr == NULL) || (xdate_ptr == NULL)) {
      return (FALSE);
   } /* Endif */
#endif

   return PSP_TICKS_TO_XDATE(tick_ptr, xdate_ptr);

} /* Endbody */

/* EOF */
