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
*** File: io_sinit.c
***
*** Comments:      
***   This file contains the function that initializes the kernel
*** default serial I/O.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"
#include "serial.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_serial_default_init
* Returned Value   : none
* Comments         :
*   Initializes the kernel default serial I/O
*
*END*----------------------------------------------------------------------*/

void _io_serial_default_init
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   if (kernel_data->INIT.IO_CHANNEL) {
      kernel_data->PROCESSOR_STDIN = _io_fopen(
         (char _PTR_)kernel_data->INIT.IO_CHANNEL,
         (char _PTR_)kernel_data->INIT.IO_OPEN_MODE);
      kernel_data->PROCESSOR_STDOUT = kernel_data->PROCESSOR_STDIN;
      kernel_data->PROCESSOR_STDERR = kernel_data->PROCESSOR_STDIN;
   } /* Endif */

} /* Endbody */

/* EOF */
