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
*** File: io_uinst.c
***
*** Comments:      
***   This file contains the function for un-installing a dynamic device
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
* Function Name    : _io_dev_uninstall
* Returned Value   : _mqx_int a task error code or MQX_OK
* Comments         :
*    Un-Install a device dynamically.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_dev_uninstall
   (
      /* [IN] A string that identifies the device for fopen */
      char_ptr           identifier
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   _mqx_int               result = IO_OK;

   _GET_KERNEL_DATA(kernel_data);

   /* Find the device */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)kernel_data->IO_DEVICES.NEXT);
   while (dev_ptr != (pointer)&kernel_data->IO_DEVICES.NEXT) {
      if (!strncmp(identifier, dev_ptr->IDENTIFIER, IO_MAXIMUM_NAME_LENGTH)) {
         /* Found it */
         if (dev_ptr->IO_UNINSTALL != NULL) {
            result = (*dev_ptr->IO_UNINSTALL)(dev_ptr);
            if (result == IO_OK) {
               _QUEUE_REMOVE(&kernel_data->IO_DEVICES, dev_ptr);
               _mem_free(dev_ptr);
            } /* Endif */
         } /* Endif */
         _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
         return(result);
      } /* Endif */
      dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)dev_ptr->QUEUE_ELEMENT.NEXT);
   } /* Endwhile */
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);

   return(IO_DEVICE_DOES_NOT_EXIST);

} /* Endbody */

/* EOF */
