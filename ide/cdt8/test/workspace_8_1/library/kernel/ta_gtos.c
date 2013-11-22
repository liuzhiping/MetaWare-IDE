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
*** File: mqx_gtos.c
***
*** Comments:
***   This file contains the function for returning TOS_RESERVED field
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _task_get_reserved_base
* Returned Value   : TD_STRUCT.TOS_RESERVED;
* Comments         :
*   This function returns the base address of the space reserved
* for this task.  See also comments in ta_rtos.c.
*
*END*----------------------------------------------------------------------*/

/* Start CR 1124: C runtime thread local storage */
pointer _task_get_reserved_base
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _GET_KERNEL_DATA(kernel_data);

   if (kernel_data->IN_ISR) {
      return 0;
   } /* Endif */

   return kernel_data->ACTIVE_PTR->TOS_RESERVED;

} /* Endbody */
   /* End   CR 1124 */

/* EOF */
