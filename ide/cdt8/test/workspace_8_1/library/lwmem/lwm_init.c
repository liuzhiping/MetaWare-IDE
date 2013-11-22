/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: lwm_init.c
***
*** Comments :
***  This file contains the function that initializes a default lwmemory
*** pool for use by MQX.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_init_internal
* Returned Value   : an MQX error code
* Comments         : initializes MQX to use the lwmem manager
*
*END*---------------------------------------------------------*/

_mqx_uint _lwmem_init_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   LWMEM_POOL_STRUCT_PTR   lwmem_pool_ptr;
   uchar_ptr               start;

   _GET_KERNEL_DATA(kernel_data);

   /* Initialize the traditional memory manager for now */
   _QUEUE_INIT(&kernel_data->MEM_COMP.POOLS, 0);
   _lwsem_create((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM, 1);
   kernel_data->MEM_COMP.VALID = MEMPOOL_VALID;

   /*
   ** Move the MQX memory pool pointer past the end of kernel data.
   */
   start = (pointer)((uchar_ptr)kernel_data + 
      sizeof(KERNEL_DATA_STRUCT));
   lwmem_pool_ptr = (LWMEM_POOL_STRUCT_PTR)start;
   kernel_data->KERNEL_LWMEM_POOL = lwmem_pool_ptr;

   start = (pointer)((uchar_ptr)start + sizeof(LWMEM_POOL_STRUCT));

   _lwmem_create_pool(lwmem_pool_ptr, start,
      (uchar_ptr)kernel_data->INIT.END_OF_KERNEL_MEMORY - (uchar_ptr)start);

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
