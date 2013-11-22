/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: lwm_alli.c
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
* Function Name    : _lwmem_alloc_internal
* Returned Value   : pointer. NULL is returned upon error.
* Comments         :
*       Allocate a block of memory for a task from the free list
*
*END*---------------------------------------------------------*/

pointer _lwmem_alloc_internal
   ( 
      /* [IN] the size of the memory block */
      _mem_size      requested_size,
      
      /* [IN] owner TD */
      TD_STRUCT_PTR  td_ptr,
      
      /* [IN] which pool to allocate from */
      _lwmem_pool_id pool_id
   )
{ /* Body */
   LWMEM_BLOCK_STRUCT_PTR  block_ptr;
   LWMEM_BLOCK_STRUCT_PTR  next_block_ptr;
   LWMEM_BLOCK_STRUCT_PTR  prev_block_ptr;
   _mem_size               block_size;
   _mem_size               next_block_size;
   LWMEM_POOL_STRUCT_PTR   mem_pool_ptr = (LWMEM_POOL_STRUCT_PTR)pool_id;
      
#if MQX_CHECK_VALIDITY
   if (mem_pool_ptr->VALID != LWMEM_POOL_VALID) {
      _task_set_error(MQX_LWMEM_POOL_INVALID);
      return(NULL);
   } /* Endif */
#endif

   /* 
   ** Adjust message size to allow for block management overhead
   ** and force size to be aligned.
   */
   requested_size += (_mem_size)sizeof(LWMEM_BLOCK_STRUCT);
   if (requested_size <  LWMEM_MIN_MEMORY_STORAGE_SIZE) {
      requested_size = LWMEM_MIN_MEMORY_STORAGE_SIZE;
   } /* Endif */

   _MEMORY_ALIGN_VAL_LARGER(requested_size);

   _int_disable();
   block_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
   prev_block_ptr = block_ptr; /* CR 2366 */
   while ( block_ptr != NULL ) {
      /* Provide window for higher priority tasks */
      mem_pool_ptr->POOL_ALLOC_PTR = block_ptr;
      _int_enable();
      _int_disable();
      block_ptr = mem_pool_ptr->POOL_ALLOC_PTR;
      if (block_ptr == mem_pool_ptr->POOL_FREE_LIST_PTR) {
         prev_block_ptr = block_ptr;
      } /* Endif */
      block_size = block_ptr->BLOCKSIZE;
      if (block_size >= requested_size) {
         /* request fits into this block */
         next_block_size = block_size - requested_size;
         if (next_block_size >= (2 * LWMEM_MIN_MEMORY_STORAGE_SIZE) ) {
            /* 
            ** The current block is big enough to split.
            ** into 2 blocks.... the part to be allocated is one block,
            ** and the rest remains as a free block on the free list.
            */
            next_block_ptr = (LWMEM_BLOCK_STRUCT_PTR)(pointer)
               ((uchar_ptr)block_ptr + requested_size);
            /* Initialize the new physical block values */
            next_block_ptr->BLOCKSIZE = next_block_size;
            /* Link new block into the free list */
            next_block_ptr->POOL        = mem_pool_ptr;
            next_block_ptr->U.NEXTBLOCK = block_ptr->U.NEXTBLOCK;
            block_ptr->U.NEXTBLOCK      = (pointer)next_block_ptr;

            /* Modify the current block, to point to this newly created block*/
            block_ptr->BLOCKSIZE = requested_size;
         } else {
            /* Take the entire block */
            requested_size = block_size;
         } /* Endif */

         if ( block_ptr == mem_pool_ptr->POOL_FREE_LIST_PTR ) {
            /* At the head of the free list */
            mem_pool_ptr->POOL_FREE_LIST_PTR = (LWMEM_BLOCK_STRUCT_PTR)
               block_ptr->U.NEXTBLOCK;
         } else {
            prev_block_ptr->U.NEXTBLOCK = block_ptr->U.NEXTBLOCK;
         } /* Endif */

         mem_pool_ptr->POOL_ALLOC_PTR = mem_pool_ptr->POOL_FREE_LIST_PTR;
         mem_pool_ptr->POOL_FREE_PTR = mem_pool_ptr->POOL_FREE_LIST_PTR;
         mem_pool_ptr->POOL_TEST_PTR = mem_pool_ptr->POOL_FREE_LIST_PTR;
         mem_pool_ptr->POOL_TEST2_PTR = mem_pool_ptr->POOL_ALLOC_START_PTR;
#if MQX_TASK_DESTRUCTION
         mem_pool_ptr->POOL_DESTROY_PTR = mem_pool_ptr->POOL_ALLOC_START_PTR;
#endif
         /* Indicate the block is in use */
         block_ptr->U.TASK_ID = td_ptr->TASK_ID;
         block_ptr->POOL      = (_lwmem_pool_id)mem_pool_ptr;
         _int_enable();
         return((pointer)((uchar_ptr)block_ptr + sizeof(LWMEM_BLOCK_STRUCT)));
      } else {
         prev_block_ptr = block_ptr;
         block_ptr = block_ptr->U.NEXTBLOCK;
      } /* Endif */
   } /* Endwhile */

   _int_enable();
   _task_set_error(MQX_OUT_OF_MEMORY);
   return(NULL);
   

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
