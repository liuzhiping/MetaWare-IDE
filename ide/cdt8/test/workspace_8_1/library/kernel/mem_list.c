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
*** File: mem_list.c
***
*** Comments :
***   This file contains the function for returning the next memory block
*** on a queue of memory blocks.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _mem_get_next_block_internal
 * Returned Value   : pointer
 * Comments         :
 *   This routine returns what the next memory block is for
 * a given memory block (where the memory block is on a
 * tasks resource list)
 *
 *END*--------------------------------------------------------*/

pointer _mem_get_next_block_internal
   (
     /* [IN] the task descriptor being checked */
     TD_STRUCT_PTR          td_ptr, 

     /* [IN] the address (USERS_AREA) of the memory block */
     pointer                memory_ptr
   )
{ /* Body */
   STOREBLOCK_STRUCT_PTR    block_ptr;

   if (memory_ptr == NULL) {
      return(td_ptr->MEMORY_RESOURCE_LIST);
   } else {
      block_ptr = GET_MEMBLOCK_PTR(memory_ptr);
      return(block_ptr->NEXTBLOCK);
   } /* Endif */
   
} /* Endbody */

/* EOF */
