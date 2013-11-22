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
*** File: io_pntf.c
***
*** Comments:      
***   This file contains the function printf.
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
* Function Name    : _io_printf
* Returned Value   : _mqx_int number of characters printed
* Comments         :
*    Performs similarly to the ANSI 'C' printf function.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_printf
   (
      /* [IN] the format string to use when printing */
      const char _PTR_ fmt_ptr, 
      ...
   )
{ /* Body */
   va_list  ap;
   _mqx_int  result;
   
   va_start(ap, fmt_ptr);

   result = _io_doprint(stdout, _io_fputc, (char _PTR_)fmt_ptr, ap);
   va_end(ap);
   return result;

} /* Endbody */

/* EOF */
