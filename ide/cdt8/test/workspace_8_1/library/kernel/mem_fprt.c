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
*** File: mem_fprt.c
***
*** Comments :
***   This file contains the function that will free part of an allocated
*** memory block.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_free_part
* Returned Value   : _mqx_uint an mqx error code
* Comments         :
*   This function cuts an allocated block to the requested size
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_free_part
   (
      /* [IN] the address of the memory block whose size is to change */
      pointer    mem_ptr,

      /* [IN] the new size for the block */
      _mem_size  requested_size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   STOREBLOCK_STRUCT_PTR  block_ptr;
   STOREBLOCK_STRUCT_PTR  prev_block_ptr;
   STOREBLOCK_STRUCT_PTR  next_block_ptr;
   STOREBLOCK_STRUCT_PTR  new_block_ptr;
   _mem_size              size;
   _mem_size              block_size;
   _mem_size              new_block_size;
   _mqx_uint              result_code;
/* Start CR 2356 */
   TD_STRUCT_PTR          td_ptr;
/* End CR 2356 */
   
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_mem_free_part, mem_ptr, requested_size);

#if MQX_CHECK_ERRORS
   /* Make sure a correct pointer was passed in.    */
   if (mem_ptr == NULL) {
      _task_set_error(MQX_INVALID_POINTER);
      _KLOGX2(KLOG_mem_free_part, MQX_INVALID_POINTER);
      return(MQX_INVALID_POINTER);
   } /* Endif */
#endif 

   /* Verify the block size */
   block_ptr = GET_MEMBLOCK_PTR(mem_ptr);

#if MQX_CHECK_ERRORS
   if (! _MEMORY_ALIGNED(block_ptr)) {
      _task_set_error(MQX_INVALID_POINTER);
      _KLOGX2(KLOG_mem_free_part, MQX_INVALID_POINTER);
      return(MQX_INVALID_POINTER);
   } /* Endif */

   if ( (block_ptr->BLOCKSIZE < MQX_MIN_MEMORY_STORAGE_SIZE) ||
      BLOCK_IS_FREE(block_ptr) )
   {
      _task_set_error(MQX_INVALID_POINTER);
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR = block_ptr;
      _KLOGX3(KLOG_mem_free_part, MQX_INVALID_POINTER, block_ptr);
      return(MQX_INVALID_POINTER);
   } /* Endif */
#endif

#if MQX_CHECK_VALIDITY   
   _int_disable();
   if ( ! VALID_CHECKSUM(block_ptr) ) {
      _int_enable();
      _task_set_error(MQX_INVALID_CHECKSUM);
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR = block_ptr;
      _KLOGX3(KLOG_mem_free_part, MQX_INVALID_CHECKSUM, block_ptr);
      return(MQX_INVALID_CHECKSUM);
   } /* Endif */
   _int_enable();
#endif

/* Start CR 2356 */
   td_ptr = SYSTEM_TD_PTR(kernel_data);
   if (block_ptr->TASK_ID != td_ptr->TASK_ID) {
      td_ptr = kernel_data->ACTIVE_PTR;
   } /* Endif */
/* End CR 2356 */

   /*  Walk through the memory resources of the task descriptor.
    *  Two pointers are maintained, one to the current block
    *  and one to the previous block.
    */

/* Start CR 2356 */
   next_block_ptr   = (STOREBLOCK_STRUCT_PTR)
      td_ptr->MEMORY_RESOURCE_LIST;
   prev_block_ptr = GET_MEMBLOCK_PTR(&td_ptr->MEMORY_RESOURCE_LIST);
/* End CR 2356 */

   /* Scan the task's memory resource list searching for the block to
    * free, Stop when the current pointer is equal to the block to free
    * or the end of the list is reached.
    */
   while (  next_block_ptr  &&
   ((pointer)next_block_ptr != mem_ptr) ) {
      /* The block is not found, and the end of the list has not been
       * reached, so move down the list.
       */
      prev_block_ptr = GET_MEMBLOCK_PTR(next_block_ptr);
      next_block_ptr = (STOREBLOCK_STRUCT_PTR)prev_block_ptr->NEXTBLOCK;
   } /* Endwhile */

#if MQX_CHECK_ERRORS
   if ( next_block_ptr == NULL ) {
      /* The specified block does not belong to the calling task. */
      _task_set_error(MQX_NOT_RESOURCE_OWNER);
      _KLOGX2(KLOG_mem_free_part, MQX_NOT_RESOURCE_OWNER);
      return(MQX_NOT_RESOURCE_OWNER);
   } /* Endif */
#endif

    /* determine the size of the block.  */
   block_size = block_ptr->BLOCKSIZE;

   size = requested_size + (_mem_size)FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA);
   if (size < MQX_MIN_MEMORY_STORAGE_SIZE) {
      size = MQX_MIN_MEMORY_STORAGE_SIZE;
   } /* Endif */
   _MEMORY_ALIGN_VAL_LARGER(size);

#if MQX_CHECK_ERRORS
   /* Verify that the size parameter is within range of the block size. */
   if (size <= block_size) {
#endif
      /* Adjust the size to allow for the overhead and force alignment */

      /* Compute the size of the new_ block that would be created. */
      new_block_size = block_size - size;

      /* Decide if it worthwile to split the block. If the amount of space
       * returned is not at least twice the size of the block overhead, 
       * then dont bother.
       */
      if (new_block_size >= (2*MQX_MIN_MEMORY_STORAGE_SIZE) ) {

         /* Create an 'inuse' block */
         new_block_ptr            = 
            (STOREBLOCK_STRUCT_PTR)((char _PTR_)block_ptr + size);
         new_block_ptr->BLOCKSIZE    = new_block_size;
         PREV_PHYS(new_block_ptr)    = block_ptr;
         new_block_ptr->TASK_ID      = block_ptr->TASK_ID;
         new_block_ptr->MEM_POOL_PTR = block_ptr->MEM_POOL_PTR;
         CALC_CHECKSUM(new_block_ptr);
         _int_disable();
         /* Split the block */
         block_ptr->BLOCKSIZE        = size;
         CALC_CHECKSUM(block_ptr);

         /* make sure right physical neighbour knows about it */
         block_ptr = NEXT_PHYS(new_block_ptr);
         PREV_PHYS(block_ptr) = new_block_ptr;
         CALC_CHECKSUM(block_ptr);

/* Start CR 2356 */
         /* Link the new block onto the requestor's task descriptor. */
         new_block_ptr->NEXTBLOCK = td_ptr->MEMORY_RESOURCE_LIST;
         td_ptr->MEMORY_RESOURCE_LIST =
            (char _PTR_)(&new_block_ptr->USER_AREA);
/* End CR 2356 */
         _int_enable();

         result_code = _mem_free((pointer)&new_block_ptr->USER_AREA);
      } else {
         result_code = MQX_OK;
      } /* Endif */
#if MQX_CHECK_ERRORS
   } else {
      result_code = MQX_INVALID_SIZE;
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if ( result_code != MQX_OK ) {
      _task_set_error(result_code);
   } /* Endif */
#endif

   _KLOGX2(KLOG_mem_free_part, result_code);
   return (result_code);

} /* Endbody */


/* EOF */
