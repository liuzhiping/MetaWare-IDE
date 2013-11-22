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
*** File: io_vpr.c
***
*** Comments:      
***   This file contains the functions for vprintf, vfprintf and vsprintf.
***   These functions are equivalent to the corresponding printf functions,
***   except that the variable argument list is replaced by one argument,
***   which has been initialized by the va_start macro.
***
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
* Function Name    : _io_vprintf
* Returned Value   : _mqx_int number of characters printed
* Comments         :
*   This function is equivalent to the corresponding printf function,
*   except that the variable argument list is replaced by one argument,
*   which has been initialized by the va_start macro.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_vprintf
   (
      /* [IN] the format string */
      const char _PTR_ fmt_ptr, 

      /* [IN] the arguments */
      va_list          arg
   )
{ /* Body */
   _mqx_int result;
   
   result = _io_doprint(stdout, _io_fputc, (char _PTR_)fmt_ptr, arg);

   return result;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_vfprintf
* Returned Value   : _mqx_int number of characters outputted.
* Comments         : 
*   This function is equivalent to the corresponding printf function,
*   except that the variable argument list is replaced by one argument,
*   which has been initialized by the va_start macro.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_vfprintf
   (
      /* [IN] the stream to print upon */
      FILE_PTR         file_ptr,

      /* [IN] the format string to use for printing */
      const char _PTR_ fmt_ptr,

      /* [IN] the argument list to print */
      va_list          arg
   )
{ /* Body */
   _mqx_int result;
   
   result = 0;
   if ( file_ptr ) {
      result = _io_doprint(file_ptr, _io_fputc, (char _PTR_)fmt_ptr, arg);
   } /* Endif */
   return result;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_vsprintf
* Returned Value   : _mqx_int number of characters printed
* Comments         :
*   This function is equivalent to the corresponding printf function,
*   except that the variable argument list is replaced by one argument,
*   which has been initialized by the va_start macro.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_vsprintf
   ( 
      /* [IN] the string to print into */
      char        _PTR_ str_ptr,
      
      /* [IN] the format string */
      const char  _PTR_ fmt_ptr,
      
      /* [IN] the arguments */
      va_list           arg
   )
{ /* Body */
   _mqx_int result;
   
   result = _io_doprint((FILE_PTR)((pointer)&str_ptr), _io_sputc, (char _PTR_)fmt_ptr, arg);
   *str_ptr = '\0';
   return result;

} /* Endbody */


/* EOF */
