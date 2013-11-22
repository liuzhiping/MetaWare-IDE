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
*** File: pa_extnd.c
***
*** Comments :
***  This file contains the function for adding another memory area to an
*** existing partition.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _partition_extend
* Returned Value   : _mqx_uint error code
*    MQX_OK, or an error code value
* Comments         :
*    adds more blocks of memory into the partition
*
*END*---------------------------------------------------------*/

_mqx_uint _partition_extend
   (
      /* [IN] the partition to add to */
      _partition_id partition,

      /* [IN] where the additional blocks are to start from */
      pointer       partition_location,

      /* [IN] the size of the partition. */
      _mem_size     partition_size
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR        kernel_data;)
   PARTPOOL_STRUCT_PTR                  partpool_ptr = (PARTPOOL_STRUCT_PTR)partition;
   PARTPOOL_BLOCK_STRUCT_PTR            partpool_block_ptr;
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR  block_ptr;
   _mqx_uint                            number_of_blocks;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_partition_extend, partition, partition_location, partition_size);

#if MQX_CHECK_VALIDITY
   if (partpool_ptr->POOL.VALID != PARTITION_VALID) {
      _KLOGX2(KLOG_partition_extend, PARTITION_INVALID);
      return(PARTITION_INVALID);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if (partpool_ptr->PARTITION_TYPE == PARTITION_DYNAMIC) {
      _KLOGX2(KLOG_partition_extend, MQX_INVALID_PARAMETER);
      return(MQX_INVALID_PARAMETER);
   } /* Endif */  
#endif

   partpool_block_ptr = (PARTPOOL_BLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(partition_location);
   partition_size     = partition_size - (_mem_size)partpool_block_ptr +
      (_mem_size)partition_location;
   _MEMORY_ALIGN_VAL_SMALLER(partition_size);

#if MQX_CHECK_ERRORS
   if (partition_size < (_mem_size)sizeof(PARTPOOL_STRUCT)) {
      _KLOGX2(KLOG_partition_extend, MQX_INVALID_PARAMETER);
      return(MQX_INVALID_PARAMETER);
   } /* Endif */  
#endif

   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
      ((uchar_ptr)partpool_block_ptr + sizeof(PARTPOOL_BLOCK_STRUCT));
   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(block_ptr);

   number_of_blocks = (partition_size -
      ((uchar_ptr)block_ptr - (uchar_ptr)partpool_block_ptr)) / 
      partpool_ptr->BLOCK_SIZE;
   partpool_ptr->TOTAL_BLOCKS += number_of_blocks;

   _partition_extend_internal(partpool_ptr, partpool_block_ptr,
      number_of_blocks);

   _KLOGX5(KLOG_partition_extend, partition, partpool_ptr, partition_size, MQX_OK);

   return MQX_OK;

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
