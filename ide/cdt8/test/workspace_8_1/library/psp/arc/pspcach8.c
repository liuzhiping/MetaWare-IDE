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
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _dcache_invalidate_line
* Comments         :
*    This function is called to invalidate a line
*    out of the data cache.
*
*END*------------------------------------------------------------------------*/

void _dcache_invalidate_line
   (
      /* [IN] the address in the cache line to invalidate */
      pointer address
   )
{ /* Body */

   _psp_set_aux(PSP_AUX_DC_IVDL,address);

} /* Endbody */

/* EOF */
