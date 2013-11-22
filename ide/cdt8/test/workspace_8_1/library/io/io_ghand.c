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
*** File: io_ghand.c
***
*** Comments:      
***   This file contains the function for returning the current I/O handler
***   in the kernel data structure, or in a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "io.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_get_handle
* Returned Value   : pointer
* Comments         : 
*
*    This function returns the address of a default standard I/O FILE.
*    If an incorrect type is given, or the file_ptr has not been specified,
*    the function will return NULL.
* 
*END*----------------------------------------------------------------------*/

pointer _io_get_handle
   (
      /* [IN] which I/O handle to return */
      _mqx_uint stdio_type
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   switch ( (uint_16)stdio_type ) {

      case IO_PROC_STDIN:
         return kernel_data->PROCESSOR_STDIN;

      case IO_PROC_STDOUT:
         return kernel_data->PROCESSOR_STDOUT;

      case IO_PROC_STDERR:
         return kernel_data->PROCESSOR_STDERR;

      case IO_STDIN:
         return kernel_data->ACTIVE_PTR->STDIN_STREAM;

      case IO_STDOUT:
         return kernel_data->ACTIVE_PTR->STDOUT_STREAM;

      case IO_STDERR:
         return kernel_data->ACTIVE_PTR->STDERR_STREAM;

      default:
         return (pointer) NULL;

   } /* Endswitch */

} /* Endbody */

/* EOF */
