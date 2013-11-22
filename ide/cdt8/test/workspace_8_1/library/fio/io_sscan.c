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
*** File: io_sscan.c
***
*** Comments:      
***   This file contains the function sscanf.
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_sscanf
* Returned Value   : _mqx_int
* Comments         :
*   This function performs similarly to the 'C' sscanf function.
*   See scanline.c for comments.
*   The function returns the number of input items converted and assigned
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_sscanf
   (
      /* [IN] the string to scan from */
      char _PTR_ str_ptr,

      /* [IN] the format string to scan with */
      char _PTR_ format_ptr, 
      ...
   )
{ /* Body */
   va_list ap;
   _mqx_int result;
   
   va_start(ap, format_ptr);
   result = _io_scanline(str_ptr, format_ptr, ap);
   va_end(ap);
   return result;

} /* Endbody */

/* EOF */
