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
*** File: pa_tdel.c
***
*** Comments :
***  This file contains the function for cleaning up partitions after a
*** task has been deleted.
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
* Function Name    : _partition_cleanup
* Returned Value   : none
* Comments         :
*    returns all partition blocks owned by the task to the pool
*
*END*---------------------------------------------------------*/

void _partition_cleanup
   (
      /* [IN] the task being destroyed */
      TD_STRUCT_PTR td_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR              kernel_data;
   PARTPOOL_STRUCT_PTR                 partpool_ptr;
   PARTPOOL_BLOCK_STRUCT_PTR           partpool_block_ptr;
   PARTITION_COMPONENT_STRUCT_PTR      part_component_ptr;
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR block_ptr;
   _mqx_uint                           i;

   _GET_KERNEL_DATA(kernel_data);

   part_component_ptr = (PARTITION_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS];

   if (part_component_ptr == NULL) {
      return; /* No work to do! */
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (part_component_ptr->VALID != PARTITION_VALID) {
      return;
   } /* Endif */
#endif         

   partpool_ptr = (PARTPOOL_STRUCT_PTR)
      ((pointer)part_component_ptr->PARTITIONS.NEXT);
   while (partpool_ptr != (PARTPOOL_STRUCT_PTR)
      ((pointer)&part_component_ptr->PARTITIONS))
   {
      /* Check each partition */
      partpool_block_ptr = &partpool_ptr->POOL;
      while (partpool_block_ptr != NULL) {
         /* Check each poolblock in the partition */
#if MQX_CHECK_VALIDITY
         if (partpool_block_ptr->VALID != PARTITION_VALID) {
            break;
         }/* Endif */
#endif         
         block_ptr = partpool_block_ptr->FIRST_IBLOCK_PTR;
         i = partpool_block_ptr->NUMBER_OF_BLOCKS + 1;
         while (--i) {
            if ((block_ptr->TASK_ID == td_ptr->TASK_ID) &&
               (block_ptr->LINK.PARTITION_PTR == partpool_ptr))
            { /* An allocated Block */
               _int_disable();
               block_ptr->LINK.NEXT_BLOCK_PTR = partpool_ptr->FREE_LIST_PTR;
               partpool_ptr->FREE_LIST_PTR = block_ptr;
               ++partpool_ptr->AVAILABLE;
               CALC_PARTITION_CHECKSUM(block_ptr);
               _int_enable();
            } /* Endif */
            block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
               ((uchar_ptr)block_ptr + partpool_ptr->BLOCK_SIZE);
         } /* Endwhile */
         partpool_block_ptr = partpool_block_ptr->NEXT_POOL_PTR;
      } /* Endwhile */

      partpool_ptr = (PARTPOOL_STRUCT_PTR)partpool_ptr->NEXT;
   } /* Endwhile */

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
