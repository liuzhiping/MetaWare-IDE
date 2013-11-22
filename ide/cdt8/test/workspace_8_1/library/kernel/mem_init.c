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
*** File: mem_init.c
***
*** Comments :
***   This file contains the function that initializes the memory pool.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_init_internal
* Returned Value   : _mqx_uint error_code
*       MQX_OK, INIT_KERNEL_MEMORY_TOO_SMALL
* Comments         :
*   This function initializes the memory storage pool.
* 
*END*---------------------------------------------------------*/

_mqx_uint _mem_init_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer                 start;

   _GET_KERNEL_DATA(kernel_data);

   _QUEUE_INIT(&kernel_data->MEM_COMP.POOLS, 0);

   _lwsem_create((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM, 1);

   kernel_data->MEM_COMP.VALID = MEMPOOL_VALID;

   /*
   ** Move the MQX memory pool pointer past the end of kernel data.
   */
   start = (pointer)((uchar_ptr)kernel_data + 
      sizeof(KERNEL_DATA_STRUCT));

   return(_mem_create_pool_internal(start, 
      kernel_data->INIT.END_OF_KERNEL_MEMORY, 
      (MEMPOOL_STRUCT_PTR)&kernel_data->KD_POOL));

} /* Endbody */

/* EOF */
