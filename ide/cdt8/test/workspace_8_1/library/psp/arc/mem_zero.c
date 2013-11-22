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
*** File: mem_zero.c
***
*** Comments:      
***   This file contains the functions for zeroing memory
***                                                               
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mem_zero
* Returned Value   : none
* Comments         :
*   This function zeros the specified number of bytes at the specified
* location.
*   The zeroing is optimized to avoid alignment problems, and attempts
* to zero 32bit numbers optimally
*
*END*----------------------------------------------------------------------*/

void _mem_zero
   (
      /* [IN] the address to start zeroing memory from */
      pointer loc,

      /* [IN] the number of bytes to zero */
      _mem_size number_of_bytes
   )
{ /* Body */

   memset(loc, '\0', number_of_bytes);
   
} /* Endbody */

#ifdef MQX_COMPILE_FOR_SMALL_SIZE
void * memset
   (
      void  *dest,
      int    c,
      size_t n
   )
{ /* Body */
   char_ptr dest_ptr = dest;

   while (n--) {
      *dest_ptr++ = (char)c;
   } /* Endwhile */
   return dest;

} /* Endbody */
#endif

/* EOF */
