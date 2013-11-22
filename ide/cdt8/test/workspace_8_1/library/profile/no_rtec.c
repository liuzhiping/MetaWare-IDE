/*HEADER************************************************************************
********************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: no_rtec.c
***
*** Comments:
***   Dummy run time error check functions for compilers that don't support
*** run time error checking
***
********************************************************************************
*END***************************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "rterrprv.h"


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _rterrchk_initialize_context
* Returned Value   : 
* Comments         : Initializes the fields in RTERRCHK_CONTEXT_STRUCT
*
*END*-------------------------------------------------------------------------*/

void _rterrchk_initialize_context
   (
      RTERRCHK_CONTEXT_STRUCT_PTR context_ptr
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _rterrchk_save_context
* Returned Value   : 
* Comments         :
*
*END*-------------------------------------------------------------------------*/

void _rterrchk_save_context
   (
      void 
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _rterrchk_restore_context
* Returned Value   : 
* Comments         :
*
*END*-------------------------------------------------------------------------*/

void _rterrchk_restore_context
   (
      void 
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _rterrchk_finish
* Returned Value   : 
* Comments         : Performs any rtec-specific cleanup that needs to 
*                    be done when a task is destroyed.
*
*END*-------------------------------------------------------------------------*/

void _rterrchk_finish
   (
      RTERRCHK_CONTEXT_STRUCT_PTR context_ptr
   )
{ /* Body */

} /* Endbody */

/* EOF */
