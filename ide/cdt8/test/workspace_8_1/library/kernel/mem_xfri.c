/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: mem_xfri.c
***
*** Comments :
***   This file contains the function for transferring memory between
*** tasks.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _mem_transfer_internal
 * Returned Value   : _mqx_uint MQX_OK or an MQX error code
 * Comments         :
 *   This routine transfers the ownership of a block of memory from
 *   an owner task to another task.  
 *
 *END*--------------------------------------------------------*/

void _mem_transfer_internal
   (
     /* [IN] the address of the USER_AREA in the memory block to transfer */
     pointer       memory_ptr,

     /* [IN] the target task descriptor */
     TD_STRUCT_PTR target_td
   )
{ /* Body */
   STOREBLOCK_STRUCT_PTR  block_ptr;

   /* Verify the block */
   block_ptr = GET_MEMBLOCK_PTR(memory_ptr);

   /* Link the block onto the target's task descriptor. */
   block_ptr->NEXTBLOCK = target_td->MEMORY_RESOURCE_LIST;
   target_td->MEMORY_RESOURCE_LIST = (char _PTR_)(&block_ptr->USER_AREA);

   block_ptr->TASK_ID = target_td->TASK_ID;
   CALC_CHECKSUM(block_ptr);
 
} /* Endbody */

/* EOF */
