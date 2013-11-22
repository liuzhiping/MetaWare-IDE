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
*** File: io_shand.c
***
*** Comments:      
***   This file contains the function for setting the default I/O stream.
***   in the kernel data structure, or for a task.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"
#include "fio.h"
#include "io.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_set_handle
* Returned Value   : FILE * (BUT as a pointer)
* Comments         : 
*    This function changes the address of a default I/O handle, and returns
*    the previous one.  If an incorrect type is given, or the I/O handle was
*    uninitialized, 0 is returned.
*
*END*----------------------------------------------------------------------*/

pointer _io_set_handle
   (
      /* [IN] which I/O handle to modify */
      _mqx_uint stdio_type,

      /* [IN] the new I/O handle */    
      pointer new_file_ptr
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   register TD_STRUCT_PTR           active_ptr;
   register pointer                 old_file_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_io_set_handle, stdio_type, new_file_ptr);
   
   switch ( (uint_16)stdio_type ) {

      case IO_PROC_STDIN:
         old_file_ptr = kernel_data->PROCESSOR_STDIN;
         kernel_data->PROCESSOR_STDIN = new_file_ptr;
         break;

      case IO_PROC_STDOUT:
         old_file_ptr = kernel_data->PROCESSOR_STDOUT;
         kernel_data->PROCESSOR_STDOUT = new_file_ptr;
         break;

      case IO_PROC_STDERR:
         old_file_ptr = kernel_data->PROCESSOR_STDERR;
         kernel_data->PROCESSOR_STDERR = new_file_ptr;
         break;

      case IO_STDIN:
         active_ptr = kernel_data->ACTIVE_PTR;
         old_file_ptr = active_ptr->STDIN_STREAM;
         active_ptr->STDIN_STREAM = new_file_ptr;
         break;

      case IO_STDOUT:
         active_ptr = kernel_data->ACTIVE_PTR;
         old_file_ptr = active_ptr->STDOUT_STREAM;
         active_ptr->STDOUT_STREAM = new_file_ptr;
         break;

      case IO_STDERR:
         active_ptr = kernel_data->ACTIVE_PTR;
         old_file_ptr = active_ptr->STDERR_STREAM;
         active_ptr->STDERR_STREAM = new_file_ptr;
         break;

      default:
         old_file_ptr = NULL;

   } /* Endswitch */

   _KLOGX2(KLOG_io_set_handle, old_file_ptr);
   return (old_file_ptr);

} /* Endbody */

/* EOF */
