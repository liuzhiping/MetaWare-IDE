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
*** File: mem_crei.c
***
*** Comments :
***   This file contains the function that initializes a memory pool.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_create_pool_internal
* Returned Value   : MQX_OK or error code
* Comments         :
* 
*END*---------------------------------------------------------*/

_mqx_uint _mem_create_pool_internal
   (
      /* [IN] the start of the memory pool */
      pointer             start,

      /* [IN] the end of the memory pool */
      pointer             end,

      /* [IN] where to store the memory pool context info. */
      MEMPOOL_STRUCT_PTR  mem_pool_ptr

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   STOREBLOCK_STRUCT_PTR   block_ptr;
   STOREBLOCK_STRUCT_PTR   end_block_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_VALIDITY
   _INT_DISABLE();
   if (kernel_data->MEM_COMP.VALID != MEMPOOL_VALID) {
      /* The RTOS memory system has been corrupted */
      _int_enable();
      return(MQX_CORRUPT_MEMORY_SYSTEM);
   } /* Endif */

   _INT_ENABLE();
#endif

   /* Align the start of the pool */
   mem_pool_ptr->POOL_PTR = (STOREBLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(start);

   /* Set the end of memory (aligned) */
   mem_pool_ptr->POOL_LIMIT = (STOREBLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_LOWER_MEM(end);

#if MQX_CHECK_ERRORS
   if ( (uchar_ptr)mem_pool_ptr->POOL_LIMIT <=
      ((uchar_ptr)mem_pool_ptr->POOL_PTR + MQX_MIN_MEMORY_POOL_SIZE) )
   {
      return MQX_MEM_POOL_TOO_SMALL;
   } /* Endif */
#endif

   block_ptr = (STOREBLOCK_STRUCT_PTR)mem_pool_ptr->POOL_PTR;
   mem_pool_ptr->POOL_HIGHEST_MEMORY_USED = (pointer)block_ptr;
   mem_pool_ptr->POOL_CHECK_POOL_PTR      = (char _PTR_)mem_pool_ptr->POOL_PTR;
   mem_pool_ptr->POOL_BLOCK_IN_ERROR      = NULL;

   /* Compute the pool size. */
   mem_pool_ptr->POOL_SIZE = (_mem_size)((uchar_ptr)mem_pool_ptr->POOL_LIMIT -
      (uchar_ptr)mem_pool_ptr->POOL_PTR);

   /* Set up the first block as an idle block */
   block_ptr->BLOCKSIZE = mem_pool_ptr->POOL_SIZE - MQX_MIN_MEMORY_STORAGE_SIZE;
   block_ptr->USER_AREA = NULL;
   block_ptr->PREVBLOCK = NULL;
   block_ptr->NEXTBLOCK = NULL;
   MARK_BLOCK_AS_FREE(block_ptr);

   CALC_CHECKSUM(block_ptr);

   mem_pool_ptr->POOL_FREE_LIST_PTR = block_ptr;

   /* 
   ** Set up last block as an in_use block, so that the _mem_free algorithm
   ** will work (block coalescing)
   */
   end_block_ptr = (STOREBLOCK_STRUCT_PTR)
      ((uchar_ptr)block_ptr + block_ptr->BLOCKSIZE);
   end_block_ptr->BLOCKSIZE = (_mem_size)(MQX_MIN_MEMORY_STORAGE_SIZE);
   end_block_ptr->USER_AREA = 0;
   end_block_ptr->PREVBLOCK = (struct storeblock_struct _PTR_)block_ptr;
   end_block_ptr->NEXTBLOCK = NULL;
   MARK_BLOCK_AS_USED(end_block_ptr, SYSTEM_TASK_ID(kernel_data));
   CALC_CHECKSUM(end_block_ptr);

   mem_pool_ptr->POOL_END_PTR = end_block_ptr;

   /* Initialize the list of extensions to this pool */
   _QUEUE_INIT(&mem_pool_ptr->EXT_LIST, 0);

   mem_pool_ptr->VALID = MEMPOOL_VALID;

   /* Protect the list of pools while adding new pool */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);
   _QUEUE_ENQUEUE(&kernel_data->MEM_COMP.POOLS, &mem_pool_ptr->LINK);
   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);

   return MQX_OK;

} /* Endbody */

/* EOF */
