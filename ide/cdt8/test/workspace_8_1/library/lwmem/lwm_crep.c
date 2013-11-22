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
*** File: lwm_crep.c
***
*** Comments :
***   This file contains the function that initializes a memory pool.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_create_pool
* Returned Value   : A handle to the memory pool or 
*                    NULL_MEM_POOL_ID on error.
* Comments         :
*   This function initializes a memory storage pool. Will set task error
* code if error occurs
* 
*END*---------------------------------------------------------*/

_lwmem_pool_id _lwmem_create_pool
   (
      /* [IN] the location of the light weight memory pool definition */
      LWMEM_POOL_STRUCT_PTR mem_pool_ptr,

      /* [IN] the start of the memory pool */
      pointer               start,

      /* [IN] the size of the memory pool */
      _mem_size             size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_BLOCK_STRUCT_PTR block_ptr;
   uchar_ptr              end;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_lwmem_create_pool, start, size);

   /* Set the end of memory (aligned) */
   end = (uchar_ptr)start + size;
   mem_pool_ptr->POOL_ALLOC_END_PTR = (pointer)_ALIGN_ADDR_TO_LOWER_MEM(end);

   /* Align the start of the pool */
   block_ptr = (LWMEM_BLOCK_STRUCT_PTR)_ALIGN_ADDR_TO_HIGHER_MEM(start);
   mem_pool_ptr->POOL_ALLOC_START_PTR = (pointer)block_ptr;

   /* Set up the first block as an idle block */
   block_ptr->BLOCKSIZE   = (uchar_ptr)mem_pool_ptr->POOL_ALLOC_END_PTR -
      (uchar_ptr)block_ptr;
   block_ptr->U.NEXTBLOCK = NULL;
   block_ptr->POOL        = mem_pool_ptr;
   mem_pool_ptr->POOL_FREE_LIST_PTR = block_ptr;
   mem_pool_ptr->POOL_ALLOC_PTR     = block_ptr;
   mem_pool_ptr->POOL_FREE_PTR      = block_ptr;
   mem_pool_ptr->POOL_TEST_PTR      = block_ptr;
   
   /* Protect the list of pools while adding new pool */
   _int_disable();
   if (kernel_data->LWMEM_POOLS.NEXT == NULL) {
      /* Initialize the light weight memory */
      _QUEUE_INIT(&kernel_data->LWMEM_POOLS, 0);
   } /* Endif */
   _QUEUE_ENQUEUE(&kernel_data->LWMEM_POOLS, &mem_pool_ptr->LINK);
#if MQX_TASK_DESTRUCTION
   kernel_data->LWMEM_CLEANUP = _lwmem_cleanup_internal;
#endif
   _int_enable();
   mem_pool_ptr->VALID = LWMEM_POOL_VALID;
   
   _KLOGX2(KLOG_lwmem_create_pool, mem_pool_ptr);
   return ((_lwmem_pool_id)mem_pool_ptr);

} /* Endbody */


#if MQX_TASK_DESTRUCTION
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_cleanup_internal
* Returned Value   : none
* Comments         :
*   This function looks for any blocks belonging to the specified
* task and frees the blocks
* 
*END*---------------------------------------------------------*/

void _lwmem_cleanup_internal
   (
      /* [IN] the task whose blocks to check for */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_POOL_STRUCT_PTR  mem_pool_ptr;
   LWMEM_BLOCK_STRUCT_PTR queue_ptr = NULL;
   LWMEM_BLOCK_STRUCT_PTR block_ptr;
   _task_id               tid;
   _mqx_uint              i;
    
   _GET_KERNEL_DATA(kernel_data);

   tid = kernel_data->ACTIVE_PTR->TASK_ID;

   i  = _QUEUE_GET_SIZE(&kernel_data->LWMEM_POOLS);
   mem_pool_ptr = (LWMEM_POOL_STRUCT_PTR)(pointer)
      kernel_data->LWMEM_POOLS.NEXT;

#if MQX_USE_LWMEM_ALLOCATOR
   /* Skip the default memory pool */
   i--;
   mem_pool_ptr = (pointer)mem_pool_ptr->LINK.NEXT;
#endif

   while (i--) {
      block_ptr = (LWMEM_BLOCK_STRUCT_PTR)mem_pool_ptr->POOL_ALLOC_START_PTR;
      _int_disable();
      while ((uchar_ptr)block_ptr < (uchar_ptr)mem_pool_ptr->POOL_ALLOC_END_PTR){
         /* Provide window for higher priority tasks */
         mem_pool_ptr->POOL_DESTROY_PTR = block_ptr;
         _int_enable();
         _int_disable();
         block_ptr = mem_pool_ptr->POOL_DESTROY_PTR;
         if (block_ptr->U.TASK_ID == td_ptr->TASK_ID) {
            /* This block is owned by the target task */
            block_ptr->U.NEXTBLOCK = queue_ptr;
            /* Transfer ownership of block to current task */
            block_ptr->U.TASK_ID = tid;
            queue_ptr = block_ptr;
         } /* Endif */
         block_ptr = (LWMEM_BLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + 
            block_ptr->BLOCKSIZE);
      } /* Endwhile */
      _int_enable();
      while (queue_ptr) {
         block_ptr = queue_ptr;
         queue_ptr = block_ptr->U.NEXTBLOCK;
         _lwmem_free(block_ptr);
      } /* Endwhile */
      mem_pool_ptr = (pointer)mem_pool_ptr->LINK.NEXT;
   } /* Endwhile */
    
} /* Endbody */
#endif
#endif /* MQX_USE_LWMEM */

/* EOF */
