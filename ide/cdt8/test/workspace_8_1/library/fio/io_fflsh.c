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
*** File: io_fflsh.c
***
*** Comments:      
***   This file contains the function io_fflush.
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
* Function Name    : _io_fflush
* Returned Value   : _mqx_int
* Comments         :
*  This function causes any buffered but unwritten data to be written.
*  It returns IO_EOF upon error, and MQX_OK otherwise.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fflush
   (
      /* [IN] the stream whose status is desired */
      FILE_PTR file_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   dev_ptr = file_ptr->DEV_PTR;
   if (dev_ptr->IO_IOCTL != NULL) {   
      return ((*dev_ptr->IO_IOCTL)(file_ptr,
         IO_IOCTL_FLUSH_OUTPUT, NULL));
   } else {
      return(MQX_OK);
   } /* Endif */

} /* Endbody */

/* EOF */
