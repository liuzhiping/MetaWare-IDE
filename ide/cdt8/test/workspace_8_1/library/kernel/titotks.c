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
*** File: titotks.c
***
*** Comments:
***   This file contains the function for converting from a time struct
*** to a tick struct.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _time_to_ticks
* Returned Value  : boolean
* Comments        : converts seconds/msecs value into a tick struct
*
*END*------------------------------------------------------------------*/

boolean _time_to_ticks
   (
      /*  [IN]  pointer to time structure  */
      TIME_STRUCT_PTR      time_ptr,

      /*  [IN]  pointer to a tick structure  */
      MQX_TICK_STRUCT_PTR  tick_ptr

   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((tick_ptr == NULL) || (time_ptr == NULL)) {
      return (FALSE);
   } /* Endif */
#endif
   PSP_TIME_TO_TICKS(time_ptr, tick_ptr);

   return( TRUE );

} /* Endbody */

/* EOF */
