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
* Function Name    : _dcache_lock_line
* Returned Value   : none
* Comments         : 
*   This function locks the given line into the data cache (if possible)
*
*END*----------------------------------------------------------------------*/

void _dcache_lock_line
   (
      /* [IN] the cache line to flush */
      pointer line
   )
{ /* Body */

   _psp_set_aux(PSP_AUX_DC_LDL, line);
   
} /* Endbody */

/* EOF */
