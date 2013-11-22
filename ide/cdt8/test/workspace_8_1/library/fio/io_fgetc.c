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
*** File: io_fgetc.c
***
*** Comments:      
***   This file contains the functions for reading a character.
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
* Function Name    : _io_fgetc
* Returned Value   : _mqx_int
* Comments         :
*  This function returns the next character as an unsigned char (converted to
* an int) or IO_EOF if end of file or error occurs.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fgetc
   (
      /* [IN] the stream to read the character from */
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

   if ( file_ptr->HAVE_UNGOT_CHARACTER ) {
      file_ptr->HAVE_UNGOT_CHARACTER = FALSE;
      return((_mqx_int)(file_ptr->UNGOT_CHARACTER & 0xFF));
   } /* Endif */

   dev_ptr = file_ptr->DEV_PTR;
#if MQX_CHECK_ERRORS
   if (dev_ptr->IO_READ == NULL) {
      file_ptr->ERROR = MQX_IO_OPERATION_NOT_AVAILABLE;
      return(IO_EOF);
   } /* Endif */
#endif

   if ((*dev_ptr->IO_READ)(file_ptr, &tmp, 1) == 1) {
      return((_mqx_int)((uchar)tmp));
   } else {
      return(IO_EOF);
   } /* Endif */

} /* Endbody */

/* EOF */
