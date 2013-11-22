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
*** File: pa_creaa.c
***
*** Comments :
***  This file contains the function for creating a partition at a location.
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
* Function Name    : _partition_create_at
* Returned Value   : _partition_id - ID of partition created if successful
*   or PARTITION_NULL_ID otherwise
* Comments         :
*   Create a partition at a location, with blocks of a certain size.
*
*END*-----------------------------------------------------------------*/

_partition_id _partition_create_at
   ( 
      /* [IN] where the partition is to start from. */
      pointer    partition_location,

      /* [IN] the size of the partition */
      _mem_size  partition_size,

      /* [IN] the size of each block in the partition in addressable uints */
      _mem_size  block_size
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   PARTPOOL_STRUCT_PTR           partpool_ptr;
   uchar_ptr                     firstblock_ptr;
   _mqx_uint                     number_of_blocks;
   _mqx_uint                     result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_partition_create_at, partition_location, partition_size, block_size);

#if MQX_CHECK_ERRORS
   if (block_size == 0) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_partition_create_at, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   } /* Endif */

   if (partition_size < sizeof(PARTPOOL_STRUCT)) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_partition_create_at, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   } /* Endif */  
#endif

   block_size += (_mem_size)sizeof(INTERNAL_PARTITION_BLOCK_STRUCT);
   _MEMORY_ALIGN_VAL_LARGER(block_size);

   partpool_ptr = (PARTPOOL_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(partition_location);
   
   firstblock_ptr  = (uchar_ptr)partpool_ptr + sizeof(PARTPOOL_STRUCT);
   firstblock_ptr  = (uchar_ptr)_ALIGN_ADDR_TO_HIGHER_MEM(firstblock_ptr);
   partition_size -= (_mem_size)firstblock_ptr - (_mem_size)partition_location;

   number_of_blocks = (_mqx_uint)(partition_size / block_size);

#if MQX_CHECK_ERRORS
   if (!number_of_blocks) {
      _KLOGX2(KLOG_partition_create_at, PARTITION_NULL_ID);
      return(PARTITION_NULL_ID);
   } /* Endif */
#endif

   result = _partition_create_internal(partpool_ptr, block_size, 
      number_of_blocks);
#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      _KLOGX2(KLOG_partition_create_at, result);
      return(PARTITION_NULL_ID);
   } /* Endif */
#endif

   partpool_ptr->PARTITION_TYPE = PARTITION_STATIC;

   _KLOGX5(KLOG_partition_create_at, partpool_ptr, partition_size, block_size, MQX_OK);
   return((_partition_id)partpool_ptr);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
