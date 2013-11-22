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
*** File: io_misc.c
***
*** Comments:      
***   This file contains the functions that normally would not appear
*** since the fio.h file generates macros that replace them.
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
* Function Name    : _io_getchar
* Returned Value   : _mqx_int
* Comments         :
*  This function returns the next character as an unsigned char (converted to
* an int) or IO_EOF if end of file or error occurs.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_getchar
   (
      void
   )
{ /* Body */

   return(_io_fgetc(stdin));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_getline
* Returned Value   : _mqx_int
* Comments         :
*    Returns the number of characters read into the input line.
*    The terminating '\n' is stripped.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_getline
   (
      /* [IN/OUT] where to store the input characters */
      char _PTR_ str_ptr,
      
      /* [IN] the maximum number of characters to store */
      _mqx_int    max_count
   )
{ /* Body */

   return(_io_fgetline(stdin, str_ptr, max_count));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_gets
* Returned Value   : char *
* Comments         :
*    This function reads the next input line into the array s;  It replaces
*  the terminating newline with '\0'.  It returns s, or NULL if end of file
*  or and error occurs.
*
*END*----------------------------------------------------------------------*/

char _PTR_ _io_gets
   (
      /* [IN/OUT] where to store the input string */
      char _PTR_ tty_line_ptr
   )
{ /* Body */

   return(_io_fgets(tty_line_ptr, 0, stdin));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_putchar
* Returned Value   : _mqx_int
* Comments         :
*   This function writes the character c (converted to an unsigned char).
* It returns the character written, or EOF for error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_putchar
   (
      /* [IN] the character to print out */
      _mqx_int c
   )
{ /* Body */

   return(_io_fputc(c, stdout));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_puts
* Returned Value   : void
* Comments         :
*   This function writes the sting.  It returns 0 on success, or MQX_EOF
* for an error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_puts
   (
      /* [IN] the string to print out */
      char _PTR_ string_ptr
   )
{ /* Body */

   return(_io_fputs(string_ptr, stdout));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_status
* Returned Value   : boolean
* Comments         :
*   This function returns TRUE if a character is available.
*
*END*----------------------------------------------------------------------*/

boolean _io_status
   (
      void
   )
{ /* Body */

   return(_io_fstatus(stdin));

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_ungetc
* Returned Value   : none
* Comments         :
*   this function pushes back a character where it will be returned
* on the next read.  Only 1 pushback character allowed.
* The function returns the character pushed back, or IO_EOF on error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_ungetc
   (
      /* [IN] the character to return to the current input stream */
      _mqx_int character
   )
{ /* Body */

   return(_io_fungetc(character, stdin));

} /* Endbody */

/* EOF */
