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
*** File: io_fscan.c
***
*** Comments:      
***   This file contains the function fscanf.
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
* Function Name    : _io_fscanf
* Returned Value   : _mqx_int number
* Comments         :
*   This function performs similarly to the scanf function of 'C'.
*   See scanline.c for comments.
*   The function returns the number of input items converted and assigned.
*   If any error occurs, IO_EOF is returned.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fscanf
   (
      /* [IN] the stream to scan from */
      FILE_PTR    file_ptr,

      /* [IN] the format string to use when scanning */
      const char _PTR_  fmt_ptr, 
      ...
   )
{ /* Body */
   char    temp_buf[IO_MAXLINE];
   va_list ap;
   _mqx_int result;
   
#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   va_start(ap, fmt_ptr);
   /* get a line of input from user */
   if (_io_fgetline( file_ptr, temp_buf, IO_MAXLINE) == IO_EOF) {
      return(IO_EOF);
   } /* Endif */
   result = _io_scanline( temp_buf, (char _PTR_)fmt_ptr, ap );
   va_end(ap);
   return result;

} /* Endbody */

/* EOF */
