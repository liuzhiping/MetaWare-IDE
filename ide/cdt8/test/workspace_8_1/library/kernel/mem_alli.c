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
*** File: mem_alli.c
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
* Function Name    : _mem_alloc_internal
* Returned Value   : pointer. NULL is returned upon error.
* Comments         :
*    THIS FUNCTION MUST BE CALLED DISABLED
*
*    A pointer to memory that can be used is returned.
*    _mem_alloc_internal walks down the free list, looking for a 
*    block big enough to satisfy the request.  If the found block 
*    is bigger than needed, then a new free block is created from 
*    the remnants, and added into the free list.  
*
*    A global memory pointer is used, this allows higher priority 
*    tasks to pre-empt the current task in _mem_alloc_internal.  
*    Care is taken to make sure that when the higher priority task 
*    is completed, the memory pool search pointer is reset to the 
*    beginning of the free list.
*
*    Since the memory test function may be running concurrently
*    with the allocator, the memory test pointers are reset.
*
*END*---------------------------------------------------------*/

pointer _mem_alloc_internal
   ( 
      /* [IN] the size of the memory block */
      _mem_size           requested_size,
      
      /* [IN] owner TD */
      TD_STRUCT_PTR       td_ptr,
      
      /* [IN] which pool to allocate from */
      MEMPOOL_STRUCT_PTR  mem_pool_ptr,

      /* [OUT] error code for operation */
      _mqx_uint_ptr       error_ptr
   )
{ /* Body */
   STOREBLOCK_STRUCT_PTR   block_ptr;
   STOREBLOCK_STRUCT_PTR   next_block_ptr;
   STOREBLOCK_STRUCT_PTR   next_next_block_ptr;
   _mem_size               block_size;
   _mem_size               next_block_size;

   *error_ptr = MQX_OK;

   /* 
   ** Adjust message size to allow for block management overhead
   ** and force size to be aligned.
   */
   requested_size += (_mem_size)(FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA));
#if MQX_CHECK_ERRORS
   if (requested_size <  MQX_MIN_MEMORY_STORAGE_SIZE) {
      requested_size = MQX_MIN_MEMORY_STORAGE_SIZE;
   } /* Endif */
#endif

   _MEMORY_ALIGN_VAL_LARGER(requested_size);

   block_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
   
   while ( TRUE ) {      

      /* 
      ** Save the current block pointer.
      ** We will be enabling access to higher priority tasks.
      ** A higher priority task may pre-empt the current task
      ** and may do a memory allocation.  If this is true,
      ** the higher priority task will reset the POOL_ALLOC_CURRENT_BLOCK
      ** upon exit, and the current task will start the search over
      ** again.  
      */
      mem_pool_ptr->POOL_ALLOC_CURRENT_BLOCK = block_ptr;

      /* allow pending interrupts */
      _int_enable();
      _int_disable();

      /* Get block pointer in case reset by a higher priority task */
      block_ptr = mem_pool_ptr->POOL_ALLOC_CURRENT_BLOCK;

      if (block_ptr == NULL) { /* Null pointer */
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = block_ptr;
         *error_ptr = MQX_OUT_OF_MEMORY;
         return( NULL );        /* request failed */
      } /* Endif */

#if MQX_CHECK_VALIDITY
      if ( !_MEMORY_ALIGNED(block_ptr) || BLOCK_IS_USED(block_ptr) ) {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = block_ptr;
         *error_ptr = MQX_CORRUPT_STORAGE_POOL_FREE_LIST;
         return((pointer)NULL);
      } /* Endif */
#endif

#if MQX_CHECK_VALIDITY
      if ( ! VALID_CHECKSUM(block_ptr) ) {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = block_ptr;
         *error_ptr = MQX_INVALID_CHECKSUM;
         return((pointer)NULL);
      } /* Endif */
#endif

      block_size = block_ptr->BLOCKSIZE;

      if (block_size >= requested_size) {
         /* request fits into this block */

         next_block_size = block_size - requested_size;
         if ( next_block_size >= (2 * MQX_MIN_MEMORY_STORAGE_SIZE) ) {
            /* 
            ** The current block is big enough to split.
            ** into 2 blocks.... the part to be allocated is one block,
            ** and the rest remains as a free block on the free list.
            */

            next_block_ptr = (STOREBLOCK_STRUCT_PTR)
               ((char _PTR_)block_ptr + requested_size);

            /* Initialize the new physical block values */
            next_block_ptr->BLOCKSIZE = next_block_size;
            next_block_ptr->PREVBLOCK = (STOREBLOCK_STRUCT_PTR)block_ptr;
            MARK_BLOCK_AS_FREE(next_block_ptr);

            CALC_CHECKSUM(next_block_ptr);

            /* Link new block into the free list */
            next_block_ptr->NEXTBLOCK = block_ptr->NEXTBLOCK;
            block_ptr->NEXTBLOCK      = (pointer)next_block_ptr;

            next_block_ptr->USER_AREA = (pointer)block_ptr;
            if ( next_block_ptr->NEXTBLOCK != NULL ) {
               ((STOREBLOCK_STRUCT_PTR)next_block_ptr->NEXTBLOCK)->
                  USER_AREA = (pointer)next_block_ptr;
            } /* Endif */
           
            /* 
            ** Modify the current block, to point to this newly created
            ** block which is now the next physical block.
            */
            block_ptr->BLOCKSIZE = requested_size;

            /* 
            ** Modify the block on the other side of the next block
            ** (the next next block) so that it's previous block pointer
            ** correctly point to the next block.
            */
            next_next_block_ptr = (STOREBLOCK_STRUCT_PTR)
               NEXT_PHYS(next_block_ptr);
            PREV_PHYS(next_next_block_ptr) = next_block_ptr;
            CALC_CHECKSUM(next_next_block_ptr);

         } else {

            /* Take the entire block */
            requested_size = block_size;

         } /* Endif */

         /* Set the size of the block */
         block_ptr->BLOCKSIZE = requested_size;
         
         /* Indicate the block is in use */
         MARK_BLOCK_AS_USED(block_ptr, td_ptr->TASK_ID);

         CALC_CHECKSUM(block_ptr);

         /* Unlink the block from the free list */
         if ( block_ptr == mem_pool_ptr->POOL_FREE_LIST_PTR ) {
            /* At the head of the free list */

            mem_pool_ptr->POOL_FREE_LIST_PTR = (STOREBLOCK_STRUCT_PTR)
               NEXT_FREE(block_ptr);
            if (mem_pool_ptr->POOL_FREE_LIST_PTR != NULL ) {
               PREV_FREE(mem_pool_ptr->POOL_FREE_LIST_PTR) = 0;
            } /* Endif */

         } else {

            /* 
            ** NOTE: PREV_FREE guaranteed to be non-zero 
            ** Have to make the PREV_FREE of this block
            ** point to the NEXT_FREE of this block
            */
            NEXT_FREE(PREV_FREE(block_ptr)) = NEXT_FREE(block_ptr);
            if ( NEXT_FREE(block_ptr) != NULL ) {
               /* 
               ** Now have to make the NEXT_FREE of this block
               ** point to the PREV_FREE of this block
               */
               PREV_FREE(NEXT_FREE(block_ptr)) = PREV_FREE(block_ptr);
            } /* Endif */

         } /* Endif */

#if MQX_MEMORY_FREE_LIST_SORTED == 1
         if ( block_ptr == mem_pool_ptr->POOL_FREE_CURRENT_BLOCK ) {
            /* Reset the freelist insertion sort by _mem_free */
            mem_pool_ptr->POOL_FREE_CURRENT_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;
         } /* Endif */
#endif

         /* Reset the __mem_test freelist pointer */
         mem_pool_ptr->POOL_FREE_CHECK_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;

         /* 
         ** Set the curent pool block to the start of the free list, so
         ** that if this task pre-empted another that was performing a
         ** _mem_alloc, the other task will restart it's search for a block
         */
         mem_pool_ptr->POOL_ALLOC_CURRENT_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;
            
          /* Remember some statistics */
         next_block_ptr = NEXT_PHYS(block_ptr);
         if ( (char _PTR_)(next_block_ptr) > (char _PTR_)
            mem_pool_ptr->POOL_HIGHEST_MEMORY_USED )
         {
            mem_pool_ptr->POOL_HIGHEST_MEMORY_USED =
               ((char _PTR_)(next_block_ptr) - 1);
         } /* Endif */

         /* Link the block onto the task descriptor. */
         block_ptr->NEXTBLOCK = td_ptr->MEMORY_RESOURCE_LIST;
         td_ptr->MEMORY_RESOURCE_LIST = (pointer)(&block_ptr->USER_AREA);

         block_ptr->MEM_POOL_PTR = (pointer)mem_pool_ptr;

#if MQX_CHECK_VALIDITY
      /* Check that user area is aligned on a cache line boundary */
      if ( !_MEMORY_ALIGNED(&block_ptr->USER_AREA) ) {
         *error_ptr = MQX_INVALID_CONFIGURATION;
         return((pointer)NULL);
      } /* Endif */
#endif

         return( (pointer)(&block_ptr->USER_AREA ) );

      } else {
         block_ptr = (STOREBLOCK_STRUCT_PTR)NEXT_FREE(block_ptr);
      } /* Endif */

   } /* Endwhile */

#ifdef lint
   return( NULL );        /* to satisfy lint */
#endif

} /* Endbody */

/* EOF */
