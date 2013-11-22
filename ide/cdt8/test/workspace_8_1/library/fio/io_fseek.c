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
*** File: io_fseek.c
***
*** Comments:      
***   This file contains the function for setting the current location
*** in a file.
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
* Function Name    : _io_fseek
* Returned Value   : _mqx_int 0 or IO_ERROR on error.
* Comments         :
*   This function sets the current file position.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fseek
   (
      /* [IN] the stream to use */
      FILE_PTR  file_ptr,

      /* [IN] the offset for the seek */
      _file_offset  offset,

      /* [IN] mode to determine where to start the seek from */
      _mqx_uint  mode
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_ERROR);
   } /* Endif */
#endif

   switch (mode) {
      case IO_SEEK_SET:
         file_ptr->LOCATION = offset;
         break;
      case IO_SEEK_CUR:
         file_ptr->LOCATION += offset;
         break;
      case IO_SEEK_END:
         file_ptr->LOCATION = file_ptr->SIZE + offset;
         break;
#if MQX_CHECK_ERRORS
      default:
         return(IO_ERROR);
#endif
   } /* Endswitch */

   /* Clear EOF flag */
   file_ptr->FLAGS &= ~IO_FLAG_AT_EOF;

   dev_ptr = file_ptr->DEV_PTR;
   if (dev_ptr->IO_IOCTL != NULL) {
      (*dev_ptr->IO_IOCTL)(file_ptr, IO_IOCTL_SEEK, NULL);
   } /* Endif */

   return(MQX_OK);

} /* Endbody */

/* EOF */
