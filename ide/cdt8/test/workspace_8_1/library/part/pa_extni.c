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
*** File: pa_extni.c
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
* Function Name    : _partition_extend_internal
* Returned Value   : void
* Comments         :
*    adds more blocks of memory into the partition
*
*END*---------------------------------------------------------*/

void _partition_extend_internal
   (
      /* [IN] the partition to add to */
      PARTPOOL_STRUCT_PTR        partpool_ptr,

      /* [IN] the new block to add */
      PARTPOOL_BLOCK_STRUCT_PTR  partpool_block_ptr,
      
      /* [IN] the number of blocks to add */
      _mqx_uint                  number_of_blocks
   )
{ /* Body */
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR block_ptr;

/* START CR 308 */
   partpool_block_ptr->VALID            = PARTITION_VALID;
/* END CR 308 */
   partpool_block_ptr->NUMBER_OF_BLOCKS = number_of_blocks;

   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)(
      (uchar_ptr)partpool_block_ptr + sizeof(PARTPOOL_BLOCK_STRUCT));
   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
      _ALIGN_ADDR_TO_HIGHER_MEM(block_ptr);

   partpool_block_ptr->FIRST_IBLOCK_PTR = block_ptr;
   
   while (number_of_blocks--) {
      _int_disable();
      block_ptr->LINK.NEXT_BLOCK_PTR = partpool_ptr->FREE_LIST_PTR;
      CALC_PARTITION_CHECKSUM(block_ptr);
      partpool_ptr->FREE_LIST_PTR    = block_ptr;
      partpool_ptr->AVAILABLE++;
      _int_enable();
      block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
         ((uchar_ptr)block_ptr + partpool_ptr->BLOCK_SIZE);
   } /* Endwhile */
   
   _int_disable();
   partpool_block_ptr->NEXT_POOL_PTR = partpool_ptr->POOL.NEXT_POOL_PTR;
   partpool_ptr->POOL.NEXT_POOL_PTR  = partpool_block_ptr;
   _int_enable();

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
