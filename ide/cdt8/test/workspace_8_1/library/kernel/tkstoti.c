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
*** File: tkstoti.c
***
*** Comments:
***   This file contains the function for converting from a tick struct
*** to a time struct.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _ticks_to_time
* Returned Value  : boolean
* Comments        : converts ticks into a seconds/msecs value
*
*END*------------------------------------------------------------------*/

boolean _ticks_to_time
   (
      /*  [IN]  pointer to a tick structure  */
      MQX_TICK_STRUCT_PTR  tick_ptr,

      /*  [OUT]  pointer to time structure  */
      TIME_STRUCT_PTR      time_ptr

   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((tick_ptr == NULL) || (time_ptr == NULL)) {
      return (FALSE);
   } /* Endif */
#endif
   PSP_TICKS_TO_TIME(tick_ptr, time_ptr);

   return( TRUE );

} /* Endbody */

/* EOF */
