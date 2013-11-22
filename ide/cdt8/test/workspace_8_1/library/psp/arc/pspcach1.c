/*HEADER*******************************************************************
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
*** File: cache.c
***
*** Comments:
***   This file contains the cache control function for any arc processor.
***
*** $Header:pspcach1.c, 6, 5/14/2004 3:08:50 PM, $
***
*** $NoKeywords$
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _icache_invalidate
* Returned Value   : none
* Comments:
*    Invalidates the instruction cache
*END*----------------------------------------------------------------------*/

void _icache_invalidate
   (
      void
   )
{ /* Body */

   _psp_set_aux(PSP_AUX_IVIC, 0);

   // avoid a bug on some unpatched ARC A5 builds with following misaligned
   // 32 bit instruction or data causing invalid instruction execute.
   _ASM("  nop_s; ");

} /* Endbody */

/* EOF */
