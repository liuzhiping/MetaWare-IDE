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
*** File: io_fputc.c
***
*** Comments:      
***   This file contains the functions for printing a character.
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
* Function Name    : _io_fputc
* Returned Value   : _mqx_int
* Comments         :
*   This function writes the character c (converted to an unsigned char).
* It returns the character written, or EOF for error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fputc
   (
      /* [IN] the character to print out */
      _mqx_int   c,

      /* [IN] the stream to print the character upon */
      FILE_PTR file_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   char                   tmp;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   dev_ptr = file_ptr->DEV_PTR;
#if MQX_CHECK_ERRORS
   if (dev_ptr->IO_WRITE == NULL) {
      file_ptr->ERROR = MQX_IO_OPERATION_NOT_AVAILABLE;
      return(IO_EOF);
   } /* Endif */
#endif
   tmp = (char)c;

   if ((*dev_ptr->IO_WRITE)(file_ptr, &tmp, 1) != 1) {
      return(IO_EOF);
   } /* Endif */

   return(c);

} /* Endbody */

/* EOF */
