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
*** File: io_fclos.c
***
*** Comments:      
***   Contains the function fclose.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_fclose
* Returned Value   : _mqx_int
* Comments         :
*   This function calls the close function for the device.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fclose
   ( 
      /* [IN] the stream to close */
      FILE_PTR    file_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   _mqx_uint               result;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   dev_ptr = file_ptr->DEV_PTR;
#if MQX_CHECK_ERRORS
   if (dev_ptr->IO_CLOSE == NULL) {
       _mem_free(file_ptr);
       return(IO_EOF);
   } /* Endif */
#endif
   
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   result = (*dev_ptr->IO_CLOSE)(file_ptr);
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
 
   _mem_free(file_ptr);

    return(result);

} /* Endbody */

/* EOF */
