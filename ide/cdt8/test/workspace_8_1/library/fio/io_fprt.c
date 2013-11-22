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
*** File: io_fprt.c
***
*** Comments:      
***   This file contains the function for fprintf.
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
* Function Name    : _io_fprintf
* Returned Value   : _mqx_int number of characters outputted.
* Comments         :
*    Similar to the fprintf function of ANSI 'C'.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fprintf
    (
      /* [IN] the stream to print upon */
      FILE_PTR    file_ptr,
      
      /* [IN] the format string to use for printing */
      const char _PTR_  fmt_ptr,
      
      ...
   )
{ /* Body */
   va_list  ap;
   _mqx_int  result;
   
   va_start(ap, fmt_ptr);
   result = 0;
   if ( file_ptr ) {
      result = _io_doprint(file_ptr, _io_fputc, (char _PTR_)fmt_ptr, ap );
   } /* Endif */
   va_end(ap);
   return result;

} /* Endbody */

/* EOF */
