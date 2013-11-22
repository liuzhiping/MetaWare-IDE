/*HEADER*******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: pspcach4.c
***
*** Comments:      
***   This file contains the cache control function for any arc processor.
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _dcache_flush
* Returned Value   : none
* Comments         : 
*   This function flushes the data cache
*
*END*----------------------------------------------------------------------*/

void _dcache_flush
   (
      void
   )
{ /* Body */
#if PSP_HAS_DATA_CACHE
   /* Start CR 2352 */
   _int_disable();
   /* End CR 2352 */
	
   _psp_set_aux(PSP_AUX_DC_FLSH,1);

   while(_psp_get_aux(PSP_AUX_DC_CTRL) & PSP_AUX_DC_CTRL_FLUSH_STATUS) {
      // wait for flush completion
   } /* Endwhile */

   /* Start CR 2352 */
   _int_enable();
   /* End CR 2352 */
#endif
} /* Endbody */

/* EOF */
