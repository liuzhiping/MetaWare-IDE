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
*** File: io_null.c
*** 
*** Comments: 
***    This file contains the nulldisk driver functions
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"
#include "ionulprv.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_null_install
* Returned Value   : _mqx_uint a task error code or MQX_OK
* Comments         :
*    Install a fdv_null driver.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_null_install
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr            identifier
   ) 
   
{ /* Body */
   _mqx_uint result;
   
   result = _io_dev_install(identifier,
      _io_null_open,
      _io_null_close,
      _io_null_read,
      _io_null_write,
      _io_null_ioctl,
      NULL); 

      return result;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_null_open
* Returned Value   : a null pointer
* Comments         : Opens and initializes fdv_null driver.
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_null_open
   (
      /* [IN] the file handle for the device being opened */
      FILE_PTR   fd_ptr,
       
      /* [IN] the remaining portion of the name of the device */
      char_ptr   open_name_ptr,

      /* [IN] the flags to be used during operation:
      ** echo, translation, xon/xoff, encoded into a pointer.
      */
      char_ptr    flags
   )
{ /* Body */

   /* Nothing to do */
   return(MQX_OK);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_null_close
* Returned Value   : ERROR CODE
* Comments         : Closes fdv_null driver
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_null_close
   (
      /* [IN] the file handle for the device being closed */
      FILE_PTR   fd_ptr
   )
{ /* Body */

   /* Nothing to do */
   return(MQX_OK);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_null_read
* Returned Value   : number of characters read
* Comments         : Reads data from fdv_ram driver
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_null_read
   (
      /* [IN] the file handle for the device */
      FILE_PTR   fd_ptr,

      /* [IN] where the characters are to be stored */
      char_ptr   data_ptr,

      /* [IN] the number of characters to input */
      _mqx_int   num
   )
{ /* Body */
   return(0);
} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_null_write
* Returned Value   : number of characters written
* Comments         : Writes data to the fdv_ram device
* 
*END*----------------------------------------------------------------------*/

_mqx_int _io_null_write
   (
      /* [IN] the file handle for the device */
      FILE_PTR   fd_ptr,

      /* [IN] where the characters are */
      char_ptr   data_ptr,

      /* [IN] the number of characters to output */
      _mqx_int   num
   )
{ /* Body */
   return(num);
} /* Endbody */


/*FUNCTION*****************************************************************
* 
* Function Name    : _io_null_ioctl
* Returned Value   : int_32
* Comments         :
*    Returns result of ioctl operation.
*
*END*********************************************************************/

_mqx_int _io_null_ioctl
   (
      /* [IN] the file handle for the device */
      FILE_PTR   fd_ptr,

      /* [IN] the ioctl command */
      _mqx_uint  cmd,

      /* [IN] the ioctl parameters */
      pointer    param_ptr
   )
{ /* Body */
     return IO_ERROR_INVALID_IOCTL_CMD;
} /* Endbody */

/* EOF */
