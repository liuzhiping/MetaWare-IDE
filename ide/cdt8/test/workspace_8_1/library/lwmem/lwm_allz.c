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
*** File: lwm_allz.c
***
*** Comments :
***  This file contains the function that allocates memory, then zeros the block.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_alloc_zero
* Returned Value   : pointer
* Comments         :
*    Allocates zero filled memory.
*
*END*--------------------------------------------------------*/

pointer _lwmem_alloc_zero
   (
      /* [IN] the size of the memory block */
      _mem_size size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_lwmem_alloc_zero, size);

   result = _lwmem_alloc_internal(size, kernel_data->ACTIVE_PTR, 
      (_lwmem_pool_id)kernel_data->KERNEL_LWMEM_POOL);
   if (result != NULL) {
      _mem_zero(result, size);
   } /* Endif */

   _KLOGX2(KLOG_lwmem_alloc_zero, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
