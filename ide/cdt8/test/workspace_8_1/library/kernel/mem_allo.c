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
*** File: mem_allo.c
***
*** Comments :
***  This file contains the function that allocates a memory block.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_alloc
* Returned Value   : pointer. NULL is returned upon error.
* Comments         : allocates a block of memory
*
*END*---------------------------------------------------------*/

pointer _mem_alloc
   (
      /* [IN] the size of the memory block */
      _mem_size requested_size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   _mqx_uint               error;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_mem_alloc, requested_size);

   _INT_DISABLE();
   result = _mem_alloc_internal(requested_size, kernel_data->ACTIVE_PTR,
      (MEMPOOL_STRUCT_PTR)&kernel_data->KD_POOL, &error);

   /* update the memory allocation pointer in case a lower priority
   ** task was preempted inside _mem_alloc_internal
   */
   kernel_data->KD_POOL.POOL_ALLOC_CURRENT_BLOCK = 
      kernel_data->KD_POOL.POOL_FREE_LIST_PTR;

   _INT_ENABLE();
   
#if MQX_CHECK_ERRORS
   if (error != MQX_OK) {
      _task_set_error(error);
   } /* Endif */
#endif

   _KLOGX3(KLOG_mem_alloc, result, kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR);
   return(result);

} /* Endbody */

/* EOF */
