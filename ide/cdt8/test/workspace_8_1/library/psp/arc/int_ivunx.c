/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: int_ivunx.c
***
*** Comments:
***   This file contains the functions for the unexpected
*** interrupt handling function for MQX, which will display on the
*** console what exception has ocurred.  
***   NOTE: the default I/O for the current task is used, since a printf
*** is being done from an ISR.  
*** This default I/O must NOT be an interrupt drive I/O channel.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/* Start CR 1930 */
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_install_verbose_unexpected_isr
* Returned Value   : _CODE_PTR_ address of old default ISR
* Comments         :
*    This routine installs the verbose unexpected interrupt handler
*    to handle un-attached interrupts and exceptions.  This interrupt
*    handler displays on the console information about any unexpected
*    interrupts.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_install_verbose_unexpected_isr
   (
      void
   ))(pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   kernel_data->FLAGS |= MQX_FLAGS_EXCEPTION_HANDLER_INSTALLED;

   return(_int_install_default_isr(_int_verbose_unexpected_isr));

} /* Endbody */

/* End CR 1930 */
/* EOF */
