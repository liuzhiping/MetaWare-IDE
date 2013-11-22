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
*** File: mem_xftd.c
***
*** Comments :
***   Thie file contains the function for tranfering ownership of a memory
*** block between tasks.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _mem_transfer_td_internal
 * Returned Value   : _mqx_uint MQX_OK, or a mqx error code.
 * Comments         :
 *   This routine transfers the ownership of a block of memory from
 *   an owner task to another task.
 *
 *END*--------------------------------------------------------*/

_mqx_uint _mem_transfer_td_internal
   (  
      /* [IN] the memory block whose ownership is to be transferred */
      pointer  memory_ptr,
      
      /* [IN] the source (owner) td */
      TD_STRUCT_PTR source_td,
      
      /* [IN] the target (new owner) td */
      TD_STRUCT_PTR target_td
   )
{ /* Body */
   STOREBLOCK_STRUCT_PTR  block_ptr;
   /* Start CR 2366 */
   STOREBLOCK_STRUCT_PTR  prev_block_ptr = NULL; 
   /* End CR 2366 */
   STOREBLOCK_STRUCT_PTR  next_block_ptr;

   block_ptr = GET_MEMBLOCK_PTR(memory_ptr);

   next_block_ptr = (STOREBLOCK_STRUCT_PTR)source_td->MEMORY_RESOURCE_LIST;
   if (memory_ptr == next_block_ptr) {

	   /* START CR 1453 */
	   /* memory block is the first block in the list */
	   /* this should be the task stack unless it was pre-allocated */
	   if (source_td->FLAGS & TASK_STACK_PREALLOCATED) {

           /* Task stack was pre-allocated, so we can free this memory block */
           source_td->MEMORY_RESOURCE_LIST = next_block_ptr->NEXTBLOCK;
       } else {

           /* We can't free our stack and TD */
           return MQX_NOT_RESOURCE_OWNER;
       }
       /* END CR 1453 */

   } else { 
       /* Scan the task's memory resource list searching for the block.
        * Stop when the current pointer is equal to the block,
        * or the end of the list is reached.
        */
       while (  next_block_ptr  && ((pointer)next_block_ptr != memory_ptr) ) {
          prev_block_ptr = GET_MEMBLOCK_PTR(next_block_ptr);
          next_block_ptr = (STOREBLOCK_STRUCT_PTR)prev_block_ptr->NEXTBLOCK;
       } /* Endwhile */

       if ( ! next_block_ptr  ) {
          /* The specified block does not belong to the source task. */
          return(MQX_NOT_RESOURCE_OWNER);
       } /* Endif */

       /* Remove the memory block from the resource list of the
       ** source task.
       */
       prev_block_ptr->NEXTBLOCK = block_ptr->NEXTBLOCK;
    } /* Endif */
    _mem_transfer_internal(memory_ptr, target_td);

    return(MQX_OK);

 } /* Endbody */

/* EOF */
