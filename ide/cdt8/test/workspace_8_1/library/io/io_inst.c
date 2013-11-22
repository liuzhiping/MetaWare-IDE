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
*** File: io_inst.c
***
*** Comments:      
***   This file contains the function for installing a dynamic device
*** driver.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"
#ifdef NULL
#undef NULL
#endif
#include <string.h>
#ifndef NULL
#define NULL ((pointer)0)
#endif


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_dev_install
* Returned Value   : _mqx_uint a task error code or MQX_OK
* Comments         :
*    Install a device dynamically, so tasks can fopen to it.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_dev_install
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr             identifier,
  
      /* [IN] The I/O open function */
      _mqx_int (_CODE_PTR_ io_open)(FILE_PTR, char _PTR_, char _PTR_),

      /* [IN] The I/O close function */
      _mqx_int (_CODE_PTR_ io_close)(FILE_PTR),

      /* [IN] The I/O read function */
      _mqx_int (_CODE_PTR_ io_read)(FILE_PTR, char _PTR_, _mqx_int),

      /* [IN] The I/O write function */
      _mqx_int (_CODE_PTR_ io_write)(FILE_PTR, char _PTR_, _mqx_int),

      /* [IN] The I/O ioctl function */
      _mqx_int (_CODE_PTR_ io_ioctl)(FILE_PTR, _mqx_uint, pointer),

      /* [IN] The I/O initialization data */
      pointer              io_init_data_ptr
   )
{ /* Body */

   return (_io_dev_install_ext(identifier, io_open, io_close, io_read, io_write,
      io_ioctl, (_mqx_int (_CODE_PTR_)(IO_DEVICE_STRUCT_PTR))NULL, 
      io_init_data_ptr));

} /* Endbody */

/* EOF */