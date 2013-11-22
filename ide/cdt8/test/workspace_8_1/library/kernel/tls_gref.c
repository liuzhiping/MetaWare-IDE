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
*** File: mqx_stad.c
***
*** Comments:
***   This file contains the function for returning CRT_TLS field
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _crt_tls_reference
* Returned Value   : Address of C runtime errno
* Comments         :
*   This function gets the address of the TD_STRUCT.CRT_TLS field.
*
*END*----------------------------------------------------------------------*/

pointer _crt_tls_reference
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _GET_KERNEL_DATA(kernel_data);

   if (kernel_data->IN_ISR) {
      return 0;
   } /* Endif */

   return &kernel_data->ACTIVE_PTR->CRT_TLS;

} /* Endbody */

/* EOF */
