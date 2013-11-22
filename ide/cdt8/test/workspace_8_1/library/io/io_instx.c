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
*** File: io_instx.c
***
*** Comments:      
***   This file contains the function for installing a dynamic device
*** driver.
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_dev_install_ext
* Returned Value   : _mqx_uint a task error code or MQX_OK
* Comments         :
*    Install a device dynamically, so tasks can fopen to it. Different from
* _io_dev_install since this function also installs an unstall function.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_dev_install_ext
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

      /* [IN] The I/O un-install function */
      _mqx_int (_CODE_PTR_ io_uninstall)(IO_DEVICE_STRUCT_PTR),

      /* [IN] The I/O initialization data */
      pointer              io_init_data_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   IO_DEVICE_STRUCT_PTR   dev_ptr;
#if MQX_CHECK_ERRORS
   _mqx_uint              i;
   _mqx_uint              found = 0;
#endif

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if ((io_open == NULL) || (io_close == NULL)){
      return(MQX_INVALID_PARAMETER);
   } /* Endif */

   /* Search for delimiter */
   for (i = 0; i < IO_MAXIMUM_NAME_LENGTH; i++) {
      if (identifier[i] == IO_DEV_DELIMITER) {
         found++;
      } else if (identifier[i] == '\0') {
         break;
      } /* Endif */
   } /* Endfor */
      
   /* 
   ** Return an error if more than 1 delimiter found, no delimiter was found
   ** or the identifier was composed of a single delimiter only.
   */
   if ((found != 1) || (i == 1)) {
      return(MQX_INVALID_PARAMETER);
   } /* Endif */
/* START CR-169 */
#endif

   /* Check to see if device already installed */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   if (kernel_data->IO_DEVICES.NEXT == NULL) {
      /* Set up the device driver queue */
      _QUEUE_INIT(&kernel_data->IO_DEVICES, 0);
   } /* Endif */

#if MQX_CHECK_ERRORS
   dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)kernel_data->IO_DEVICES.NEXT);
   while (dev_ptr != (pointer)&kernel_data->IO_DEVICES.NEXT) {
      if (!strncmp(identifier, dev_ptr->IDENTIFIER, IO_MAXIMUM_NAME_LENGTH)) {
         _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
         return(IO_DEVICE_EXISTS);
      } /* Endif */
      dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)dev_ptr->QUEUE_ELEMENT.NEXT);
   } /* Endwhile */
#endif
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
/* END CR-169 */
   
   dev_ptr = (IO_DEVICE_STRUCT_PTR)_mem_alloc_system_zero((_mem_size)
      sizeof(IO_DEVICE_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (dev_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   }/* Endif */
#endif
   
   dev_ptr->IDENTIFIER      = identifier;
   dev_ptr->IO_OPEN         = io_open;
   dev_ptr->IO_CLOSE        = io_close;
   dev_ptr->IO_READ         = io_read;
   dev_ptr->IO_WRITE        = io_write;
   dev_ptr->IO_IOCTL        = io_ioctl;
   dev_ptr->IO_UNINSTALL    = io_uninstall;
   dev_ptr->DRIVER_INIT_PTR = io_init_data_ptr;

   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   _QUEUE_ENQUEUE(&kernel_data->IO_DEVICES, dev_ptr);
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);

   return MQX_OK;

} /* Endbody */

/* EOF */
