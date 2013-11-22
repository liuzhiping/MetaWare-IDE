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
*** File: _mem_vrfy.c
***
*** Comments :
***    This file contains the function that verifies memory can be
*** read and written correctly.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
*
* Function Name    : _mem_verify
* Returned Value   : _mqx_uint
*    A task error code on error, MQX_OK if no error.
* Comments         :
*   This function checks that memory can be read and written
* without corruption.  It aligns the addresses it's given to
* a PSP specified value and then writes every 32-bit element
* in the memory range once with values that change every bit.
* I then reads back the data and returns an error if the
* value read back does not equal the value written.
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_verify
   (
      pointer   base,
      pointer   extent
   )
{ /* Body */

   _mqx_uint result = MQX_INVALID_SIZE;

   if (extent > base) {
      uchar_ptr cbase   = (uchar_ptr) _ALIGN_ADDR_TO_HIGHER_MEM(base);
      uchar_ptr cextent = (uchar_ptr) _ALIGN_ADDR_TO_LOWER_MEM(extent);

      if (cextent > cbase) {
         uint_32     length = cextent - cbase;
         uint_32_ptr p, p1 = (uint_32_ptr) cbase;
         uint_32_ptr eom = (uint_32_ptr) (cbase + length);
         uint_32     v = 0x12345678;

         for (p = p1; p < eom; p++) {
            *p = v;
            v += 0x11111111;
         } /* Endfor */

         result = MQX_OK;

         v = 0x12345678;
         for (p = p1; p < eom; p++) {
            if (*p != v) {
               result = MQX_CORRUPT_MEMORY_SYSTEM;
               break;
            } /* Endif */
            v += 0x11111111;
         } /* Endfor */

      } /* Endif */

   } /* Endif */

   return(result);

} /* Endbody */

/* EOF */
