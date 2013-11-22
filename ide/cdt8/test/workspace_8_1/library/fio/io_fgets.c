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
*** File: io_fgets.c
***
*** Comments:      
***   This file contains the function for getting a string.
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
* Function Name    : _io_fgets
* Returned Value   : char *
* Comments         :
*    This function reads at most the next size-1 characters into the array
*   tty_line_ptr, stopping if a newline is encountered; The newline is 
*   included in the array, which is terminated by '\0'.
*   This function returns tty_line_ptr, or NULL (if end of file or error).
*
*END*----------------------------------------------------------------------*/

char _PTR_ _io_fgets
   (
      /* [IN/OUT] where to store the input string */
      char _PTR_  tty_line_ptr,

      /* [IN] the maximum length to store */
      _mqx_int    size,
      
      /* [IN] the stream to read from */
      FILE_PTR    file_ptr
   )
{ /* Body */
   _mqx_int result;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(NULL);
   } /* Endif */
#endif

   result = _io_fgetline(file_ptr, tty_line_ptr, size);

   if (result == IO_EOF) {
      return(NULL);
   } /* Endif */

   return tty_line_ptr;

} /* Endbody */

/* EOF */

