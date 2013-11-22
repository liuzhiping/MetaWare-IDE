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
*** File: pa_creat.c
***
*** Comments :
***  This file contains the function for creating a partition.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*--------------------------------------------------------------
* 
* Function Name    : _partition_create
* Returned Value   : _partition_id - ID of partition created if successful
*   or PARTITION_NULL_ID otherwise
* Comments         :
*    Create a partition from kernel memory,
*  with a certain number of blocks.
*
*END*-----------------------------------------------------------------*/

_partition_id _partition_create
   ( 
      /* 
      ** [IN] the size of each block in the partition in smallest 
      ** addressable units
      */
      _mem_size  block_size,
      
      /* [IN] the initial number of blocks in the partition */
      _mqx_uint  initial_blocks,

      /* 
      ** [IN] the number of blocks to grow by if all partition blocks are 
      ** in use 
      */
      _mqx_uint  grow_blocks,

      /* [IN] the maximum number of blocks to allow in the partition */
      _mqx_uint  maximum_blocks
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PARTPOOL_STRUCT_PTR           partpool_ptr;
   _mem_size                     actual_size;
   _mqx_uint                     result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE5(KLOG_partition_create, block_size, initial_blocks, grow_blocks,
      maximum_blocks);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _task_set_error(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      _KLOGX2(KLOG_partition_create, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   } /* Endif */

   if (block_size == 0) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_partition_create, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   } /* Endif */
#endif

   actual_size = (_mem_size)sizeof(INTERNAL_PARTITION_BLOCK_STRUCT) + block_size;
   _MEMORY_ALIGN_VAL_LARGER(actual_size);

#if PSP_MEMORY_ALIGNMENT
   partpool_ptr = (PARTPOOL_STRUCT_PTR)_mem_alloc_system(
      (_mem_size)(sizeof(PARTPOOL_STRUCT) + PSP_MEMORY_ALIGNMENT + 
      (actual_size * initial_blocks)));
#else
   partpool_ptr = ((PARTPOOL_STRUCT_PTR))_mem_alloc_system(
      (_mem_size)(sizeof(PARTPOOL_STRUCT) + (actual_size * initial_blocks)));
#endif
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (partpool_ptr == NULL) {
      _KLOGX2(KLOG_partition_create, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   }/* Endif */
#endif

   result = _partition_create_internal(partpool_ptr, actual_size, 
      initial_blocks);
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (result != MQX_OK) {
      _KLOGX2(KLOG_partition_create, PARTITION_NULL_ID);
      _mem_free(partpool_ptr);
      return(PARTITION_NULL_ID);
   } /* Endif */
#endif

   partpool_ptr->PARTITION_TYPE = PARTITION_DYNAMIC;
   partpool_ptr->GROW_BLOCKS    = grow_blocks;
   partpool_ptr->MAXIMUM_BLOCKS = maximum_blocks;

   _KLOGX3(KLOG_partition_create, partpool_ptr, MQX_OK);
   return((_partition_id)partpool_ptr);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
