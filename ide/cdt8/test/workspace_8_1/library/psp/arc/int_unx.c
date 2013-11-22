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
*** File: int_unx.c
***
*** Comments:
***   This file contains the function for the unexpected
*** exception handling function for MQX, which will display on the
*** console what exception has ocurred.  
***  NOTE: the default I/O for the current task is used, since a printf
***  is being done from an ISR.  
***  This default I/O must NOT be an interrupt drive I/O channel.
***
*** $Header:int_unx.c, 8, 11/26/2004 9:37:09 AM, Peter Shepherd$
***
*** $NoKeywords$
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_unexpected_isr
* Returned Value   : void
* Comments         :
*    A default handler for all unhandled interrupts.
*
*END*----------------------------------------------------------------------*/

void _int_unexpected_isr
   (
      /* [IN] the parameter passed to the default ISR, the vector */
      pointer parameter
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR         kernel_data;
   TD_STRUCT_PTR                  td_ptr;

   _GET_KERNEL_DATA(kernel_data);
   td_ptr      = kernel_data->ACTIVE_PTR;

   _int_disable();
   if (td_ptr->STATE != UNHANDLED_INT_BLOCKED) {
      td_ptr->STATE = UNHANDLED_INT_BLOCKED;
      td_ptr->INFO  = (_mqx_uint)parameter;

      _QUEUE_UNLINK(td_ptr);
   } /* Endif */
   _int_enable();

} /* Endbody */

/* EOF */
