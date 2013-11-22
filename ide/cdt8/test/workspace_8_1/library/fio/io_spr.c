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
*** File: io_spr.c
***
*** Comments:      
***   This file contains the function for sprintf.
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
* Function Name    : _io_sprintf
* Returned Value   : _mqx_int number of characters printed
* Comments         :
*    This function performs similarly to the sprintf function found in 'C'.
*    See doprint.c for comments.
*    The returned number of characters does not include the terminating '\0'
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_sprintf
   (
      /* [IN] the string to print into */
      char        _PTR_ str_ptr,
      
      /* [IN] the format specifier */
      const char  _PTR_ fmt_ptr,
      ...
   )
{ /* Body */
   _mqx_int result;
   va_list ap;
   
   va_start(ap, fmt_ptr);
   result = _io_doprint((FILE_PTR)((pointer)&str_ptr), _io_sputc, (char _PTR_)fmt_ptr, ap);
   *str_ptr = '\0';
   va_end(ap);
   return result;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_sputc
* Returned Value   : void
* Comments         :
*    writes the character into the string located by the string pointer and
*    updates the string pointer.
*
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_sputc
   (
      /* [IN] the character to put into the string */
      _mqx_int  c,
      
      /* [IN/OUT] this is REALLY a pointer to a string pointer, updated */
      FILE_PTR input_string_ptr
   )
{ /* Body */
   char _PTR_ _PTR_ string_ptr = (char _PTR_ _PTR_)((pointer)input_string_ptr);

   *(*string_ptr)++ = (char)c;
   return c;

} /* Endbody */

/* EOF */
