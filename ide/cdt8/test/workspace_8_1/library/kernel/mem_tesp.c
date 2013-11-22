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
*** File: _mem_tesp.c
***
*** Comments :
***    This file contains the function that tests a memory pool's validity
*** It checks for incorrect checksums, and incorrect memory pointers.
*** This function can run concurrently with any other memory functions.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_test_pool
* Returned Value   : _mqx_uint
*    A task error code on error, MQX_OK if no error
*    CORRUPT_STORAGE_POOL_POINTERS, CORRUPT_STORAGE_POOL,
*    INVALID_CHECKSUM, CORRUPT_STORAGE_POOL_FREE_LIST
* Comments         :
*   This function a specified memory for any errors.  It uses
* global memory pointers, disable and enable to allow it
* to run freely in the background as a low priority task,
* but have a small amount
* of disabled code.
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_test_pool
   (
      /* [IN] the pool to check */
      _mem_pool_id pool_id

   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
          STOREBLOCK_STRUCT_PTR   next_block_ptr;
          MEMPOOL_STRUCT_PTR      mem_pool_ptr;
          _mqx_uint               result = MQX_OK;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE2(KLOG_mem_test_pool, pool_id);

   mem_pool_ptr = (MEMPOOL_STRUCT_PTR)pool_id;

   /* First check the physical blocks */

   mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK = (STOREBLOCK_STRUCT_PTR)mem_pool_ptr->POOL_PTR;
   while (mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK < mem_pool_ptr->POOL_END_PTR){
      if ( (! mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK) ||
           (! _MEMORY_ALIGNED(mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK)))
      {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = 
            (STOREBLOCK_STRUCT_PTR)mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK;
         result = MQX_CORRUPT_STORAGE_POOL;
         break;
      } /* Endif */

      _int_disable();
      if ( ! VALID_CHECKSUM(mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK) ) {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = 
            (STOREBLOCK_STRUCT_PTR)mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK;
         _int_enable();
         result = MQX_INVALID_CHECKSUM;
         break;
      } /* Endif */

      next_block_ptr = NEXT_PHYS(mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK);
      if (next_block_ptr->PREVBLOCK != mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK){
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = next_block_ptr;
         _int_enable();
         result = MQX_CORRUPT_STORAGE_POOL;
         break;
      } /* Endif */
      mem_pool_ptr->POOL_PHYSICAL_CHECK_BLOCK = next_block_ptr;
      _int_enable();

   } /* Endwhile */

   if (result != MQX_OK) {
      _KLOGX3(KLOG_mem_test_pool, result, mem_pool_ptr->POOL_BLOCK_IN_ERROR);
      return(result);
   } /* Endif */

   /* Now check the free list */
   _int_disable();
   if ( mem_pool_ptr->POOL_FREE_LIST_PTR == NULL ) { /* no free list to check */
      _int_enable();
      return MQX_OK;
   } /* Endif */

   mem_pool_ptr->POOL_FREE_CHECK_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;
   next_block_ptr = mem_pool_ptr->POOL_FREE_CHECK_BLOCK;
   if ( next_block_ptr->USER_AREA != (pointer)NULL ) {
      _KLOGX3(KLOG_mem_test_pool, MQX_CORRUPT_STORAGE_POOL_FREE_LIST, next_block_ptr );
      mem_pool_ptr->POOL_BLOCK_IN_ERROR = next_block_ptr;
      _int_enable();
      return(MQX_CORRUPT_STORAGE_POOL_FREE_LIST);
   } /* Endif */

   _int_enable();

   while ( mem_pool_ptr->POOL_FREE_CHECK_BLOCK < mem_pool_ptr->POOL_END_PTR ) {
      if ( (! mem_pool_ptr->POOL_FREE_CHECK_BLOCK) ||
           (! _MEMORY_ALIGNED(mem_pool_ptr->POOL_FREE_CHECK_BLOCK)))
      {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = mem_pool_ptr->POOL_FREE_CHECK_BLOCK;
         result = MQX_CORRUPT_STORAGE_POOL_FREE_LIST;
         break;
      } /* Endif */

      _int_disable();
      if ( ! VALID_CHECKSUM(mem_pool_ptr->POOL_FREE_CHECK_BLOCK) ) {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = mem_pool_ptr->POOL_FREE_CHECK_BLOCK;
         _int_enable();
         result = MQX_INVALID_CHECKSUM;
         break;
      } /* Endif */
      _int_enable();

      _int_disable();
      if ( BLOCK_IS_USED(mem_pool_ptr->POOL_FREE_CHECK_BLOCK) ) {
         /* An allocated block on the free list */
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = mem_pool_ptr->POOL_FREE_CHECK_BLOCK;
         _int_enable();  
         result = MQX_CORRUPT_STORAGE_POOL_FREE_LIST;
         break;
      } /* Endif */

      next_block_ptr = (STOREBLOCK_STRUCT_PTR)
         NEXT_FREE(mem_pool_ptr->POOL_FREE_CHECK_BLOCK);
      if ( ! next_block_ptr ) {
         _int_enable();  /* If zero, free list has been completed */
         break;
      } /* Endif */
      if ( next_block_ptr->USER_AREA != (char _PTR_)mem_pool_ptr->POOL_FREE_CHECK_BLOCK ) {
         mem_pool_ptr->POOL_BLOCK_IN_ERROR = mem_pool_ptr->POOL_FREE_CHECK_BLOCK;
         _int_enable();
         result = MQX_CORRUPT_STORAGE_POOL_FREE_LIST;
         break;
      } /* Endif */
      mem_pool_ptr->POOL_FREE_CHECK_BLOCK = next_block_ptr;
      _int_enable();

   } /* Endwhile */

   if (result == MQX_OK) {
      _KLOGX2(KLOG_mem_test_pool, result);
   } else {
      _KLOGX3(KLOG_mem_test_pool, result, mem_pool_ptr->POOL_BLOCK_IN_ERROR);
   } /* Endif */
   return(result);

} /* Endbody */

/* EOF */
