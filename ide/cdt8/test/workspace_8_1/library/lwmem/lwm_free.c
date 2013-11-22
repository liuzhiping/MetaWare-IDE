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
*** File: lwm_free.c
***
*** Comments :
***  This file contains the function that frees dynamically allocated memory.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_free
* Returned Value   : _mqx_uint MQX_OK or a MQX error code.
* Comments         :
*   This function frees the given block of memory.  It performs
*   error checking validating that the block being freed was 
*   obtained by the same task who is freeing it.  
*   It also coalesces any free block found physically
*   on either side of the block being freed.
*   If coalescing is not possible, then the block is placed onto the free list.
*
*END*---------------------------------------------------------*/

_mqx_uint _lwmem_free
   (
      /* [IN] the address of the memory block to free */
      pointer mem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   LWMEM_BLOCK_STRUCT_PTR  block_ptr;
   LWMEM_BLOCK_STRUCT_PTR  free_list_ptr;
   LWMEM_POOL_STRUCT_PTR   mem_pool_ptr;
   /* Start CR 321 */
   boolean                 insert;
   /* End CR 321 */
   
   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_lwmem_free, mem_ptr);

#if MQX_CHECK_ERRORS
   /* Verify the passed in parameter */
   if (mem_ptr == NULL) {
      _task_set_error(MQX_INVALID_POINTER);
      _KLOGX2(KLOG_lwmem_free, MQX_INVALID_POINTER);
      return(MQX_INVALID_POINTER);
   } /* Endif */
#endif

   block_ptr = GET_LWMEMBLOCK_PTR(mem_ptr);
   mem_pool_ptr = (LWMEM_POOL_STRUCT_PTR)block_ptr->POOL;
#if MQX_CHECK_VALIDITY
   if (mem_pool_ptr->VALID != LWMEM_POOL_VALID) {
      _task_set_error(MQX_LWMEM_POOL_INVALID);
      _KLOGX2(KLOG_lwmem_free, MQX_LWMEM_POOL_INVALID);
      return(MQX_LWMEM_POOL_INVALID);
   } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   /* Verify the passed in parameter */
   if (! ((block_ptr->U.TASK_ID == kernel_data->ACTIVE_PTR->TASK_ID) ||
      (block_ptr->U.TASK_ID == SYSTEM_TD_PTR(kernel_data)->TASK_ID)))
   {
      _task_set_error(MQX_NOT_RESOURCE_OWNER);
      return(MQX_NOT_RESOURCE_OWNER);
   } /* Endif */
#endif

   _int_disable();
   free_list_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
   /* Start CR 321 */
   while (TRUE) {
      if ((mem_pool_ptr->POOL_FREE_LIST_PTR == NULL) || 
         ((pointer)block_ptr < mem_pool_ptr->POOL_FREE_LIST_PTR)) 
      {
         free_list_ptr = mem_pool_ptr->POOL_FREE_LIST_PTR;
         if (((uchar_ptr)block_ptr + block_ptr->BLOCKSIZE) ==
            (uchar_ptr)free_list_ptr)
         {
            /* Join with the next block on the list */
            block_ptr->BLOCKSIZE += free_list_ptr->BLOCKSIZE;
            block_ptr->U.NEXTBLOCK = free_list_ptr->U.NEXTBLOCK;
         } else {
           block_ptr->U.NEXTBLOCK = free_list_ptr;
         } /* Endif */
         mem_pool_ptr->POOL_FREE_LIST_PTR = block_ptr;
         insert = FALSE;
         break;
      }/* Endif */
      if (((pointer)block_ptr < free_list_ptr->U.NEXTBLOCK) || 
         (free_list_ptr->U.NEXTBLOCK == NULL)) 
      {
         insert = TRUE;
         break;
      }/* Endif */
      free_list_ptr = free_list_ptr->U.NEXTBLOCK;
      /* Provide window for higher priority tasks */
      mem_pool_ptr->POOL_FREE_PTR = free_list_ptr;
      _int_enable();
      _int_disable();
      free_list_ptr = mem_pool_ptr->POOL_FREE_PTR;
   } /* Endwhile */

   if (insert) {
      /* 
      ** We are between the two blocks where we are to be inserted, 
      ** free_list_ptr is before block_ptr,
      ** block_ptr may be at end of list.
      */
      if (((uchar_ptr)block_ptr + block_ptr->BLOCKSIZE) == 
        free_list_ptr->U.NEXTBLOCK) 
      {
         /* Join with the next block on the list */
         block_ptr->BLOCKSIZE += ((LWMEM_BLOCK_STRUCT_PTR)
            (free_list_ptr->U.NEXTBLOCK))->BLOCKSIZE;
         block_ptr->U.NEXTBLOCK = ((LWMEM_BLOCK_STRUCT_PTR)
            (free_list_ptr->U.NEXTBLOCK))->U.NEXTBLOCK;
      } else {
         /* this block is to be inserted */
         block_ptr->U.NEXTBLOCK = free_list_ptr->U.NEXTBLOCK;
      } /* Endif */
      
      if (((uchar_ptr)free_list_ptr + free_list_ptr->BLOCKSIZE ==
         (uchar_ptr)block_ptr))
      {
         free_list_ptr->BLOCKSIZE += block_ptr->BLOCKSIZE;
         free_list_ptr->U.NEXTBLOCK = block_ptr->U.NEXTBLOCK;
      } else {
         free_list_ptr->U.NEXTBLOCK = block_ptr;
      } /* Endif */
   
   } /* Endif */   
   /* End CR 321 */
   
   /* Start CR 319 */
   mem_pool_ptr->POOL_ALLOC_PTR = mem_pool_ptr->POOL_FREE_LIST_PTR;
   /* End CR 319 */
   mem_pool_ptr->POOL_FREE_PTR  = mem_pool_ptr->POOL_FREE_LIST_PTR;
   mem_pool_ptr->POOL_TEST_PTR  = mem_pool_ptr->POOL_FREE_LIST_PTR;
   mem_pool_ptr->POOL_TEST2_PTR = mem_pool_ptr->POOL_ALLOC_START_PTR;
#if MQX_TASK_DESTRUCTION
   mem_pool_ptr->POOL_DESTROY_PTR = mem_pool_ptr->POOL_ALLOC_START_PTR;
#endif
   _int_enable();
   
   _KLOGX2(KLOG_lwmem_free, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */
