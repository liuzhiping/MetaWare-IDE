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
*** File: lwm_allo.c
***
*** Comments :
***  This file contains the function that allocates a memory block.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_alloc
* Returned Value   : pointer. NULL is returned upon error.
* Comments         : allocates a block of memory
*
*END*---------------------------------------------------------*/

pointer _lwmem_alloc
   (
      /* [IN] the size of the memory block */
      _mem_size requested_size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_lwmem_alloc, requested_size);

   result = _lwmem_alloc_internal(requested_size, kernel_data->ACTIVE_PTR,
      (_lwmem_pool_id)kernel_data->KERNEL_LWMEM_POOL);

   _KLOGX2(KLOG_lwmem_alloc, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
