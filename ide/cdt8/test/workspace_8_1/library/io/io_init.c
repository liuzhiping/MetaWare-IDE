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
*** File: io_init.c
***
*** Comments:      
***   This file contains the function that initializes the I/O 
*** sub-system.
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
* Function Name    : _io_init
* Returned Value   : _mqx_uint MQX_OK or a MQX error code
* Comments         :
*   Initializes the kernel I/O subsystem
*
*END*----------------------------------------------------------------------*/

_mqx_uint _io_init
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   return (_lwsem_create((LWSEM_STRUCT_PTR)&kernel_data->IO_LWSEM, 1));

} /* Endbody */

/* EOF */
