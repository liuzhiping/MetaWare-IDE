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
*** File: io_fgetl.c
***
*** Comments:      
***   This file contains the function for reading an input line.
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
* Function Name    : _io_fgetline
* Returned Value   : _mqx_int number_read
* Comments         :
*    Returns the number of characters read into the input line.
*    The terminating line feed is stripped.
*    The function returns the number of characters read, or IO_EOF on error.
*
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fgetline
   (
      /* [IN] the stream to read the characters from */
      FILE_PTR    file_ptr,

      /* [IN/OUT] where to store the input characters */
      char _PTR_  str_ptr,

      /* [IN] the maximum number of characters to store */
      _mqx_int    max_length
   )
{ /* Body */
   _mqx_int  c;
   _mqx_int  i;
   _mqx_uint flags;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      *str_ptr = '\0';
      return(IO_EOF);
   } /* Endif */
#endif

   if (max_length) {
      max_length--;  /* Need to leave 1 space for the null termination */
   } else {
      max_length = MAX_MQX_INT;  /* Effectively infinite length */
   } /* Endif */

   c = _io_fgetc(file_ptr);
   if (c == IO_EOF) {
      return(IO_EOF);
   } /* Endif */
   flags = file_ptr->FLAGS;
   i = 0;
   while ( (! ((c == '\n') || (c == '\r'))) && (i < max_length) ) {
      if ((flags & IO_FLAG_TEXT) && (c == '\b')) {
         if ( i ) {
            *--str_ptr = ' ';
            --i;
         } /* Endif */
      } else {
         *str_ptr++ = (char)c;
         ++i;
      } /* Endif */
      c = _io_fgetc(file_ptr);
      if (c == IO_EOF) {
         return(IO_EOF);
      } /* Endif */
   } /* Endwhile */

   if (i >= max_length) {
      _io_fungetc((_mqx_int)c, file_ptr);
   } /* Endif */

   *str_ptr = '\0';

   return (i);

} /* Endbody */

/* EOF */
