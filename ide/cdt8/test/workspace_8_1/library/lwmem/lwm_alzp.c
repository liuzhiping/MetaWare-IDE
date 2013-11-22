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
*** File: lwm_alzp.c
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
* Function Name    : _lwmem_alloc_zero_from
* Returned Value   : pointer
* Comments         :
*    Allocates zero filled memory.
*
*END*--------------------------------------------------------*/

pointer _lwmem_alloc_zero_from
   (
      /* [IN] the pool to allocate from */
      pointer    pool_id,

      /* [IN] the size of the memory block */
      _mem_size  size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_lwmem_alloc_zero_from, size);

   result = _lwmem_alloc_internal(size, kernel_data->ACTIVE_PTR, pool_id);
   if (result != NULL) {
      _mem_zero(result, size);
   } /* Endif */

   _KLOGX2(KLOG_lwmem_alloc_zero_from, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
