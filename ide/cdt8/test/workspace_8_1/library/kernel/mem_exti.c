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
*** File: mem_exti.c
***
*** Comments :
***   This file contains the function that will add an additional block
*** of physical memory to a memory pool.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_extend_pool_internal
* Returned Value   : _mqx_uint MQX_OK or a MQX error code.
* Comments         :
*   This function adds the specified memory area for use
* by the memory manager.
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_extend_pool_internal
   (
      /* [IN] the address of the start of the memory pool addition */
      pointer             start_of_pool,

      /* [IN] the size of the memory pool addition */
      _mem_size           size,

      /* [IN] the memory pool to extend */
      MEMPOOL_STRUCT_PTR  mem_pool_ptr

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR       kernel_data;
   STOREBLOCK_STRUCT_PTR        block_ptr;
   STOREBLOCK_STRUCT_PTR        end_ptr;
   STOREBLOCK_STRUCT_PTR        free_ptr;
   STOREBLOCK_STRUCT_PTR        tmp_ptr;
   MEMPOOL_EXTENSION_STRUCT_PTR ext_ptr;
   uchar_ptr                    real_start_ptr;
   uchar_ptr                    end_of_pool;
   _mem_size                    block_size;
   _mem_size                    real_size;
   _mem_size                    free_block_size;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (size < (_mem_size)(3*MQX_MIN_MEMORY_STORAGE_SIZE)) {
      /* Pool must be big enough to hold at least 3 memory blocks */
      return(MQX_INVALID_SIZE);
   }/* Endif */
#endif

#if MQX_CHECK_VALIDITY
   if (mem_pool_ptr->VALID != MEMPOOL_VALID) {
      return(MQX_INVALID_COMPONENT_HANDLE);
   }/* Endif */
#endif
      
   ext_ptr = (MEMPOOL_EXTENSION_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(start_of_pool);

   real_start_ptr = (uchar_ptr)ext_ptr + sizeof(MEMPOOL_EXTENSION_STRUCT);
   real_start_ptr = (uchar_ptr)_ALIGN_ADDR_TO_HIGHER_MEM(real_start_ptr);

   end_of_pool = (uchar_ptr)start_of_pool + size;
   end_of_pool = (uchar_ptr)_ALIGN_ADDR_TO_LOWER_MEM(end_of_pool);

   real_size  = (_mem_size)(end_of_pool - real_start_ptr);

   ext_ptr->START = start_of_pool;
   ext_ptr->SIZE  = size;

   block_ptr  = (STOREBLOCK_STRUCT_PTR)real_start_ptr;
   block_size = MQX_MIN_MEMORY_STORAGE_SIZE;

   free_ptr = (STOREBLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + block_size);
   free_block_size = real_size - (_mem_size)(2 * MQX_MIN_MEMORY_STORAGE_SIZE);

   end_ptr  = (STOREBLOCK_STRUCT_PTR)((uchar_ptr)free_ptr + free_block_size);

   /* 
   ** Make a small minimal sized memory block to be as
   ** the first block in the pool.  This will be an in-use block
   ** and will thus avoid problems with memory co-allescing during
   ** memory frees
   */
   block_ptr->BLOCKSIZE = block_size;
   block_ptr->USER_AREA = 0;
   block_ptr->PREVBLOCK = (struct storeblock_struct _PTR_)NULL;
   block_ptr->NEXTBLOCK = free_ptr;
   MARK_BLOCK_AS_USED(block_ptr, SYSTEM_TASK_ID(kernel_data));
   CALC_CHECKSUM(block_ptr);

   /* 
   ** Let the next block be the actual free block that will be added
   ** to the free list
   */
   free_ptr->BLOCKSIZE = free_block_size;
   free_ptr->USER_AREA = 0;
   free_ptr->PREVBLOCK = block_ptr;
   free_ptr->NEXTBLOCK = end_ptr;
   MARK_BLOCK_AS_FREE(free_ptr);
   CALC_CHECKSUM(free_ptr);

   /*
   ** Set up a minimal sized block at the end of the pool, and also
   ** mark it as being allocated.  Again this is to comply with the
   ** _mem_free algorithm
   */
   end_ptr->BLOCKSIZE = block_size;
   end_ptr->USER_AREA = 0;
   end_ptr->PREVBLOCK = free_ptr;
   end_ptr->NEXTBLOCK = NULL;
   MARK_BLOCK_AS_USED(end_ptr, SYSTEM_TASK_ID(kernel_data));
   CALC_CHECKSUM(end_ptr);

   _int_disable();  /* Add the block to the free list */
   tmp_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
   mem_pool_ptr->POOL_FREE_LIST_PTR = free_ptr;
   if (tmp_ptr != NULL) {
      PREV_FREE(tmp_ptr) = free_ptr;
   } /* Endif */
   PREV_FREE(free_ptr) = NULL;
   NEXT_FREE(free_ptr) = tmp_ptr;

   /* Reset the free list queue walker for some other task */
   mem_pool_ptr->POOL_FREE_CURRENT_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;

   /* Link in the extension */
   _QUEUE_ENQUEUE(&mem_pool_ptr->EXT_LIST, &ext_ptr->LINK);

   _int_enable();

   return(MQX_OK);

} /* Endbody */

/* EOF */
