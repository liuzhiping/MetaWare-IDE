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
*** File: int_gdef.c
***
*** Comments:
***   This file contains the function for returning the
*** default ISR, called when an unexpected interrupt occurs.
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_get_default_isr
* Returned Value   : _CODE_PTR_ address or NULL on error
* Comments         :
*    This routine returns the current default ISR,
* called whenever an unhandled interrupt occurs.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_get_default_isr
   (
      void
   ))(pointer)
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return(kernel_data->DEFAULT_ISR);

} /* Endbody */

/* EOF */
