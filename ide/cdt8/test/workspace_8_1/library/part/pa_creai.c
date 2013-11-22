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
*** File: pa_creai.c
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
* Function Name    : _partition_create_internal
* Returned Value   : _mqx_uint - MQX_OK or error
* Comments         :
*    Create a partition at a location, 
* with a specified number of blocks.
*
*END*-----------------------------------------------------------------*/

_mqx_uint _partition_create_internal
   ( 
      /* [IN] the start of the partition */
      PARTPOOL_STRUCT_PTR partpool_ptr,
      
      /* [IN] the total size of each block with overheads */
      _mem_size           actual_size,

      /* [IN] the initial number of blocks in the partition */
      _mqx_uint           initial_blocks

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR              kernel_data;
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR block_ptr;
   PARTITION_COMPONENT_STRUCT_PTR      part_component_ptr;
   _mqx_uint                           result;

   _GET_KERNEL_DATA(kernel_data);

   part_component_ptr = (PARTITION_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS];
   if (part_component_ptr == NULL) {
      result = _partition_create_component();
      part_component_ptr = (PARTITION_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (part_component_ptr == NULL){
         return(result);
      } /* Endif */
#endif
   } /* Endif */

   _mem_zero(partpool_ptr, (_mem_size)sizeof(PARTPOOL_STRUCT));

   partpool_ptr->BLOCK_SIZE            = actual_size;
/* START CR 308 */
   partpool_ptr->POOL.VALID            = PARTITION_VALID;
/* END CR 308 */
   partpool_ptr->POOL.NUMBER_OF_BLOCKS = initial_blocks;
   partpool_ptr->AVAILABLE             = initial_blocks;
   partpool_ptr->TOTAL_BLOCKS          = initial_blocks;

   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)((uchar_ptr)partpool_ptr + 
      sizeof(PARTPOOL_STRUCT));
   
   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(block_ptr);

   partpool_ptr->POOL.FIRST_IBLOCK_PTR = block_ptr;
   
   while (initial_blocks--)  {
      block_ptr->LINK.NEXT_BLOCK_PTR = partpool_ptr->FREE_LIST_PTR;
      CALC_PARTITION_CHECKSUM(block_ptr);
      partpool_ptr->FREE_LIST_PTR  = block_ptr;
      block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + 
         actual_size);
   } /* Endwhile */
   
   _int_disable();
   _QUEUE_ENQUEUE(&part_component_ptr->PARTITIONS, partpool_ptr);
   _int_enable();

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
