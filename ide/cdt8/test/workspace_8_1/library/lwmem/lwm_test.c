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
*** File: _lwm_test.c
***
*** Comments :
***    This file contains the function that tests the lwmemory pools for
*** validity.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_test
* Returned Value   : _mqx_uint
*    A task error code on error, MQX_OK if no error
* Comments         :
*   This function checks the  memory pool for any errors.
*
*END*--------------------------------------------------------*/

_mqx_uint _lwmem_test
   (
      /* [OUT] - the pool in error */
      _lwmem_pool_id _PTR_ pool_error_ptr,

      /* [OUT] - the block in error */
      pointer        _PTR_ block_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_POOL_STRUCT_PTR  mem_pool_ptr;
   LWMEM_BLOCK_STRUCT_PTR queue_ptr = NULL;
   LWMEM_BLOCK_STRUCT_PTR block_ptr;
   _mqx_uint              i;
   _mqx_uint              result;
    
   _GET_KERNEL_DATA(kernel_data);

   /* 
   ** It is not considered an error if the lwmem component has not been
   ** created yet
   */
   if (kernel_data->LWMEM_POOLS.NEXT == NULL) {
      return(MQX_OK);
   } /* Endif */

   result = _queue_test(&kernel_data->LWMEM_POOLS, pool_error_ptr);
   if (result != MQX_OK) {
      return(result);
   } /* Endif */
   _int_disable();
   i  = _QUEUE_GET_SIZE(&kernel_data->LWMEM_POOLS);
   mem_pool_ptr = (LWMEM_POOL_STRUCT_PTR)(pointer)
      kernel_data->LWMEM_POOLS.NEXT;
   while (i--) {
      if (mem_pool_ptr->VALID != LWMEM_POOL_VALID) {
         _int_enable();
         *pool_error_ptr = mem_pool_ptr;
         *block_error_ptr = NULL;
         return(MQX_LWMEM_POOL_INVALID);
      } /* Endif */

      /* Make sure pool is ok */
      block_ptr = mem_pool_ptr->POOL_ALLOC_START_PTR;
      while ((uchar_ptr)block_ptr < (uchar_ptr)mem_pool_ptr->POOL_ALLOC_END_PTR){
         mem_pool_ptr->POOL_TEST2_PTR = block_ptr;
         _int_enable();
         _int_disable();
         block_ptr = mem_pool_ptr->POOL_TEST2_PTR;
         if (block_ptr->POOL != mem_pool_ptr) {
            _int_enable();
            *pool_error_ptr = mem_pool_ptr;
            *block_error_ptr = (pointer)block_ptr;
            return(MQX_CORRUPT_STORAGE_POOL);
         } /* Endif */
         block_ptr = (LWMEM_BLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + 
            block_ptr->BLOCKSIZE);
      } /* Endwhile */

      /* Make sure Freelist is ok */
      block_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
      while (block_ptr) {
         /* Provide window for higher priority tasks */
         mem_pool_ptr->POOL_TEST_PTR = block_ptr;
         _int_enable();
         _int_disable();
         block_ptr = mem_pool_ptr->POOL_TEST_PTR;
         if (((pointer)block_ptr < mem_pool_ptr->POOL_ALLOC_START_PTR) ||
             ((pointer)block_ptr > mem_pool_ptr->POOL_ALLOC_END_PTR) ||
             (block_ptr->POOL != mem_pool_ptr) ||
             (block_ptr->U.NEXTBLOCK &&
             (block_ptr->U.NEXTBLOCK <= (pointer)((uchar_ptr)block_ptr +
             block_ptr->BLOCKSIZE))))
         {
            /* This block is in error */
            _int_enable();
            *pool_error_ptr = mem_pool_ptr;
            *block_error_ptr = block_ptr;
            return(MQX_CORRUPT_STORAGE_POOL_FREE_LIST);
         } /* Endif */
         block_ptr = block_ptr->U.NEXTBLOCK;
      } /* Endwhile */
      mem_pool_ptr = (pointer)mem_pool_ptr->LINK.NEXT;
   } /* Endwhile */
   _int_enable();

   *pool_error_ptr = NULL;
   *block_error_ptr = NULL;
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
