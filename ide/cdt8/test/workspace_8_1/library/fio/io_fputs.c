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
*** File: io_fputs.c
***
*** Comments:      
***   This file contains the functions for printing a string
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_fputs
* Returned Value   : _mqx_int MQX_OK on success, or IO_EOF for an error
* Comments         :
*   This function writes the sting.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fputs
   (
      /* [IN] the string to print out */
      const char _PTR_  string_ptr,

      /* [IN] the stream upon which to print out the string */
      FILE_PTR    file_ptr
   )
{ /* Body */
   _mqx_int result;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   result = _io_write(file_ptr, (char _PTR_)string_ptr, strlen(string_ptr));

   return(result);

} /* Endbody */

/* EOF */
