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
*** File: no_prof.c
***
*** Comments:
***   Dummy profiling functions for compilers that don't support
*** profiling
***
********************************************************************************
*END***************************************************************************/

#include "mqx_inc.h"
#include "profprv.h"


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _profiler_initialize_context
* Returned Value   : 
* Comments         : Initializes the fields in PROFILE_CONTEXT_STRUCT
*
*END*-------------------------------------------------------------------------*/

void _profiler_initialize_context
   (
      /* [IN] The profile context for this task */
      PROFILE_CONTEXT_STRUCT_PTR    context_ptr
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _profiler_save_context
* Returned Value   : 
* Comments         : 
*
*END*-------------------------------------------------------------------------*/

void _profiler_save_context
   (
      void 
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _profiler_restore_context
* Returned Value   : 
* Comments         : 
*
*END*-------------------------------------------------------------------------*/

void _profiler_restore_context
   (
      void 
   )
{ /* Body */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _profiler_finish
* Returned Value   : 
* Comments         : 
*
*END*-------------------------------------------------------------------------*/

void _profiler_finish
   (
      /* [IN] The profile context for this task */
      PROFILE_CONTEXT_STRUCT_PTR    context_ptr
   )
{ /* Body */

} /* Endbody */

/* EOF */

