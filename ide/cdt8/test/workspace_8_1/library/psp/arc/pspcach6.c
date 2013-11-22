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
* Function Name    : _dcache_flush_mlines
* Returned Value   : none
* Comments         :
* 
*  This function is called to flush the data cache by performing cache
*  copybacks.  It must determine how many cache lines need to be copied
*  back and then perform the copybacks.
*
*END*-----------------------------------------------------------------------*/

void _dcache_flush_mlines
   (
      /* [IN] the address somewhere in the cache line */
      pointer     cache_entry,

      /* [IN] the number of bytes from the address requiring flushing */
      uint_32     entry_size
   )
{ /* Body */
#if PSP_HAS_DATA_CACHE
   uint_32     final_address;
   uint_32     cache_address;

   /*
   **  set cache_entry to the beginning of the cache line;  final_address
   **  indicates the last address which needs to be pushed.  Increment
   **  cache_entry and push the resulting line until final_address is passed.
   */
   cache_address = (uint_32)cache_entry;
   final_address = (cache_address + entry_size -1);
   cache_address = cache_address & PSP_MEMORY_ALIGNMENT_MASK;

   while ( (cache_address <= final_address) ) {
      _dcache_flush_line((pointer)cache_address);
      cache_address += PSP_CACHE_LINE_SIZE;
   } /* Endwhile */
#endif
} /* Endbody */

/* EOF */
