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
* Function Name    : _dcache_disable
* Returned Value   : none
* Comments         : 
*   This function disables the data cache (if possible)
*
*END*----------------------------------------------------------------------*/

void _dcache_disable
   (
      void
   )
{ /* Body */

   _psp_set_aux(PSP_AUX_DC_CTRL, PSP_AUX_DC_CTRL_DC_DISABLE);
   
} /* Endbody */

/* EOF */
