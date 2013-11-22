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
*** File: io_scanf.c
***
*** Comments:      
***   This file contains the function scanf.
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
* Function Name    : _io_scanf
* Returned Value   : _mqx_int number_assigned
* Comments         :
*   This function performs similarly to the scanf function found in 'C'.
*   See scanline.c for comments.
*   The function returns the number of input items converted and assigned.
*   If any error occurs, IO_EOF is returned.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_scanf
   ( 
      /* [IN] the format string to scan with */
      const char _PTR_ fmt_ptr, 
      ...
   )
{ /* Body */
   char    temp_buf[IO_MAXLINE];
   va_list ap;
   _mqx_int result;
   
   va_start(ap, fmt_ptr);
   temp_buf[0] = '\0';
   if (_io_fgetline(stdin, temp_buf, IO_MAXLINE) == IO_EOF) {
      return(IO_EOF);
   } /* Endif */
   result = _io_scanline(temp_buf, (char _PTR_)fmt_ptr, ap);
   va_end(ap);
   return result;

} /* Endbody */

/* EOF */
