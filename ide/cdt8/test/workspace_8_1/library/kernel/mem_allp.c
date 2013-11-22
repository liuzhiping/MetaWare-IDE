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
*** File: mem_allp.c
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
* Function Name    : _mem_alloc_from
* Returned Value   : pointer NULL is returned upon error.
* Comments         : Allocates a block of memory from a specified
*   pool
*
*END*---------------------------------------------------------*/

pointer _mem_alloc_from
   (
      /* [IN] the pool to allocate from */
      _mem_pool_id  pool_id,

      /* [IN] the size of the memory block */
      _mem_size     requested_size

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   MEMPOOL_STRUCT_PTR      mem_pool_ptr;
   _mqx_uint                error;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_mem_alloc_from, requested_size);

   mem_pool_ptr = (MEMPOOL_STRUCT_PTR)pool_id;

   _INT_DISABLE();

   result = _mem_alloc_internal(requested_size, kernel_data->ACTIVE_PTR, 
      mem_pool_ptr, &error);

#if MQX_CHECK_ERRORS
   if (error != MQX_OK) {
      _task_set_error(error);
   } /* Endif */
#endif

   /* update the memory allocation pointer in case a lower priority
   ** task was preempted inside _mem_alloc_internal
   */
   mem_pool_ptr->POOL_ALLOC_CURRENT_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;

   _INT_ENABLE();


   _KLOGX3(KLOG_mem_alloc_from, result, mem_pool_ptr->POOL_BLOCK_IN_ERROR);
   return(result);

} /* Endbody */

/* EOF */
