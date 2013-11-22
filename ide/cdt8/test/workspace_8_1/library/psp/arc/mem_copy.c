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
*** File: mem_copy.c
***
*** Comments:      
***   This file contains the functions for copying memory.
***                                                               
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mem_copy
* Returned Value   : none
* Comments         :
*   This function copies the specified number of bytes from the
* source address to the destination address.  No attempt is made
* to handle overlapping copies to prevent loss of data.
*   The copying is optimized to avoid alignment problems, and attempts
* to copy 32bit numbers optimally
*
*END*----------------------------------------------------------------------*/

void _mem_copy
   (
      /* [IN] address to copy data from */
      pointer from,

      /* [IN] address to copy data to */
      pointer to,

      /* [IN] number of bytes to copy */
      _mem_size number_of_bytes
   )
{ /* Body */

   memcpy(to, from, number_of_bytes);

} /* Endbody */

#ifdef MQX_COMPILE_FOR_SMALL_SIZE
void * memcpy
   (
            void  *dest,
      const void  *src,
            size_t n
   )
{ /* Body */
   char_ptr dest_ptr = (pointer)dest;
   char_ptr src_ptr  = (pointer)src;

   while (n--) {
      *dest_ptr++ = *src_ptr++;
   } /* Endwhile */
   return dest;

} /* Endbody */
#endif

/* EOF */

