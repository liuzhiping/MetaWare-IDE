/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: io_fopen.c
***
*** Comments:      
***   Contains the function fopen.
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_fopen
* Returned Value   : FILE *
* Comments         :
*    The returned value is a pointer to an I/O model
*
*END*----------------------------------------------------------------------*/

FILE_PTR _io_fopen
   ( 
      /* [IN] the name of the device to open */
      const char _PTR_ open_type_ptr,
      
      /* [IN] I/O initialization parameter to pass to the device initialization */
      const char _PTR_ open_mode_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   FILE_PTR               file_ptr;
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   char _PTR_             dev_name_ptr;
   char _PTR_             tmp_ptr;
   _mqx_int                result;

   _GET_KERNEL_DATA(kernel_data);
  
   /* Begin CR 2294 */ 
   if (kernel_data->IO_DEVICES.NEXT == NULL) {
      return(NULL);
   }
   /* End CR 2294 */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)kernel_data->IO_DEVICES.NEXT);
   while (dev_ptr != (pointer)&kernel_data->IO_DEVICES.NEXT) {
      dev_name_ptr = dev_ptr->IDENTIFIER;
      tmp_ptr      = (char _PTR_)open_type_ptr;
      while (*tmp_ptr && *dev_name_ptr &&
         (*tmp_ptr == *dev_name_ptr))
      {
         ++tmp_ptr;
         ++dev_name_ptr;
      } /* Endwhile */
      if (*dev_name_ptr == '\0') {
         /* Match */
         break;
      } /* Endif */
      dev_ptr = (IO_DEVICE_STRUCT_PTR)((pointer)dev_ptr->QUEUE_ELEMENT.NEXT);
   } /* Endwhile */
   
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM);
   
   if (dev_ptr == (pointer)&kernel_data->IO_DEVICES.NEXT) {
      return(NULL);
   } /* Endif */

   file_ptr = (FILE_PTR)_mem_alloc_system_zero((_mem_size)sizeof(FILE));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (file_ptr == NULL) {
      return(NULL);
   } /* Endif */
#endif
   
   file_ptr->DEV_PTR = dev_ptr;
   if (dev_ptr->IO_OPEN != NULL) {
      result = (*dev_ptr->IO_OPEN)(file_ptr, (char _PTR_)open_type_ptr, (char _PTR_)open_mode_ptr);
#if MQX_CHECK_ERRORS
      if (result != MQX_OK) {
         _task_set_error(result);
         _mem_free(file_ptr);
         return(NULL);
      } /* Endif */
#endif
   } /* Endif */

   return(file_ptr);

} /* Endbody */

/* EOF */
